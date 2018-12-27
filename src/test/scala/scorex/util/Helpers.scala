package scorex.util

object Helpers {
  /**
    * Helper to construct a byte array from a bunch of bytes. The inputs are actually ints so that I
    * can use hex notation and not get stupid errors about precision.
    */
  def bytesFromInts(bytesAsInts: Int*): Array[Byte] = {
    val a = new Array[Byte](bytesAsInts.length)
    for (i <- a.indices) {
      val v = bytesAsInts(i)
      // values from unsigned byte range will be encoded as negative values which is expected here
      assert(v >= Byte.MinValue && v <= 0xFF, s"$v is out of the signed/unsigned Byte range")
      a(i) = v.toByte
    }
    a
  }
}
