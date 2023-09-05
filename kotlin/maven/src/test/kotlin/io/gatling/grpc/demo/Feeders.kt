package io.gatling.grpc.demo

import java.util.concurrent.ThreadLocalRandom
import java.util.function.Supplier

object Feeders {

  private fun random(alphabet: String, n: Int): String {
    val s = StringBuilder()
    for (i in 0 until n) {
      val index = ThreadLocalRandom.current().nextInt(alphabet.length)
      s.append(alphabet[index])
    }
    return s.toString()
  }

  private fun randomString(n: Int): String {
    return random("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", n)
  }

  fun randomNames(): Supplier<Iterator<Map<String, Any>>> {
    return Supplier {
      object : Iterator<Map<String, Any>> {
        override fun hasNext(): Boolean {
          return true
        }

        override fun next(): Map<String, Any> {
          return mapOf("firstName" to randomString(20), "lastName" to randomString(20))
        }
      }
    }
  }
}
