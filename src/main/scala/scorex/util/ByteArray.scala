package scorex.util

import java.util

case class MutbaleByteArray(private var buffer: Array[Byte]) {
  var size = 0

  def append(bytes: Array[Byte]) = {
    if (buffer.length - size < bytes.length) {
      grow(buffer.length + (buffer.length >> 1))
    }
    System.arraycopy(bytes, 0, buffer, size, bytes.length)
  }

  def grow(newCapacity: Int) = {
    buffer = util.Arrays.copyOf(buffer, newCapacity)
  }

  def apply(index: Int) = buffer(index)

}
