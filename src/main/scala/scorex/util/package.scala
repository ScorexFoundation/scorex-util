package scorex

import scorex.util.encode.Base16
import supertagged.TaggedType

package object util {

  case class ModifierId(hashBytes: Array[Byte]) {
    // This is much more efficient than hashing whole array or String.
    // We can use the first 4 bytes and convert them into Int.
    override def hashCode: Int = {
      val len = math.min(hashBytes.length, 4)
	  var hash = 0
	  for (i <- 0 until len) {
	    hash = (hash << 8) | (hashBytes(i) & 0xFF)
	  }
	  hash
	}
    override def equals(other: Any): Boolean = other.isInstanceOf[ModifierId] && hashBytes.sameElements(other.asInstanceOf[ModifierId].hashBytes)
	override def toString: String = Base16.encode(hashBytes)
  }

  def bytesToId(bytes: Array[Byte]): ModifierId = new ModifierId(bytes)

  def idToBytes(id: ModifierId): Array[Byte] = id.hashBytes

  def stringToId(s: String): ModifierId = new ModifierId(Base16.decode(s).get)

  implicit val modifierOrdering : Ordering[ModifierId] = new Ordering[ModifierId] {
    def compare(a: ModifierId, b: ModifierId): Int = {
      val len = math.min(a.hashBytes.length, b.hashBytes.length)
	  for (i <- 0 until len) {
	    val diff = (a.hashBytes(i) & 0xFF) - (b.hashBytes(i) & 0xFF)
        if (diff != 0) {
		  return diff
		}
      }
	  return a.hashBytes.length - len
	}
  }

  implicit class ModifierIdOps(val m: ModifierId) extends AnyVal {
    @inline def toBytes: Array[Byte] = idToBytes(m)
  }

  implicit class ByteArrayOps(val b: Array[Byte]) extends AnyVal  {
    @inline def toModifierId: ModifierId = bytesToId(b)
  }

  implicit class StringOps(val s: String) extends AnyVal  {
    @inline def toModifierId: ModifierId = stringToId(s)
  }

  object ModifierId {
    def apply(s: String): ModifierId = stringToId(s)
  }
}
