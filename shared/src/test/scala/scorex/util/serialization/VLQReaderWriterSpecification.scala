package scorex.util.serialization

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.Assertion
import org.scalatest.propspec.AnyPropSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import scorex.util.TestHelpers._
import scorex.util.Generators

trait VLQReaderWriterSpecification extends AnyPropSpec
  with Generators
  with ScalaCheckPropertyChecks
  with Matchers {

  def byteBufReader(bytes: Array[Byte]): VLQReader
  def byteArrayWriter(): VLQWriter

  private val seqPrimValGen: Gen[Seq[Any]] = for {
    length <- Gen.chooseNum(1, 1000)
    anyValSeq <- Gen.listOfN(length,
      Gen.oneOf[Any](
        Arbitrary.arbByte.arbitrary,
        Arbitrary.arbShort.arbitrary,
        Arbitrary.arbInt.arbitrary,
        Arbitrary.arbLong.arbitrary,
        arrayGen[Byte],
        arrayGen[Boolean]))
  } yield anyValSeq

  // source: http://github.com/google/protobuf/blob/a7252bf42df8f0841cf3a0c85fdbf1a5172adecb/java/core/src/test/java/com/google/protobuf/CodedInputStreamTest.java#L239
  private val expectedValues: Seq[(Array[Byte], Long)] = Seq(
    (bytesFromInts(0x00), 0),
    (bytesFromInts(0x01), 1),
    (bytesFromInts(0x7f), 127),
    // 14882
    (bytesFromInts(0xa2, 0x74), (0x22 << 0) | (0x74 << 7)),
    // 2961488830
    (bytesFromInts(0xbe, 0xf7, 0x92, 0x84, 0x0b),
      (0x3e << 0) | (0x77 << 7) | (0x12 << 14) | (0x04 << 21) | (0x0bL << 28)),
    // 64-bit
    // 7256456126
    (bytesFromInts(0xbe, 0xf7, 0x92, 0x84, 0x1b),
      (0x3e << 0) | (0x77 << 7) | (0x12 << 14) | (0x04 << 21) | (0x1bL << 28)),
    // 41256202580718336
    (bytesFromInts(0x80, 0xe6, 0xeb, 0x9c, 0xc3, 0xc9, 0xa4, 0x49),
      (0x00 << 0) | (0x66 << 7) | (0x6b << 14) | (0x1c << 21) | (0x43L << 28) | (0x49L << 35) | (0x24L << 42) | (0x49L << 49)),
    // 11964378330978735131 (-6482365742730816485)
    (bytesFromInts(0x9b, 0xa8, 0xf9, 0xc2, 0xbb, 0xd6, 0x80, 0x85, 0xa6, 0x01),
      (0x1b << 0) | (0x28 << 7) | (0x79 << 14) | (0x42 << 21) | (0x3bL << 28) | (0x56L << 35) | (0x00L << 42) | (0x05L << 49) | (0x26L << 56) | (0x01L << 63))
  )

  property("predefined long values and serialized data round trip") {
    expectedValues.foreach { case (bytes, v) =>
      val writer = byteArrayWriter()
      writer.putULong(v)
      val encodedBytes = writer.toBytes
      withClue(s"for value $v: \n") {
        encodedBytes shouldEqual bytes
      }

      val r = byteBufReader(encodedBytes)
      withClue(s"for bytes $bytes: \n") {
        r.getULong() shouldEqual v
      }
      r.remaining shouldBe 0
    }
  }

  property("round trip serialization/deserialization of arbitrary value list") {
    // increase threshold to make sure we cover a lot of types combination
    // and a good diversity withing a values of the each type
    forAll(seqPrimValGen, minSuccessful(500)) { (values: Seq[Any]) =>
      val writer = byteArrayWriter()
      for (any <- values) {
        any match {
          case v: Byte => writer.put(v)
          case v: Short =>
            writer.putShort(v)
            if (v >= 0) writer.putUShort(v.toInt)
          case v: Int =>
            writer.putInt(v)
            if (v >= 0) writer.putUInt(v.toLong)
          case v: Long =>
            // test all paths
            writer.putLong(v)
            writer.putULong(v)
          case v: Array[Byte] => writer.putUShort(v.length).putBytes(v)
          case v: Array[Boolean] => writer.putUShort(v.length).putBits(v)
          case _ => fail(s"writer: unsupported value type: ${any.getClass}");
        }
      }
      val reader = byteBufReader(writer.toBytes)
      values.foreach {
        case v: Byte => reader.getByte() shouldEqual v
        case v: Short =>
          // test all paths
          reader.getShort() shouldEqual v
          if (v >= 0) reader.getUShort().toShort shouldEqual v
        case v: Int =>
          reader.getInt() shouldEqual v
          if (v >= 0) reader.getUInt() shouldEqual v
        case v: Long =>
          // test all paths
          reader.getLong() shouldEqual v
          reader.getULong() shouldEqual v
        case v: Array[Byte] =>
          val size = reader.getUShort()
          reader.getBytes(size) shouldEqual v
        case v: Array[Boolean] =>
          val size = reader.getUShort()
          reader.getBits(size) shouldEqual v
        case ref => fail(s"reader: unsupported value type: ${ref.getClass}");
      }
    }
  }

  private def bytesLong(v: Long): Array[Byte] =
    byteArrayWriter().putULong(v).toBytes

  private def checkSize(low: Long, high: Long, size: Int): Assertion = {
    // Gen.choose does not always include range limit values
    bytesLong(low).length shouldBe size
    bytesLong(high).length shouldBe size
    forAll(Gen.choose(low, high)) { (v: Long) =>
      bytesLong(v).length shouldBe size
    }
  }

  property("size of serialized data") {
    // source: http://github.com/scodec/scodec/blob/055eed8386aa85ff27dba3f72b104a8aa3d6012d/unitTests/src/test/scala/scodec/codecs/VarLongCodecTest.scala#L16
    checkSize(0L, 127L, 1)
    checkSize(128L, 16383L, 2)
    checkSize(16384L, 2097151L, 3)
    checkSize(2097152L, 268435455L, 4)
    checkSize(268435456L, 34359738367L, 5)
    checkSize(34359738368L, 4398046511103L, 6)
    checkSize(4398046511104L, 562949953421311L, 7)
    checkSize(562949953421312L, 72057594037927935L, 8)
    checkSize(72057594037927936L, Long.MaxValue, 9)
    checkSize(Long.MinValue, -1L, 10)
  }

  private def bytesZigZaggedLong(v: Long): Array[Byte] =
    byteArrayWriter().putLong(v).toBytes

  private def checkSizeZigZagged(low: Long, high: Long, size: Int): Unit = {
    // Gen.choose does not always include range limit values
    bytesZigZaggedLong(low).length shouldBe size
    bytesZigZaggedLong(high).length shouldBe size
    forAll(Gen.choose(low, high)) { (v: Long) =>
      bytesZigZaggedLong(v).length shouldBe size
    }
  }

  property("size of serialized zigzag'ed data") {
    checkSizeZigZagged(-64L, 64L - 1, 1)

    checkSizeZigZagged(-8192L, -64L - 1, 2)
    checkSizeZigZagged(64L, 8192L - 1, 2)

    checkSizeZigZagged(-1048576L, -8192L - 1, 3)
    checkSizeZigZagged(8192L, 1048576L - 1, 3)

    checkSizeZigZagged(-134217728L, -1048576L - 1, 4)
    checkSizeZigZagged(1048576L, 134217728L - 1,  4)

    checkSizeZigZagged(-17179869184L, -134217728L - 1, 5)
    checkSizeZigZagged(134217728L, 17179869184L - 1,  5)

    checkSizeZigZagged(-2199023255552L, -17179869184L - 1, 6)
    checkSizeZigZagged(17179869184L, 2199023255552L - 1,  6)

    checkSizeZigZagged(-281474976710656L, -2199023255552L - 1, 7)
    checkSizeZigZagged(2199023255552L, 281474976710656L - 1,  7)

    checkSizeZigZagged(-36028797018963968L, -281474976710656L - 1, 8)
    checkSizeZigZagged(281474976710656L, 36028797018963968L - 1,  8)

    checkSizeZigZagged(Long.MinValue / 2, -36028797018963968L - 1, 9)
    checkSizeZigZagged(36028797018963968L, Long.MaxValue / 2,  9)

    checkSizeZigZagged(Long.MinValue, Long.MinValue / 2 - 1, 10)
    checkSizeZigZagged(Long.MaxValue / 2 + 1, Long.MaxValue,  10)
  }

  property("fail deserialization by deliberately messing with different methods") {
    forAll(Gen.chooseNum(1L, Long.MaxValue)) { (v: Long) =>
      val writer = byteArrayWriter()
      writer.putULong(v)
      writer.putLong(v)
      val reader = byteBufReader(writer.toBytes)
      reader.getLong() should not be v
      reader.getULong() should not be v
    }
  }

  property("malformed input for deserialization") {
    // source: http://github.com/google/protobuf/blob/a7252bf42df8f0841cf3a0c85fdbf1a5172adecb/java/core/src/test/java/com/google/protobuf/CodedInputStreamTest.java#L281
    assertThrows[RuntimeException](byteBufReader(bytesFromInts(0x80)).getULong())
    assertThrows[RuntimeException](byteBufReader(bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x00)).getULong())
  }



  property("Coll[Boolean] bit encoding format") {
    val expectations = Seq[(Array[Boolean], Array[Byte])](
      Array[Boolean]() -> Array[Byte](),
      Array(false) -> Array(0),
      Array(true) -> Array(1),
      Array(false, false, true) -> Array(4), // 00000100
      Array(true, true, false) -> Array(3), // 00000011
      Array(true, false, true) -> Array(5), // 00000101
      (Array.fill(8)(false) :+ true) -> Array(0, 1), // 00000000 00000001
      (Array.fill(9)(false) :+ true) -> Array(0, 2), // 00000000 00000010
      (Array.fill(10)(false) :+ true) -> Array(0, 4) // 00000000 00000100
    )
    expectations.foreach { case (bools, bytes) =>
      byteArrayWriter().putBits(bools).toBytes shouldEqual bytes
      byteBufReader(bytes).getBits(bools.length) shouldEqual bools
    }
  }

  property("putUByte range check assertion") {
    val w = byteArrayWriter()
    w.putUByte(0)
    w.putUByte(255)
    an[IllegalArgumentException] should be thrownBy w.putUByte(-1)
    an[IllegalArgumentException] should be thrownBy w.putUByte(256)
  }

  property("putUShort range check assertion") {
    val w = byteArrayWriter()
    w.putUShort(0)
    w.putUShort(0xFFFF)
    an[IllegalArgumentException] should be thrownBy w.putUShort(-1)
    an[IllegalArgumentException] should be thrownBy w.putUShort(0xFFFF + 1)
  }

  property("putUInt range check assertion") {
    val w = byteArrayWriter()
    w.putUInt(0)
    w.putUInt(0xFFFFFFFFL)
    an[IllegalArgumentException] should be thrownBy w.putUInt(-1)
    an[IllegalArgumentException] should be thrownBy w.putUInt(0xFFFFFFFFL + 1)
  }

  property("getUShort range check assertion") {
    def check(in: Int): Unit =
      byteBufReader(byteArrayWriter().putUInt(in.toLong).toBytes).getUShort() shouldBe in

    def checkFail(in: Int): Unit =
      an[IllegalArgumentException] should be thrownBy
        byteBufReader(byteArrayWriter().putUInt(in.toLong).toBytes).getUShort()

    check(0)
    check(0xFFFF)
    checkFail(-1)
    checkFail(0xFFFF + 1)
    checkFail(Int.MaxValue)
  }

  property("getBytes size check") {
    val bytes = Array[Byte](1, 2, 3)

    { // successful case
      val r = byteBufReader(bytes)
      r.getBytes(3) shouldBe bytes
    }
    
    { // successful case 2
      val r = byteBufReader(bytes)
      r.position = 2
      r.getBytes(1) shouldBe bytes.slice(2, 3)
    }

    { // failure case
      val r = byteBufReader(bytes)
      an[IllegalArgumentException] should be thrownBy {
        r.getBytes(4)
      }
    }

    { // failure case 2
      val r = byteBufReader(bytes)
      r.position = 2
      an[IllegalArgumentException] should be thrownBy {
        r.getBytes(2)
      }
    }
  }

  property("getUInt range check assertion") {
    def check(in: Long): Unit =
      byteBufReader(byteArrayWriter().putULong(in).toBytes).getUInt() shouldBe in

    def checkFail(in: Long): Unit =
      an[IllegalArgumentException] should be thrownBy
        byteBufReader(byteArrayWriter().putULong(in).toBytes).getUInt()

    check(0)
    check(0xFFFFFFFFL)
    checkFail(-1)
    checkFail(0xFFFFFFFFL + 1L)
    checkFail(Long.MaxValue)
  }

  property("Byte roundtrip") {
    forAll { (v: Byte) => byteBufReader(byteArrayWriter().put(v).toBytes).getByte() shouldBe v }
  }

  property("unsigned Byte roundtrip") {
    forAll { (v: Byte) =>
      val uv: Int = v & 0xFF
      byteBufReader(byteArrayWriter().putUByte(uv).toBytes).getUByte() shouldBe uv
    }
  }

  property("Short roundtrip") {
    forAll { (v: Short) => byteBufReader(byteArrayWriter().putShort(v).toBytes).getShort() shouldBe v }
  }

  property("unsigned Short roundtrip") {
    forAll { (v: Short) =>
      val uv: Int = v & 0xFFFF
      byteBufReader(byteArrayWriter().putUShort(uv).toBytes).getUShort() shouldBe uv
    }
  }

  property("Int roundtrip") {
    forAll { (v: Int) => byteBufReader(byteArrayWriter().putInt(v).toBytes).getInt() shouldBe v }
  }

  property("unsigned Int roundtrip") {
    forAll { (v: Int) =>
      val uv: Long = v.toLong + (Int.MinValue.toLong * -1)
      byteBufReader(byteArrayWriter().putUInt(uv).toBytes).getUInt() shouldBe uv
    }
  }

  property("Long roundtrip") {
    forAll { (v: Long) => byteBufReader(byteArrayWriter().putLong(v).toBytes).getLong() shouldBe v }
  }

  property("ULong roundtrip") {
    forAll { (v: Long) => byteBufReader(byteArrayWriter().putULong(v).toBytes).getULong() shouldBe v }
  }

  property("Boolean array roundtrip") {
    forAll { (v: Array[Boolean]) => byteBufReader(byteArrayWriter().putBits(v).toBytes).getBits(v.length) shouldBe v }
  }

  property("short string roundtrip") {
    forAll(Gen.alphaStr.suchThat(_.length < 256)) { (v: String) =>
      byteBufReader(byteArrayWriter().putShortString(v).toBytes).getShortString() shouldBe v
    }
  }

  property("byte corner cases") {
    def roundtrip(v: Byte, expected: Array[Byte]): Unit = {
      val bytes = byteArrayWriter().put(v).toBytes
      bytes shouldEqual expected
      byteBufReader(expected).getByte() shouldBe v
    }

    roundtrip(Byte.MinValue, bytesFromInts(Byte.MinValue))
    roundtrip(0, bytesFromInts(0))
    roundtrip(Byte.MaxValue, bytesFromInts(Byte.MaxValue))
  }

  property("unsigned byte corner cases") {
    def roundtrip(v: Int, expected: Array[Byte]): Unit = {
      val bytes = byteArrayWriter().putUByte(v).toBytes
      bytes shouldEqual expected
      byteBufReader(expected).getUByte() shouldBe v
    }

    an[IllegalArgumentException] should be thrownBy roundtrip(-1, bytesFromInts(0))
    roundtrip(0, bytesFromInts(0))
    roundtrip(255, bytesFromInts(255))
    an[IllegalArgumentException] should be thrownBy roundtrip(256, bytesFromInts(0))
  }

  private def prettyPrint(arr: Array[Byte]): String =
    arr.map(b => String.format("0x%02X", Byte.box(b))).mkString(", ")

  property("Short corner cases") {
    def roundtrip(v: Short, expected: Array[Byte]): Unit = {
      val bytes = byteArrayWriter().putShort(v).toBytes
      withClue(s"for value $v got bytes ${prettyPrint(bytes)} (expected ${prettyPrint(expected)}): \n") {
        bytes shouldEqual expected
      }
      byteBufReader(expected).getShort() shouldBe v
    }
    roundtrip(Short.MinValue, bytesFromInts(0xFF, 0xFF, 0x03))
    roundtrip(-8194, bytesFromInts(0x83, 0x80, 0x01))
    roundtrip(-8193, bytesFromInts(0x81, 0x80, 0x01))
    roundtrip(-8192, bytesFromInts(0xFF, 0x7F))
    roundtrip(-8191, bytesFromInts(0xFD, 0x7F))
    roundtrip(-66, bytesFromInts(0x83, 0x01))
    byteBufReader( bytesFromInts(0x83, 0x00)).getShort() shouldBe -2
    roundtrip(-65, bytesFromInts(0x81, 0x01))
    byteBufReader( bytesFromInts(0x81, 0x00)).getShort() shouldBe -1
    roundtrip(-64, bytesFromInts(0x7F))
    roundtrip(-63, bytesFromInts(0x7D))
    roundtrip(-1, bytesFromInts(0x01))
    roundtrip(0, bytesFromInts(0))
    roundtrip(1, bytesFromInts(0x02))
    roundtrip(62, bytesFromInts(0x7C))
    roundtrip(63, bytesFromInts(0x7E))
    byteBufReader( bytesFromInts(0x80, 0x00)).getShort() shouldBe 0
    roundtrip(64, bytesFromInts(0x80, 0x01))
    byteBufReader( bytesFromInts(0x82, 0x00)).getShort() shouldBe 1
    roundtrip(65, bytesFromInts(0x82, 0x01))
    roundtrip(8190, bytesFromInts(0xFC, 0x7F))
    roundtrip(8191, bytesFromInts(0xFE, 0x7F))
    roundtrip(8192, bytesFromInts(0x80, 0x80, 0x01))
    roundtrip(8193, bytesFromInts(0x82, 0x80, 0x01))
    roundtrip(Short.MaxValue, bytesFromInts(0xFE, 0xFF, 0x03))
  }

  property("unsigned Short corner cases") {
    def roundtrip(v: Int, expected: Array[Byte]): Unit = {
      val bytes = byteArrayWriter().putUShort(v).toBytes
      withClue(s"for value $v got bytes ${prettyPrint(bytes)} (expected ${prettyPrint(expected)}): \n") {
        bytes shouldEqual expected
      }
      byteBufReader(expected).getUShort() shouldBe v
    }

    an[IllegalArgumentException] should be thrownBy roundtrip(-2, bytesFromInts(0))
    an[IllegalArgumentException] should be thrownBy roundtrip(-1, bytesFromInts(0))
    roundtrip(0, bytesFromInts(0))
    roundtrip(1, bytesFromInts(1))
    roundtrip(126, bytesFromInts(0x7E))
    roundtrip(127, bytesFromInts(0x7F))
    roundtrip(128, bytesFromInts(0x80, 0x01))
    roundtrip(129, bytesFromInts(0x81, 0x01))
    roundtrip(16382, bytesFromInts(0xFE, 0x7F))
    roundtrip(16383, bytesFromInts(0xFF, 0x7F))
    roundtrip(16384, bytesFromInts(0x80, 0x80, 0x01))
    roundtrip(16385, bytesFromInts(0x81, 0x80, 0x01))
    roundtrip(65534, bytesFromInts(0xFE, 0xFF, 0x03))
    roundtrip(65535, bytesFromInts(0xFF, 0xFF, 0x03))
    an[IllegalArgumentException] should be thrownBy roundtrip(65536, bytesFromInts(0))
    an[IllegalArgumentException] should be thrownBy roundtrip(65537, bytesFromInts(0))
  }

  property("Int corner cases") {
    def roundtrip(v: Int, expected: Array[Byte]): Unit = {
      val bytes = byteArrayWriter().putInt(v).toBytes
      withClue(s"for value $v got bytes ${prettyPrint(bytes)} (expected ${prettyPrint(expected)}): \n") {
        bytes shouldEqual expected
      }
      byteBufReader(expected).getInt() shouldBe v
    }
    roundtrip(Int.MinValue, bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x01)) // 10 bytes
    roundtrip(-1073741825,  bytesFromInts(0x81, 0x80, 0x80, 0x80, 0xF8, 0xFF, 0xFF, 0xFF, 0xFF, 0x01)) // 10 bytes
    roundtrip(-1073741824,  bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0x07)) // 5 bytes
    roundtrip(-134217729,   bytesFromInts(0x81, 0x80, 0x80, 0x80, 0x01)) // 5 bytes
    roundtrip(-134217728,   bytesFromInts(0xFF, 0xFF, 0xFF, 0x7F)) // 4 bytes
    roundtrip(-1048577,     bytesFromInts(0x81, 0x80, 0x80, 0x01)) // 4 bytes
    roundtrip(-1048576,     bytesFromInts(0xFF, 0xFF, 0x7F))
    roundtrip(-8194,        bytesFromInts(0x83, 0x80, 0x01))
    roundtrip(-8193,        bytesFromInts(0x81, 0x80, 0x01))
    roundtrip(-8192,        bytesFromInts(0xFF, 0x7F))
    roundtrip(-8191,        bytesFromInts(0xFD, 0x7F))
    roundtrip(-66,          bytesFromInts(0x83, 0x01))
    roundtrip(-65,          bytesFromInts(0x81, 0x01))
    roundtrip(-64,          bytesFromInts(0x7F))
    roundtrip(-63,          bytesFromInts(0x7D))
    roundtrip(-1,           bytesFromInts(0x01))
    roundtrip(0,            bytesFromInts(0))
    roundtrip(1,            bytesFromInts(0x02))
    roundtrip(62,           bytesFromInts(0x7C))
    roundtrip(63,           bytesFromInts(0x7E))
    roundtrip(64,           bytesFromInts(0x80, 0x01))
    roundtrip(65,           bytesFromInts(0x82, 0x01))
    roundtrip(8190,         bytesFromInts(0xFC, 0x7F))
    roundtrip(8191,         bytesFromInts(0xFE, 0x7F))
    roundtrip(8192,         bytesFromInts(0x80, 0x80, 0x01))
    roundtrip(8193,         bytesFromInts(0x82, 0x80, 0x01))
    roundtrip(1048575,      bytesFromInts(0xFE, 0xFF, 0x7F))
    roundtrip(1048576,      bytesFromInts(0x80, 0x80, 0x80, 0x01)) // 4 bytes
    roundtrip(134217727,    bytesFromInts(0xFE, 0xFF, 0xFF, 0x7F)) // 4 bytes
    roundtrip(134217728,    bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x01)) // 5 bytes
    roundtrip(1073741823,   bytesFromInts(0xFE, 0xFF, 0xFF, 0xFF, 0x07)) // 5 bytes
    roundtrip(1073741824,   bytesFromInts(0x80, 0x80, 0x80, 0x80, 0xF8, 0xFF, 0xFF, 0xFF, 0xFF, 0x01)) // 10 bytes
    roundtrip(Int.MaxValue, bytesFromInts(0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x01)) // 10 bytes
  }

  property("unsigned Int corner cases") {
    def roundtrip(v: Long, expected: Array[Byte]): Unit = {
      val bytes = byteArrayWriter().putUInt(v).toBytes
      withClue(s"for value $v got bytes ${prettyPrint(bytes)} (expected ${prettyPrint(expected)}): \n") {
        bytes shouldEqual expected
      }
      byteBufReader(expected).getUInt() shouldBe v
    }
    an[IllegalArgumentException] should be thrownBy roundtrip(-1, bytesFromInts(0))
    roundtrip(0, bytesFromInts(0))
    roundtrip(126, bytesFromInts(0x7E))
    roundtrip(127, bytesFromInts(0x7F))
    roundtrip(128, bytesFromInts(0x80, 0x01))
    roundtrip(129, bytesFromInts(0x81, 0x01))
    roundtrip(16383, bytesFromInts(0xFF, 0x7F))
    roundtrip(16384, bytesFromInts(0x80, 0x80, 0x01))
    roundtrip(16385, bytesFromInts(0x81, 0x80, 0x01))
    roundtrip(2097151, bytesFromInts(0xFF, 0xFF, 0x7F))
    roundtrip(2097152, bytesFromInts(0x80, 0x80, 0x80, 0x01)) // 4 bytes
    roundtrip(268435455, bytesFromInts(0xFF, 0xFF, 0xFF, 0x7F)) // 4 bytes
    roundtrip(268435456, bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x01)) // 5 bytes
    roundtrip(Int.MaxValue.toLong * 2 + 1, bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0x0F)) // 5 bytes
    an[IllegalArgumentException] should be thrownBy
      roundtrip(Int.MaxValue.toLong * 2 + 2, bytesFromInts(0))
  }

  property("Long corner cases") {
    def roundtrip(v: Long, expected: Array[Byte]): Unit = {
      val bytes = byteArrayWriter().putLong(v).toBytes
      withClue(s"for value $v got bytes ${prettyPrint(bytes)} (expected ${prettyPrint(expected)}): \n") {
        bytes shouldEqual expected
      }
      byteBufReader(expected).getLong() shouldBe v
    }
    roundtrip(Long.MinValue,       bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x01)) // 10 bytes
    roundtrip(Long.MinValue / 2 - 1, bytesFromInts(0x81, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 10 bytes
    roundtrip(Long.MinValue / 2,   bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 9 bytes
    roundtrip(-36028797018963969L, bytesFromInts(0x81, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 9 bytes
    roundtrip(-36028797018963968L, bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 8 bytes
    roundtrip(-281474976710657L,   bytesFromInts(0x81, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 8 bytes
    roundtrip(-281474976710656L,   bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 7 bytes
    roundtrip(-2199023255553L,     bytesFromInts(0x81, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 7 bytes
    roundtrip(-2199023255552L,     bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 6 bytes
    roundtrip(-17179869185L,       bytesFromInts(0x81, 0x80, 0x80, 0x80, 0x80, 0x01)) // 6 bytes
    roundtrip(-17179869184L,       bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 5 bytes
    roundtrip(-134217729,          bytesFromInts(0x81, 0x80, 0x80, 0x80, 0x01)) // 5 bytes
    roundtrip(-134217728,          bytesFromInts(0xFF, 0xFF, 0xFF, 0x7F)) // 4 bytes
    roundtrip(-1048577,            bytesFromInts(0x81, 0x80, 0x80, 0x01)) // 4 bytes
    roundtrip(-1048576,            bytesFromInts(0xFF, 0xFF, 0x7F))
    roundtrip(-8194,               bytesFromInts(0x83, 0x80, 0x01))
    roundtrip(-8193,               bytesFromInts(0x81, 0x80, 0x01))
    roundtrip(-8192,               bytesFromInts(0xFF, 0x7F))
    roundtrip(-8191,               bytesFromInts(0xFD, 0x7F))
    roundtrip(-66,                 bytesFromInts(0x83, 0x01))
    roundtrip(-65,                 bytesFromInts(0x81, 0x01))
    roundtrip(-64,                 bytesFromInts(0x7F))
    roundtrip(-63,                 bytesFromInts(0x7D))
    roundtrip(-1,                  bytesFromInts(0x01))
    roundtrip(0,                   bytesFromInts(0))
    roundtrip(1,                   bytesFromInts(0x02))
    roundtrip(62,                  bytesFromInts(0x7C))
    roundtrip(63,                  bytesFromInts(0x7E))
    roundtrip(64,                  bytesFromInts(0x80, 0x01))
    roundtrip(65,                  bytesFromInts(0x82, 0x01))
    roundtrip(8190,                bytesFromInts(0xFC, 0x7F))
    roundtrip(8191,                bytesFromInts(0xFE, 0x7F))
    roundtrip(8192,                bytesFromInts(0x80, 0x80, 0x01))
    roundtrip(8193,                bytesFromInts(0x82, 0x80, 0x01))
    roundtrip(1048575,             bytesFromInts(0xFE, 0xFF, 0x7F))
    roundtrip(1048576,             bytesFromInts(0x80, 0x80, 0x80, 0x01)) // 4 bytes
    roundtrip(134217727,           bytesFromInts(0xFE, 0xFF, 0xFF, 0x7F)) // 4 bytes
    roundtrip(134217728,           bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x01)) // 5 bytes
    roundtrip(17179869183L,        bytesFromInts(0xFE, 0xFF, 0xFF, 0xFF, 0x7F)) // 5 bytes
    roundtrip(17179869184L,        bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 6 bytes
    roundtrip(2199023255551L,      bytesFromInts(0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 6 bytes
    roundtrip(2199023255552L,      bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 7 bytes
    roundtrip(281474976710655L,    bytesFromInts(0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 7 bytes
    roundtrip(281474976710656L,    bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 8 bytes
    roundtrip(36028797018963967L,  bytesFromInts(0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 8 bytes
    roundtrip(36028797018963968L,  bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 9 bytes
    roundtrip(Long.MaxValue / 2,   bytesFromInts(0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 9 bytes
    roundtrip(Long.MaxValue / 2 + 1, bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 10 bytes
    roundtrip(Long.MaxValue,       bytesFromInts(0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x01)) // 10 bytes
  }

  property("unsigned Long corner cases") {
    def roundtrip(v: Long, expected: Array[Byte]): Unit = {
      val bytes = byteArrayWriter().putULong(v).toBytes
      withClue(s"for value $v got bytes ${prettyPrint(bytes)} (expected ${prettyPrint(expected)}): \n") {
        bytes shouldEqual expected
      }
      byteBufReader(expected).getULong() shouldBe v
    }
    roundtrip(Long.MinValue, bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 10 bytes
    roundtrip(-1, bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x01)) // 10 bytes
    roundtrip(126, bytesFromInts(0x7E))
    roundtrip(127, bytesFromInts(0x7F))
    roundtrip(128, bytesFromInts(0x80, 0x01))
    roundtrip(129, bytesFromInts(0x81, 0x01))
    roundtrip(16383, bytesFromInts(0xFF, 0x7F))
    roundtrip(16384, bytesFromInts(0x80, 0x80, 0x01))
    roundtrip(16385, bytesFromInts(0x81, 0x80, 0x01))
    roundtrip(2097151, bytesFromInts(0xFF, 0xFF, 0x7F))
    roundtrip(2097152,   bytesFromInts(0x80, 0x80, 0x80, 0x01)) // 4 bytes
    roundtrip(268435455, bytesFromInts(0xFF, 0xFF, 0xFF, 0x7F)) // 4 bytes
    roundtrip(268435456,    bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x01)) // 5 bytes
    roundtrip(34359738367L, bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 5 bytes
    roundtrip(34359738368L,   bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 6 bytes
    roundtrip(4398046511103L, bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 6 bytes
    roundtrip(4398046511104L,   bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 7 bytes
    roundtrip(562949953421311L, bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 7 bytes
    roundtrip(562949953421312L,   bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 8 bytes
    roundtrip(72057594037927935L, bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 8 bytes
    roundtrip(72057594037927936L, bytesFromInts(0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01)) // 9 bytes
    roundtrip(Long.MaxValue, bytesFromInts(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F)) // 10 bytes
  }

  private def printHolesInByteArraySpace(): Unit = {
    var v = Short.MinValue
    while (v <= Short.MaxValue) {
      val bytes = BigInt(v.toInt).toByteArray
      try {
        val deserV = byteBufReader(bytes).getShort()
        val roundtripBytes = byteArrayWriter().putShort(deserV).toBytes
        if (!roundtripBytes.sameElements(bytes) && roundtripBytes(0) != bytes(0)) {
          println(s"bytes from deserialized $deserV = ${prettyPrint(roundtripBytes)}, expected ${prettyPrint(bytes)}")
        }
      } catch {
        case _: Throwable =>
      }
      v = (v + 1).toShort
    }
  }

  ignore("find holes in byte array space of VLQ") {
    printHolesInByteArraySpace()
  }

  property("putUShort, putUInt, putULong equivalence") {
    forAll(Arbitrary.arbShort.arbitrary.suchThat(_ >= 0)) { v =>
      val expected = byteArrayWriter().putUShort(v.toInt).toBytes
      byteArrayWriter().putUInt(v.toLong).toBytes shouldEqual expected
      byteArrayWriter().putULong(v.toLong).toBytes shouldEqual expected
    }
  }

  property("putShort, putInt, putLong equivalence") {
    forAll { (v: Short) =>
      val expected = byteArrayWriter().putShort(v).toBytes
      byteArrayWriter().putInt(v.toInt).toBytes shouldEqual expected
      byteArrayWriter().putLong(v.toLong).toBytes shouldEqual expected
    }
  }
}
