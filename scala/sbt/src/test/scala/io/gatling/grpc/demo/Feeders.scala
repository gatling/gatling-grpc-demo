package io.gatling.grpc.demo

import java.util.concurrent.ThreadLocalRandom

import io.grpc._

object Feeders {

  val channelCredentials: Array[Map[String, ChannelCredentials]] =
    (1 to 3).map { i =>
      val credentials = TlsChannelCredentials
        .newBuilder()
        .keyManager(
          ClassLoader.getSystemResourceAsStream(s"certs/client$i.crt"),
          ClassLoader.getSystemResourceAsStream(s"certs/client$i.key")
        )
        .trustManager(ClassLoader.getSystemResourceAsStream("certs/ca.crt"))
        .build
      Map("channelCredentials" -> credentials)
    }.toArray

  private val Alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

  private def random(alphabet: String, n: Int): String =
    LazyList.continually(ThreadLocalRandom.current.nextInt(alphabet.length)).map(alphabet).take(n).mkString

  private def randomString(n: Int): String = random(Alphabet, n)

  val randomNames: Iterator[Map[String, String]] =
    Iterator.continually {
      Map(
        "firstName" -> randomString(20),
        "lastName" -> randomString(20)
      )
    }
}
