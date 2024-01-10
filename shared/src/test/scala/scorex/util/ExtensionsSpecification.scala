package scorex.util

import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import Extensions._
import org.scalacheck.Gen
import org.scalatest.propspec.AnyPropSpec

class ExtensionsSpecification extends AnyPropSpec
  with ScalaCheckDrivenPropertyChecks
  with Matchers {

  property("ByteOps.toUByte") {
    forAll { (b: Byte) =>
      val expected = if (b >= 0) b.toInt else 255 + b + 1
      b.toUByte shouldBe expected
    }
  }

  property("ShortOps.toByteExact") {
    forAll(Gen.chooseNum(Byte.MaxValue.toShort, Byte.MaxValue.toShort)) { (x: Short) =>
      x.toByteExact shouldBe x.toByte
    }

    forAll(Gen.chooseNum(Short.MinValue, (Byte.MinValue - 1).toShort)) { (x: Short) =>
      an[ArithmeticException] should be thrownBy x.toByteExact
    }

    forAll(Gen.chooseNum((Byte.MaxValue + 1).toShort, Short.MaxValue)) { (x: Short) =>
      an[ArithmeticException] should be thrownBy x.toByteExact
    }

  }

  property("IntOps.toByteExact") {
    forAll(Gen.chooseNum(Byte.MaxValue.toInt, Byte.MaxValue.toInt)) { (x: Int) =>
      x.toByteExact shouldBe x.toByte
    }

    forAll(Gen.chooseNum(Int.MinValue, Byte.MinValue - 1)) { (x: Int) =>
      an[ArithmeticException] should be thrownBy x.toByteExact
    }

    forAll(Gen.chooseNum(Byte.MaxValue + 1, Int.MaxValue)) { (x: Int) =>
      an[ArithmeticException] should be thrownBy x.toByteExact
    }
  }

  property("IntOps.toShortExact") {
    forAll(Gen.chooseNum(Short.MaxValue.toInt, Short.MaxValue.toInt)) { (x: Int) =>
      x.toShortExact shouldBe x.toShort
    }

    forAll(Gen.chooseNum(Int.MinValue, Short.MinValue - 1)) { (x: Int) =>
      an[ArithmeticException] should be thrownBy x.toShortExact
    }

    forAll(Gen.chooseNum(Short.MaxValue + 1, Int.MaxValue)) { (x: Int) =>
      an[ArithmeticException] should be thrownBy x.toShortExact
    }
  }

  property("LongOps.toByteExact") {
    forAll(Gen.chooseNum(Byte.MaxValue.toLong, Byte.MaxValue.toLong)) { (x: Long) =>
      x.toByteExact shouldBe x.toByte
    }

    forAll(Gen.chooseNum(Long.MinValue, (Byte.MinValue - 1).toLong)) { (x: Long) =>
      an[ArithmeticException] should be thrownBy x.toByteExact
    }

    forAll(Gen.chooseNum((Byte.MaxValue + 1).toLong, Long.MaxValue)) { (x: Long) =>
      an[ArithmeticException] should be thrownBy x.toByteExact
    }
  }

  property("LongOps.toShortExact") {
    forAll(Gen.chooseNum(Short.MaxValue.toLong, Short.MaxValue.toLong)) { (x: Long) =>
      x.toShortExact shouldBe x.toShort
    }

    forAll(Gen.chooseNum(Long.MinValue, (Short.MinValue - 1).toLong)) { (x: Long) =>
      an[ArithmeticException] should be thrownBy x.toShortExact
    }

    forAll(Gen.chooseNum((Short.MaxValue + 1).toLong, Long.MaxValue)) { (x: Long) =>
      an[ArithmeticException] should be thrownBy x.toShortExact
    }
  }

  property("LongOps.toIntExact") {
    forAll(Gen.chooseNum(Int.MaxValue.toLong, Int.MaxValue.toLong)) { (x: Long) =>
      x.toIntExact shouldBe x.toInt
    }

    forAll(Gen.chooseNum(Long.MinValue, Int.MinValue.toLong - 1)) { (x: Long) =>
      an[ArithmeticException] should be thrownBy x.toIntExact
    }

    forAll(Gen.chooseNum(Int.MaxValue.toLong + 1, Long.MaxValue)) { (x: Long) =>
      an[ArithmeticException] should be thrownBy x.toIntExact
    }
  }

  property("TraversableOps.cast") {
    List(1,2,3,4).cast[Int] shouldBe List(1,2,3,4)
    an[IllegalArgumentException] should be thrownBy List(1,"2",3,4).cast[Int]
  }

}
