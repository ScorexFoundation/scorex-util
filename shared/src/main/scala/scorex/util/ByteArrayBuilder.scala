package scorex.util

import java.nio.ByteBuffer
import java.util

/** Similar to StringBuilder but works with underlying Array[Byte].
  * Borrowed from https://github.com/odnoklassniki/one-nio/blob/master/src/one/nio/util/ByteArrayBuilder.java
  * Modifications of the underlying array is performed via ByteBuffer wrapper, so that saved bytes can
  * be read back via ByteBuffer API. */
class ByteArrayBuilder(initCapacity: Int) {
  protected var arr: Array[Byte] = null
  protected var buf: ByteBuffer = null

  this.arr = new Array[Byte](initCapacity)
  this.buf = ByteBuffer.wrap(this.arr)

  def this() = this(256)

  final def array: Array[Byte] = arr

  final def length: Int = buf.position()

  final def setLength(newPosition: Int): Unit = {
    buf.position(newPosition)
  }

  final def capacity(): Int = arr.length

  final def byteAt(index: Int): Byte = arr(index)

  final def crop(offset: Int): Unit = {
    var count = buf.position()
    if (offset < count) {
      count -= offset
      System.arraycopy(arr, offset, arr, 0, count)
    }
    else {
      count = 0
    }
  }

  final def trim: Array[Byte] = {
    val count = buf.position()
    if (arr.length > count) {
      arr = util.Arrays.copyOf(arr, count)
      buf = ByteBuffer.wrap(arr)
    }
    arr
  }

  final def toBytes: Array[Byte] = {
    val count = buf.position()
    val result = new Array[Byte](count)
    System.arraycopy(arr, 0, result, 0, count)
    result
  }

  final def append(b: Byte): ByteArrayBuilder = {
    ensureCapacity(1)
    buf.put(b)
    this
  }

  final def append(b: Array[Byte]): ByteArrayBuilder =
    append(b, 0, b.length)

  final def append(b: Array[Byte], offset: Int, length: Int): ByteArrayBuilder = {
    ensureCapacity(length)
    buf.put(b, offset, length)
    this
  }

  final def append(b: Boolean): ByteArrayBuilder = {
    append((if (b) 0x01 else 0x00).toByte)
    this
  }

  final def append(c: Char): ByteArrayBuilder = {
    ensureCapacity(1)
    buf.putChar(c)
    this
  }

  final def append(n: Short): ByteArrayBuilder = {
    ensureCapacity(2)
    buf.putShort(n)
    this
  }

  final def append(n: Int): ByteArrayBuilder = {
    ensureCapacity(4)
    buf.putInt(n)
    this
  }

  final def append(n: Long): ByteArrayBuilder = {
    ensureCapacity(8)
    buf.putLong(n)
    this
  }

  private def ensureCapacity(required: Int): Unit = {
    val count = buf.position()
    if (count + required > arr.length) {
      arr = util.Arrays.copyOf(arr, Math.max(count + required, arr.length << 1))
      newBuffer(arr)
    }
  }

  private def newBuffer(newArr: Array[Byte]): Unit = {
    val newBuf = ByteBuffer.wrap(newArr)
    newBuf.position(buf.position)
    buf = newBuf
  }
}