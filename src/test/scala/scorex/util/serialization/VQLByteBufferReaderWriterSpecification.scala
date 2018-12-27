package scorex.util.serialization

import java.nio.ByteBuffer

import scorex.util.ByteArrayBuilder

class VQLByteBufferReaderWriterSpecification extends VLQReaderWriterSpecification {

  override def byteBufReader(bytes: Array[Byte]): VLQReader = {
    val buf = ByteBuffer.wrap(bytes)
    buf.position(0)
    new VLQByteBufferReader(buf)
  }

  override def byteArrayWriter(): VLQWriter = {
    new VLQByteBufferWriter(new ByteArrayBuilder())
  }
}
