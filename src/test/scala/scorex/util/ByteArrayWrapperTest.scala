package scorex.util

import java.util.concurrent.Executors

import akka.util.ByteString
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class ByteArrayWrapperTest extends FlatSpec with Matchers with ScorexLogging {

  val N = 5000000
  val K = 300000000
  val M = 10

  val numOfCpu = Runtime.getRuntime().availableProcessors()
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numOfCpu))

  "TEst" should "run benchmark for ByteArrayWrapper" in {
    println("ByteArrayWrapper benchmark")
    val futures = (0 until numOfCpu)
      .map{ core =>
        val arr = Array.ofDim[ByteArrayWrapper](N)

        (0 until arr.length).foreach { i =>
          val data = Array.ofDim[Byte](32)
//          util.Random.nextBytes(data)
          arr(i) = ByteArrayWrapper(data)
        }
        (core, arr)
      }
      .map{ case (core, arr) =>
        Future {
          println(s"Start for CPU $core")
          (1 to M).foreach { m =>
            var it = 0
            val time = System.currentTimeMillis()
            var b = 0d
            while (it < K) {
              val i = it % (N -1) //util.Random.nextInt(arr.length)
              val j = N - i - 1 //util.Random.nextInt(arr.length)
              arr(i).equals(arr(j))
              it += 1
              b += i + j + m
            }
            println(s"$b Iteration $m on cpu $core time for ByteArrayWrapper: " + (System.currentTimeMillis() - time))
          }
        }
      }

    Await.result(Future.sequence(futures), Duration.Inf)

  }

  it should "run benchmark for Array[Byte]" in {

    println("Array[Byte] benckmark")
    val futures = (0 until numOfCpu)
      .map { core =>

        val arr = Array.ofDim[Array[Byte]](N)

        (0 until arr.length).foreach { i =>
          val data = Array.ofDim[Byte](32)
//          util.Random.nextBytes(data)
          arr(i) = data
        }

        (core, arr)
      }
      .map { case (core, arr) =>
        Future {

          (1 to M).foreach { m =>
            var it = 0
            val time = System.currentTimeMillis()
            var b = 0d

            while (it < K) {
              val i = it % (N -1) //util.Random.nextInt(arr.length)
              val j = N - i - 1 //util.Random.nextInt(arr.length)
              java.util.Arrays.equals(arr(i), arr(j))
              it += 1
              b += i + j + m

            }

            println(s"$b Iteration $m on cpu $core time for Array[Byte]: " + (System.currentTimeMillis() - time))
          }
        }
      }

    Await.result(Future.sequence(futures), Duration.Inf)

  }

  it should "run benchmark for WrappedArray" in {

    println("WrappedArray[Byte] benckmark")
    val futures = (0 until numOfCpu)
      .map { core =>

        val arr = Array.ofDim[mutable.WrappedArray[Byte]](N)

        (0 until arr.length).foreach { i =>
          val data = Array.ofDim[Byte](32)
//          util.Random.nextBytes(data)
          val a = mutable.WrappedArray.make[Byte](data)
          arr(i) = a
        }

        (core, arr)
      }
      .map { case (core, arr) =>
        Future {

          (1 to M).foreach { m =>
            var it = 0
            val time = System.currentTimeMillis()
            var b = 0d

            while (it < K) {
              val i = it % (N -1) //util.Random.nextInt(arr.length)
              val j = N - i - 1 //util.Random.nextInt(arr.length)
              arr(i).equals(arr(j))
              it += 1
              b += i + j + m

            }

            println(s"$b Iteration $m on cpu $core time for WrappedArray[Byte]: " + (System.currentTimeMillis() - time))
          }
        }
      }

    Await.result(Future.sequence(futures), Duration.Inf)

  }


  it should "run benchmark for ByteString" in {

    println("ByteString benckmark")
    val futures = (0 until numOfCpu)
      .map { core =>

        val arr = Array.ofDim[ByteString](N)

        (0 until arr.length).foreach { i =>
          val data = Array.ofDim[Byte](32)
//          util.Random.nextBytes(data)
          arr(i) = ByteString(data)
        }

        (core, arr)
      }
      .map { case (core, arr) =>
        Future {

          (1 to M).foreach { m =>
            var it = 0
            val time = System.currentTimeMillis()
            var b = 0d

            while (it < K) {
              val i = it % (N -1) //util.Random.nextInt(arr.length)
              val j = N - i - 1 //util.Random.nextInt(arr.length)
              arr(i).equals(arr(j))
              it += 1
              b += i + j + m

            }

            println(s"$b Iteration $m on cpu $core time for ByteString: " + (System.currentTimeMillis() - time))
          }
        }
      }

    Await.result(Future.sequence(futures), Duration.Inf)

  }

  it should "run benchmark for String" in {

    println("String benckmark")
    val futures = (0 until numOfCpu)
      .map { core =>

        val arr = Array.ofDim[String](N)

        (0 until arr.length).foreach { i =>
//          val data = Array.ofDim[Char](32)
          //          util.Random.nextBytes(data)
          arr(i) = "%016d".format(core * N + i)
        }

        (core, arr)
      }
      .map { case (core, arr) =>
        Future {

          (1 to M).foreach { m =>
            var it = 0
            val time = System.currentTimeMillis()
            var b = 0d

            while (it < K) {
              val i = it % (N -1) //util.Random.nextInt(arr.length)
              val j = N - i - 1 //util.Random.nextInt(arr.length)
              val t = if (arr(i).equals(arr(j))) 1 else 0
              it += 1
              b += i + j + m + t

            }

            println(s"$b Iteration $m on cpu $core time for String: " + (System.currentTimeMillis() - time))
          }
        }
      }

    Await.result(Future.sequence(futures), Duration.Inf)

  }

}
