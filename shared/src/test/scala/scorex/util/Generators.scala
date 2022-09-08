package scorex.util

import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}

import scala.reflect.ClassTag

trait Generators  {

  implicit def arrayGen[T: Arbitrary : ClassTag]: Gen[Array[T]] = for {
    length <- Gen.chooseNum(1, 100)
    bytes <- Gen.listOfN(length, arbitrary[T])
  } yield bytes.toArray
}