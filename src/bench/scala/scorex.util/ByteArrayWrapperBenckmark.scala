package scorex.util

import java.util

import akka.util.ByteString
import org.scalameter.Bench.OfflineReport
import org.scalameter.api._

import scala.collection.mutable
import scala.reflect.ClassTag

object ByteArrayWrapperBenckmark extends OfflineReport {

  val numOfCpu = 1//Runtime.getRuntime().availableProcessors()

  val sizes = Gen.range("size")(1000000, 5000000, 2000000)
  val parallelismLevels = Gen.range("parallelismLevel")(1, numOfCpu, 1)
  val numOfIterations = 10000000

  override def defaultConfig = new Context(Map(
    exec.benchRuns -> 3,
    exec.independentSamples -> 3,
  ))

  performance of "ByteArrayWrapper" in {
    val arrs = arrays(createByteArrayWrapper).cached
    measure method "equals" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          equalsTest(arr)((a, b) => a.equals(b))
        }
      }
    }
    measure method "hashCode" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          hashCodeTest(arr)(_.hashCode)
        }
      }
    }
    measure method "allocation" in {
      using(sizeParTuples) in { case (size,p) =>
        par(p) {
          allocTest(size)(createByteArrayWrapper)
        }
      }
    }
    measure method "allocationAndUpdate" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          allocAndUpdateTest(arr)(createByteArrayWrapper)
        }
      }
    }
  }

  performance of "Array[Byte]" in {
    val arrs = arrays(createByteArray).cached

    measure method "equals" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          equalsTest(arr) { (a,b) => java.util.Arrays.equals(a,b)}
        }
      }
    }
    measure method "hashCode" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          hashCodeTest(arr)(util.Arrays.hashCode)
        }
      }
    }
    measure method "allocation" in {
      using(sizeParTuples) in { case (size, p) =>
        par(p) {
          allocTest(size)(createByteArray)
        }
      }
    }
    measure method "allocationAndUpdate" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          allocAndUpdateTest(arr)(createByteArray)
        }
      }
    }
  }

  performance of "WrappedArray" in {
    val arrs = arrays(createWrappedArray).cached
    measure method "equals" in {
      using(arrs) in { case (arr, p) =>
        equalsTest(arr) { (a,b) => a.equals(b)}
      }
    }
    measure method "hashCode" in {
      using(arrs) in { case (arr, p) =>
        hashCodeTest(arr) (_.hashCode)
      }
    }
    measure method "allocation" in {
      using(sizeParTuples) in { case (size, p) =>
        par(p) {
          allocTest(size)(createWrappedArray)
        }
      }
    }
    measure method "allocationAndUpdate" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          allocAndUpdateTest(arr)(createWrappedArray)
        }
      }
    }
  }

  performance of "ByteString" in {
    val arrs = arrays(createByteString).cached
    measure method "equals" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          equalsTest(arr) { (a, b) => a.equals(b) }
        }
      }
    }
    measure method "hashCode" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          hashCodeTest(arr)(_.hashCode)
        }
      }
    }
    measure method "allocation" in {
      using(sizeParTuples) in { case (size, p) =>
        par(p) {
          allocTest(size)(createByteString)
        }
      }
    }
    measure method "allocationAndUpdate" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          allocAndUpdateTest(arr)(createByteString)
        }
      }
    }
  }

  performance of "String" in {
    val arrs = arrays(createString).cached
    measure method "equals" in {
      using(arrs) in { case (arr, p) =>
        equalsTest(arr) { (a,b) => a.equals(b)}
      }
    }
    measure method "hashCode" in {
      using(arrs) in { case (arr, p) =>
        hashCodeTest(arr) (_.hashCode)
      }
    }
    measure method "allocation" in {
      using(sizeParTuples) in { case (size, p) =>
        par(p) {
          allocTest(size)(createString)
        }
      }
    }
    measure method "allocationAndUpdate" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          allocAndUpdateTest(arr)(createString)
        }
      }
    }
  }

  def createByteArrayWrapper(i: Int) = {
    val data = intToByteArray(i)
    ByteArrayWrapper(data)
  }

  def createByteArray(i: Int) = {
    intToByteArray(i)
  }

  def createWrappedArray(i: Int) = {
    val data= intToByteArray(i)
    mutable.WrappedArray.make[Byte](data)
  }

  def createString(i: Int) = {
    new String(intToByteArray(i))
  }

  def createByteString(i: Int) = {
    val data= intToByteArray(i)
    ByteString(data)
  }

  def intToByteArray(value: Int): Array[Byte] = Array[Byte](
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    (value >>> 24).toByte,
    (value >>> 16).toByte,
    (value >>> 8).toByte,
     value.toByte
  )

  def arrays[T: ClassTag](instance: (Int) => T) = {
    val arrays = for {
      size <- sizes
    } yield {
      val arr = Array.ofDim[T](size)

      (0 until arr.length).foreach { i =>
        arr(i) = instance(i)
      }
      arr
    }

    for {
      arr <- arrays
      par <- parallelismLevels
    } yield {
      (arr, par)
    }
  }

  def sizeParTuples = {
    for {
      size <- sizes
      par <- parallelismLevels
    } yield {
      (size, par)
    }
  }

  def par(parallelizm: Int)(code: => Any){
    code
//    implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numOfCpu))
//
//    val futures = (0 until parallelizm).map { _ =>
//      Future {
//        code
//      }
//    }
//    Await.result(Future.sequence(futures), Duration.Inf)
  }

  def equalsTest[T](arr: Array[T])(eq: (T,T) => Boolean) = {
    var r = true
    var it = 0
    while (it < numOfIterations) {
      val i = it % (arr.length - 1) //util.Random.nextInt(arr.length)
      val j = arr.length - i - 1 //util.Random.nextInt(arr.length)
      r = eq(arr(i),arr(j))
      it += 1
    }
    r
  }

  def hashCodeTest[T](arr: Array[T])(hashCode: (T) => Int) = {
    var r = 0
    var it = 0
    while (it < numOfIterations) {
      val i = it % (arr.length - 1) //util.Random.nextInt(arr.length)
      r = hashCode(arr(i))
      it += 1
    }
    r
  }

  def allocTest[T](size:Int)(instance: (Int) => T) = {
    var r:T = instance(0)
    var it = 1
    while (it < size) {
      r = instance(it)
      it += 1
    }
    r
  }

  def allocAndUpdateTest[T](arr: Array[T])(instance: (Int) => T) = {
    var it = 0
    while (it < numOfIterations) {
      val i = it % (arr.length - 1) //util.Random.nextInt(arr.length)
      arr(i) = instance(i)
      it += 1
    }
  }
}
