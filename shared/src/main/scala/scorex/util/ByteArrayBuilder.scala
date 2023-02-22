package scorex.util

import java.nio.ByteBuffer
import java.util

/** Similar to StringBuilder but works with underlying Array[Byte].
  * Borrowed from https://github.com/odnoklassniki/one-nio/blob/master/src/one/nio/util/ByteArrayBuilder.java
  * Modifications of the underlying array is performed via ByteBuffer wrapper, so that saved bytes can
  * be read back via ByteBuffer API.
  * @param initCapacity initial capacity of the underlying array
  */
class ByteArrayBuilder(initCapacity: Int) {
  private var arr: Array[Byte] = null
  private var buf: ByteBuffer = null

  this.arr = new Array[Byte](initCapacity)
  this.buf = ByteBuffer.wrap(this.arr)

  def this() = this(256)

  /** Returns the underlying array. */
  final def array: Array[Byte] = arr

  /** The length of the written part of the buffer (from 0 to the current position) */
  final def length: Int = buf.position()

  /** Sets the new position of the buffer. */
  final def setLength(newPosition: Int): Unit = {
    buf.position(newPosition)
  }

  /** Returns the length of the underlying array (buffer capacity). */
  final def capacity(): Int = arr.length

  /** Returns a byte at the given index in the buffer. */
  final def byteAt(index: Int): Byte = arr(index)

  /** Trims the underlying array to the current position. */
  final def trim: Array[Byte] = {
    val count = buf.position()
    if (arr.length > count) {
      arr = util.Arrays.copyOf(arr, count)
      buf = ByteBuffer.wrap(arr)
    }
    arr
  }

  /** Retruns a new array with all the written bytes. */
  final def toBytes: Array[Byte] = {
    val count = buf.position()
    val result = new Array[Byte](count)
    System.arraycopy(arr, 0, result, 0, count)
    result
  }

  /** Appends a byte to the buffer.
    * @return this builder
    */
  final def append(b: Byte): ByteArrayBuilder = {
    ensureCapacity(1)
    buf.put(b)
    this
  }

  /** Appends a byte array to the buffer.
    * @return this builder
    */
  final def append(b: Array[Byte]): ByteArrayBuilder =
    append(b, 0, b.length)

  /** Appends a part of a byte array to the buffer.
    * @param b the byte array to append
    * @param offset the offset of the first byte to append
    * @param length the number of bytes to append
    * @return this builder
    */
  final def append(b: Array[Byte], offset: Int, length: Int): ByteArrayBuilder = {
    ensureCapacity(length)
    buf.put(b, offset, length)
    this
  }

  /** Appends a boolean to the buffer (true is encoded in the buffer as 0x01, false as 0x00).
    * @param b the boolean to append
    * @return this builder
    */
  final def append(b: Boolean): ByteArrayBuilder = {
    append((if (b) 0x01 else 0x00).toByte)
    this
  }

  /** Appends a char to the buffer.
    * @param c the char to append
    * @return this builder
    */
  final def append(c: Char): ByteArrayBuilder = {
    ensureCapacity(1)
    buf.putChar(c)
    this
  }

  /** Appends a short to the buffer.
    * @param n the short to append
    * @return this builder
    */
  final def append(n: Short): ByteArrayBuilder = {
    ensureCapacity(2)
    buf.putShort(n)
    this
  }

  /** Appends an Int to the buffer.
    * @param n the Int to append
    * @return this builder
    */
  final def append(n: Int): ByteArrayBuilder = {
    ensureCapacity(4)
    buf.putInt(n)
    this
  }

  /** Appends a Long to the buffer.
    * @param n the Long to append
    * @return this builder
    */
  final def append(n: Long): ByteArrayBuilder = {
    ensureCapacity(8)
    buf.putLong(n)
    this
  }

  /** Ensures that the underlying array has enough capacity to append the given number of bytes. */
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