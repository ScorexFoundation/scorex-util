package scorex

import scorex.util.encode.Base16

package object util {

  /** Represents hash based id of a modifier (see [Content-addressable
    * storage](https://en.wikipedia.org/wiki/Content-addressable_storage)). `ModifierId` is used extensively
    * all over the code base.
    * In most cases `ModifierId` is used as equality safe replacement of the original `Array[Byte]` 32-bytes
    * hash which is stored in blockchain.
    *
    * The reason for this is that the default implementation of `hashCode` and `equals` in `Array` class
    * doesn't allow to use arrays in `Map` (as keys) and in `Set` collections. Other methods like `distinct`
    * also become broken.
    *
    * This class avoids the above mentioned problems and in addition outperforms even Array[Byte]
    * while guaranteeing the correctness of equality sensitive operations with collections.
    * The idea is to exploit the fact that ModifierId is backed by cryptographic hash, we know this for sure,
    * so it is not general Array[Byte].
    *
    * The implementation of `hashCode()` below is much more efficient than hashing the whole 32 bytes of
    * `hashBytes` array and actually provide better `hashCode` randomness (since the hash function if
    * cryptographic), which will further improve performance of `Map` and `Set` operations.
    *
    * @param  hashBytes  cryptographic hash
    */
  case class ModifierId(hashBytes: Array[Byte]) {
    // This is much more efficient than hashing whole array or String.
    // We can use the first 4 bytes and convert them into Int.
    override def hashCode: Int = {
      val bytes = hashBytes
      hashFromBytes(bytes(0), bytes(1), bytes(2), bytes(3))
    }

    override def equals(other: Any): Boolean = (this eq other.asInstanceOf[AnyRef]) ||
      (other match {
        case other: ModifierId => java.util.Arrays.equals(hashBytes, other.hashBytes)
        case _ => false
      })

	  override def toString: String = Base16.encode(hashBytes)
  }

  @inline final def hashFromBytes(b1: Byte, b2: Byte, b3: Byte, b4: Byte): Int =  {
    b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF)
  }

  def bytesToId(bytes: Array[Byte]): ModifierId = new ModifierId(bytes)

  def idToBytes(id: ModifierId): Array[Byte] = id.hashBytes

  def stringToId(s: String): ModifierId = new ModifierId(Base16.decode(s).get)

  implicit val modifierOrdering : Ordering[ModifierId] = new Ordering[ModifierId] {
    def compare(a: ModifierId, b: ModifierId): Int = {
      java.util.Arrays.compare(a.hashBytes, b.hashBytes)
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
