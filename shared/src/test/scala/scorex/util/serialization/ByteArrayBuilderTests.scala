package scorex.util.serialization


import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import scorex.util.ByteArrayBuilder

class ByteArrayBuilderTests extends AnyPropSpec with ScalaCheckPropertyChecks with Matchers {

  property("Append basic types") {
    val b = new ByteArrayBuilder(1)

    b.append(1.toByte)  // Byte
    b.toBytes shouldBe(Array[Byte](1))
    b.capacity() shouldBe 1

    b.append(1) // Int
    b.toBytes shouldBe(Array[Byte](1, 0, 0, 0, 1))
    b.capacity() shouldBe 5

    b.append(1L << 32) // Long
    b.toBytes shouldBe(Array[Byte](1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0))
    b.capacity() shouldBe 13

    b.append(Array[Byte](10, 20)) // Long
    b.toBytes shouldBe(Array[Byte](1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 10, 20))
    b.capacity() shouldBe 26

  }

}
