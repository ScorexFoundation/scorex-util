package scorex

import scorex.util.encode.Base16

opaque type ModifierId <: String = String
object ModifierId {
  def apply(s: String): ModifierId = s
}

def bytesToId(bytes: Array[Byte]): ModifierId = ModifierId(Base16.encode(bytes))

def idToBytes(id: ModifierId): Array[Byte] = Base16.decode(id).get

extension (m: ModifierId) {
  @inline def toBytes: Array[Byte] = idToBytes(m)
}

extension (bytes: Array[Byte]) {
  @inline def toModifierId: ModifierId = bytesToId(bytes)
}