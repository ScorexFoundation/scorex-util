package scorex.util

import java.util.Comparator

object Utils {

  def byteArrayHashCode(data: Array[Byte]): Int = { //do not use Arrays.hashCode, it generates too many collisions (31 is too low)
    var h = 1
    for (b <- data) {
      h = h * (-1640531527) + b
    }
    h
  }


  /**
    * Compares primitive Byte Arrays.
    * It uses unsigned binary comparation; so the byte with negative value is always higher than byte with non-negative value.
    */
  val BYTE_ARRAY_COMPARATOR: Comparator[Array[Byte]] = (o1: Array[Byte], o2: Array[Byte]) => compare(o1, o2)

  def compare(o1: Array[Byte], o2: Array[Byte]): Int = { //            if (o1 == o2) return 0;
    val len = Math.min(o1.length, o2.length)
    var i = 0
    while ( {
      i < len
    }) {
      val b1 = o1(i) & 0xFF
      val b2 = o2(i) & 0xFF
      if (b1 != b2) return b1 - b2

      {
        i += 1; i - 1
      }
    }
    o1.length - o2.length
  }
}
