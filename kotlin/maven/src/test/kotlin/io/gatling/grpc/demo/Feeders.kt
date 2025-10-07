package io.gatling.grpc.demo

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.FeederBuilder
import io.grpc.*
import java.util.concurrent.ThreadLocalRandom

object Feeders {

  private fun channelCredentialsByIndex(i: Int): ChannelCredentials {
    return TlsChannelCredentials.newBuilder()
      .keyManager(
        ClassLoader.getSystemResourceAsStream("certs/client$i.crt"),
        ClassLoader.getSystemResourceAsStream("certs/client$i.key"),
      )
      .trustManager(ClassLoader.getSystemResourceAsStream("certs/ca.crt"))
      .build()
  }

  val channelCredentials: FeederBuilder<Any> =
    listFeeder((1..3).map { i -> mapOf("channelCredentials" to channelCredentialsByIndex(i)) })

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

  val randomNames: Iterator<Map<String, Any>> =
    object : Iterator<Map<String, Any>> {
      override fun hasNext(): Boolean {
        return true
      }

      override fun next(): Map<String, Any> {
        return mapOf("firstName" to randomString(20), "lastName" to randomString(20))
      }
    }
}
