package scorex.util.encode

import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, PropSpec}
import scorex.util.Generators
import scorex.util.encode.ZigZagEncoder._

class ZigZagSpecification extends PropSpec
  with Generators
  with PropertyChecks
  with Matchers {

  property("ZigZag encoding format") {
    // source: http://github.com/google/protobuf/blob/a7252bf42df8f0841cf3a0c85fdbf1a5172adecb/java/core/src/test/java/com/google/protobuf/CodedOutputStreamTest.java#L281
    assert(0 == encodeZigZagInt(0))
    assert(1 == encodeZigZagInt(-1))
    assert(2 == encodeZigZagInt(1))
    assert(3 == encodeZigZagInt(-2))
    assert(0x7FFFFFFE == encodeZigZagInt(0x3FFFFFFF))
    assert(0x7FFFFFFF == encodeZigZagInt(0xC0000000))
    assert(0xFFFFFFFE == encodeZigZagInt(0x7FFFFFFF))
    assert(0xFFFFFFFF == encodeZigZagInt(0x80000000))

    assert(0 == encodeZigZagLong(0))
    assert(1 == encodeZigZagLong(-1))
    assert(2 == encodeZigZagLong(1))
    assert(3 == encodeZigZagLong(-2))
    assert(0x000000007FFFFFFEL == encodeZigZagLong(0x000000003FFFFFFFL))
    assert(0x000000007FFFFFFFL == encodeZigZagLong(0xFFFFFFFFC0000000L))
    assert(0x00000000FFFFFFFEL == encodeZigZagLong(0x000000007FFFFFFFL))
    assert(0x00000000FFFFFFFFL == encodeZigZagLong(0xFFFFFFFF80000000L))
    assert(0xFFFFFFFFFFFFFFFEL == encodeZigZagLong(0x7FFFFFFFFFFFFFFFL))
    assert(0xFFFFFFFFFFFFFFFFL == encodeZigZagLong(0x8000000000000000L))
  }

  property("ZigZag Long round trip") {
    forAll(Gen.chooseNum(Long.MinValue, Long.MaxValue)) { v: Long =>
      decodeZigZagLong(encodeZigZagLong(v)) shouldBe v
    }
  }

  property("ZigZag Int round trip") {
    forAll(Gen.chooseNum(Int.MinValue, Int.MaxValue)) { v: Int =>
      decodeZigZagInt(encodeZigZagInt(v)) shouldBe v
    }
  }

}
