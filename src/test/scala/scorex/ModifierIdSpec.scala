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

  "ModifierId" should "equals or not equal if and only if the corresponding Base16 strings are equal" in {
    val str1 = "0001020304050607080910111213141516171819F0F1F2F3F4F5F6F7F8F900FF"
    val str2 = "0001020304050607080910111213141516171819F0F1F2F3F4F5F6F7F8F9007F"
	ModifierId(str1) shouldEqual ModifierId(str1)
	ModifierId(str1) should not equal ModifierId(str2)
  }

  "ModifierId" should "provide the same ordering as Base16 strings" in {
    val strs = Array("0001020304050607080910111213141516171819F0F1F2F3F4F5F6F7F8F900FF",
   		             "0101020304050607080910111213141516171819F0F1F2F3F4F5F6F7F8F900FF",
					 "FF01020304050607080910111213141516171819F0F1F2F3F4F5F6F7F8F900FF",
					 "0001020304050607080910111213141516171819F0F1F2F3F4F5F6F7F8F9007F",
					 "0001020304050607080910111213141516171819F0F1F2F3F4F5F6F7F8F901FF")
	for (i <- 0 until strs.size)
	  for (j <- 0 until strs.size)
	   	math.signum(modifierOrdering.compare(ModifierId(strs(i)), ModifierId(strs(j)))) shouldEqual math.signum(strs(i).compare(strs(j)))
  }

}
