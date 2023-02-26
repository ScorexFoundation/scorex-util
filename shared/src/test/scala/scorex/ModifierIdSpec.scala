package scorex

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import scorex.util._


class ModifierIdSpec extends AnyFlatSpec with Matchers {

  "ModifierId" should "convert to/from bytes" in {
    val bytes = Array.fill[Byte](32)(1)
    // explicitly
    idToBytes(bytesToId(bytes)) shouldEqual bytes
    // via extension
    bytes.toModifierId.toBytes shouldEqual bytes
  }

}
