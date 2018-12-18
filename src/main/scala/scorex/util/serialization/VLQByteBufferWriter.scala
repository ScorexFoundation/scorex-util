package scorex.util.serialization

import scorex.util.ByteArrayBuilder
import scorex.util.serialization.Writer.Aux

/**
  * Not thread safe
  */
class VLQByteBufferWriter(b: ByteArrayBuilder) extends Writer with VLQWriter {
  override type CH = ByteArrayBuilder

  @inline override def newWriter(): Aux[ByteArrayBuilder] = {
    new VLQByteBufferWriter(new ByteArrayBuilder())
  }

  @inline override def putChunk(chunk: ByteArrayBuilder): this.type = {
    b.append(chunk.toBytes)
    this
  }

  @inline override def put(x: Byte): this.type = {
    b.append(x)
    this
  }

  @inline override def putBoolean(x: Boolean): this.type = {
    b.append(x)
    this
  }

  @inline override def putShort(x: Short): this.type = {
    b.append(x)
    this
  }

  @inline override def putBytes(xs: Array[Byte]): this.type = {
    b.append(xs)
    this
  }

  override def length(): Int = b.length()

  override def result(): ByteArrayBuilder = b

  @inline def toBytes: Array[Byte] = {
    b.toBytes
  }
}

