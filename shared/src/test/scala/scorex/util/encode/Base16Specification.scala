package scorex.util.encode

class Base16Specification extends BytesEncoderSpecification {
  override val encoder: BytesEncoder = Base16

  property("test vectors") {
     val bytes = Array[Byte](1, 2, 3)
     Base16.encode(bytes) shouldBe "010203"
     Base16.encode(Array.emptyByteArray) shouldBe ""
  }
}
