package io.gatling.grpc.demo

import java.util.concurrent.ThreadLocalRandom

object Feeders {

  private val Alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

  private def random(alphabet: String, n: Int): String =
    LazyList.continually(ThreadLocalRandom.current.nextInt(alphabet.length)).map(alphabet).take(n).mkString

  private def randomString(n: Int): String = random(Alphabet, n)

  def randomNames: Iterator[Map[String, String]] =
    Iterator.continually {
      Map(
        "firstName" -> randomString(20),
        "lastName" -> randomString(20)
      )
    }
}
