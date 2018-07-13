package scorex.util

import java.util

import akka.util.ByteString
import org.scalameter.Bench.OfflineReport
import org.scalameter.api._

import scala.collection.mutable
import scala.reflect.ClassTag

object ByteArrayWrapperBenckmark extends OfflineReport {

  val numOfCpu = 1//Runtime.getRuntime().availableProcessors()

  val sizes = Gen.range("size")(1000000, 5000000, 1000000)
  val parallelismLevels = Gen.range("parallelismLevel")(1, numOfCpu, 1)
  val numOfIterations = 10000000

  override def defaultConfig = new Context(Map(
    exec.benchRuns -> 10,
    exec.independentSamples -> 3,
    exec.jvmflags -> List("-Xmx4096m")
  ))

  performance of "ByteArrayWrapper" in {
    val arrs = arrays(createByteArrayWrapper)
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
          allocAndUpdateTest(arr, createByteArrayWrapper)((a, b) => a.equals(b))
        }
      }
    }
  }

  performance of "Array[Byte]" in {
    val arrs = arrays(createByteArray)

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
          allocAndUpdateTest(arr, createByteArray){ (a,b) => java.util.Arrays.equals(a,b)}
        }
      }
    }
  }

  performance of "WrappedArray" in {
    val arrs = arrays(createWrappedArray)
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
          allocAndUpdateTest(arr, createWrappedArray){ (a,b) => a.equals(b)}
        }
      }
    }
  }

//  performance of "ByteString" in {
//    val arrs = arrays(createByteString)
//    measure method "equals" in {
//      using(arrs) in { case (arr, p) =>
//        par(p) {
//          equalsTest(arr) { (a, b) => a.equals(b) }
//        }
//      }
//    }
//    measure method "hashCode" in {
//      using(arrs) in { case (arr, p) =>
//        par(p) {
//          hashCodeTest(arr)(_.hashCode)
//        }
//      }
//    }
//    measure method "allocation" in {
//      using(sizeParTuples) in { case (size, p) =>
//        par(p) {
//          allocTest(size)(createByteString)
//        }
//      }
//    }
//    measure method "allocationAndUpdate" in {
//      using(arrs) in { case (arr, p) =>
//        par(p) {
//          allocAndUpdateTest(arr, createByteString){ (a, b) => a.equals(b) }
//        }
//      }
//    }
//  }

  performance of "StringBase16bouncycastle" in {
    val arrs = arrays(createStringBase16bouncycastle)
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
          allocTest(size)(createStringBase16bouncycastle)
        }
      }
    }
    measure method "allocationAndUpdate" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          allocAndUpdateTest(arr, createStringBase16bouncycastle){ (a,b) => a.equals(b)}
        }
      }
    }
  }

  performance of "StringFastBase16" in {
    val arrs = arrays(createStringBase16)
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
          allocTest(size)(createStringBase16)
        }
      }
    }
    measure method "allocationAndUpdate" in {
      using(arrs) in { case (arr, p) =>
        par(p) {
          allocAndUpdateTest(arr, createStringBase16){ (a,b) => a.equals(b)}
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
          allocAndUpdateTest(arr, createString){ (a,b) => a.equals(b)}
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

  def createStringBase16(i: Int) = {
    bytesToHex(intToByteArray(i))
  }

  def createStringBase16bouncycastle(i: Int) = {
    Base16.encode(intToByteArray(i))
  }

  def createString(i: Int) = {
    new String(intToByteArray(i))
  }

  def createByteString(i: Int) = {
    val data= intToByteArray(i)
    ByteString(data)
  }

  def intToByteArray(value: Int): Array[Byte] = {
    val a = Array.ofDim[Byte](32)
    a(0) = value.toByte
    a(1) = (value >>> 8).toByte
    a(2) = (value >>> 16).toByte
    a(3) = (value >>> 24).toByte
    a
  }

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

  def allocAndUpdateTest[T](arr: Array[T], instance: (Int) => T)(eq: (T,T) => Boolean) = {
    var it = 0
    while (it < numOfIterations) {
      val i = scala.util.Random.nextInt(arr.length) //it % (arr.length - 1) //
      val j = scala.util.Random.nextInt(arr.length) //it % (arr.length - 1) //
      val newInstance = instance(it)
      val oldInstance = arr(i)
      arr(j) = if (eq(newInstance, oldInstance)) oldInstance else newInstance
      it += 1
    }
  }


  private val hexArray = "0123456789ABCDEF".toCharArray

  def bytesToHex(bytes: Array[Byte]): String = {
    val buf = new Array[Char](bytes.length * 2)
    var j = 0
    while (j < bytes.length) {
      val v = bytes(j) & 0xFF
      buf(j * 2) = hexArray(v >>> 4)
      buf(j * 2 + 1)= hexArray(v & 0x0F)
      j += 1
    }
    new String(buf)
  }
}
