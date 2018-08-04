package scorex.util

import scorex.util.encode.{Base16, BytesEncoder}

/**
  * Trait with bytes to string encoder
  */
trait ScorexEncoding {
  implicit val encoder: BytesEncoder = Base16
}
