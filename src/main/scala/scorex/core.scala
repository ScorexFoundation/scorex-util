package scorex

import supertagged.TaggedType
import scorex.crypto.encode.Base16

object core {

  object ModifierId extends TaggedType[String]
  type ModifierId = ModifierId.Type

  def bytesToId(bytes: Array[Byte]): ModifierId = ModifierId @@ Base16.encode(bytes)

  def idToBytes(id: ModifierId): Array[Byte] = Base16.decode(id).get

  implicit class ModifierIdOps(m: ModifierId) {
    @inline def toBytes: Array[Byte] = idToBytes(m)
  }

  implicit class ByteArrayOps(b: Array[Byte]) {
    @inline def toModifierId: ModifierId = bytesToId(b)
  }
}
