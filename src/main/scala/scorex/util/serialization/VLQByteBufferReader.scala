package scorex.util.serialization

import java.nio.ByteBuffer
import java.util.BitSet
import scorex.util.Extensions._

/**
  * Not thread safe
  */
class VLQByteBufferReader(buf: ByteBuffer) extends Reader {
  import VLQByteBufferReader._

  type CH = ByteBuffer

  @inline override def getChunk(size: Int): ByteBuffer = {
    ByteBuffer.wrap(getBytes(size))
  }

  @inline override def peekByte(): Byte = buf.array()(buf.position())
  @inline override def getByte(): Byte = buf.get
  @inline override def getUByte(): Int = buf.get & 0xFF
  @inline override def getShort(): Short = buf.getShort()

  /**
    * Decode Short previously encoded with [[VLQByteBufferWriter.putUShort]] using VLQ.
    * @see [[https://en.wikipedia.org/wiki/Variable-length_quantity]]
    * @return Int
    * @throws AssertionError for deserialized values not in unsigned Short range
    */
  @inline override def getUShort(): Int = {
    val x = getULong().toInt
    assert(x >= 0 && x <= 0xFFFF, s"$x is out of unsigned short range")
    x
  }

  /**
    * Decode signed Int previously encoded with [[VLQByteBufferWriter.putInt]] using VLQ with ZigZag.
    *
    * @note Uses ZigZag encoding. Should be used to decode '''only''' a value that was previously
    *       encoded with [[VLQByteBufferWriter.putInt]].
    * @see [[https://en.wikipedia.org/wiki/Variable-length_quantity]]
    * @return signed Int
    */
  @inline override def getInt(): Int =
  // should only be changed simultaneously with `putInt`
    decodeZigZagInt(getULong().toInt)

  /**
    * Decode Int previously encoded with [[VLQByteBufferWriter.putUInt]] using VLQ.
    * @see [[https://en.wikipedia.org/wiki/Variable-length_quantity]]
    * @return Long
    */
  @inline override def getUInt(): Long = {
    val x = getULong()
    assert(x >= 0L && x <= 0xFFFFFFFFL, s"$x is out of unsigned int range")
    x
  }

  /**
    * Decode signed Long previously encoded with [[VLQByteBufferWriter.putLong]] using VLQ with ZigZag.
    *
    * @note Uses ZigZag encoding. Should be used to decode '''only''' a value that was previously
    *       encoded with [[VLQByteBufferWriter.putLong]].
    * @see [[https://en.wikipedia.org/wiki/Variable-length_quantity]]
    * @return signed Long
    */
  @inline override def getLong(): Long = decodeZigZagLong(getULong())

  /**
    * Decode Long previously encoded with [[VLQByteBufferWriter.putULong]] using VLQ.
    * @see [[https://en.wikipedia.org/wiki/Variable-length_quantity]]
    * @return Long
    */
  @inline override def getULong(): Long = {
    // should be fast if java -> scala conversion did not botched it
    // source: http://github.com/google/protobuf/blob/a7252bf42df8f0841cf3a0c85fdbf1a5172adecb/java/core/src/main/java/com/google/protobuf/CodedInputStream.java#L2653
    // for faster version see: http://github.com/google/protobuf/blob/a7252bf42df8f0841cf3a0c85fdbf1a5172adecb/java/core/src/main/java/com/google/protobuf/CodedInputStream.java#L1085
    var result: Long = 0
    var shift = 0
    while (shift < 64) {
      val b = buf.get()
      result = result | ((b & 0x7F).toLong << shift)
      if ((b & 0x80) == 0) return result
      shift += 7
    }
    sys.error(s"Cannot deserialize Long value. Unexpected buffer $buf with bytes remaining ${buf.getBytes(buf.remaining)}")
    // see https://rosettacode.org/wiki/Variable-length_quantity for implementations in other languages
  }

  @inline override def getBytes(size: Int): Array[Byte] = buf.getBytes(size)

  @inline override def getBits(size: Int): Array[Boolean] = {
    if (size == 0) return Array[Boolean]()
    val bitSet = BitSet.valueOf(buf.getBytes((size + 7) / 8))
    val boolArray = new Array[Boolean](size)
    var i = 0
    while (i < size) {
      boolArray(i) = bitSet.get(i)
      i += 1
    }
    boolArray
  }

  @inline override def getOption[T](getValue: => T): Option[T] = buf.getOption(getValue)

  private var _mark: Int = _
  @inline override def mark(): this.type = {
    _mark = buf.position()
    this
  }
  @inline override def consumed: Int = buf.position() - _mark

  @inline override def position: Int = buf.position()

  @inline override def position_=(p: Int): Unit = buf.position(p)

  @inline override def remaining: Int = buf.remaining()

  private var lvl: Int = 0
  @inline override def level: Int = lvl
  @inline override def level_=(v: Int): Unit = lvl = v
}

object VLQByteBufferReader {

  /**
    * Decode a signed value previously ZigZag-encoded with [[VLQByteBufferWriter.encodeZigZagInt]]
    *
    * @see [[https://developers.google.com/protocol-buffers/docs/encoding#types]]
    * @param n unsigned Int previously encoded with [[VLQByteBufferWriter.encodeZigZagInt]]
    * @return signed Int
    */
  def decodeZigZagInt(n: Int): Int =
  // source: http://github.com/google/protobuf/blob/a7252bf42df8f0841cf3a0c85fdbf1a5172adecb/java/core/src/main/java/com/google/protobuf/CodedInputStream.java#L553
    (n >>> 1) ^ -(n & 1)

  /**
    * Decode a signed value previously ZigZag-encoded with [[VLQByteBufferWriter.encodeZigZagLong]]
    *
    * @see [[https://developers.google.com/protocol-buffers/docs/encoding#types]]
    * @param n unsigned Long previously encoded with [[VLQByteBufferWriter.encodeZigZagLong]]
    * @return signed Long
    */
  def decodeZigZagLong(n: Long): Long =
  // source: http://github.com/google/protobuf/blob/a7252bf42df8f0841cf3a0c85fdbf1a5172adecb/java/core/src/main/java/com/google/protobuf/CodedInputStream.java#L566
    (n >>> 1) ^ -(n & 1)
}