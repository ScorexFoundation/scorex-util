package scorex.util.encode

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec

trait BytesEncoderSpecification extends AnyPropSpec
  with ScalaCheckPropertyChecks
  with ScalaCheckDrivenPropertyChecks
  with Matchers {

  val encoder: BytesEncoder

  property("Encoding then decoding preserves data") {
    forAll { data: Array[Byte] =>
      whenever(data.length > 0 && data.head != 0) {
        val encoded = encoder.encode(data)
        encoded.find(c => !encoder.Alphabet.contains(c)) shouldBe None
        val restored = encoder.decode(encoded).get
        restored shouldBe data
      }
    }
  }

  property("Decoding should return failure on incorrect characters") {
    forAll { str: String =>
      whenever(str.exists(c => !encoder.Alphabet.contains(c))) {
        encoder.decode(str).isFailure shouldBe true
      }
    }
  }

}
