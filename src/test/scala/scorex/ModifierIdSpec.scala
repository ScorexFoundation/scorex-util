package scorex

import scorex.util._
import org.scalatest.{FlatSpec, Matchers}

class ModifierIdSpec extends FlatSpec with Matchers {

  "ModifierId" should "convert to/from bytes" in {
    val bytes = Array.fill[Byte](32)(1)
    // explicitly
    idToBytes(bytesToId(bytes)) shouldEqual bytes
    // via extension
    bytes.toModifierId.toBytes shouldEqual bytes
  }

}
