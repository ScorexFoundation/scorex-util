package scorex.util.serialization

import scala.util.Try

abstract class Serializer[TFamily, T <: TFamily, R <: Reader, W <: Writer] {

  def serialize(obj: T, w: W): Unit

  def parse(r: R): TFamily

  def parseTry(r: R): Try[TFamily] = {
    Try(parse(r))
  }
}