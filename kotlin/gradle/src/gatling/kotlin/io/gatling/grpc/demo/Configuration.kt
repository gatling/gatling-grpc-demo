package io.gatling.grpc.demo

import io.gatling.javaapi.grpc.GrpcDsl
import io.gatling.javaapi.grpc.GrpcProtocolBuilder

object Configuration {
  @JvmStatic
  fun baseGrpcProtocol(host: String, port: Int): GrpcProtocolBuilder {
    return GrpcDsl.grpc.forAddress(host, port).useInsecureTrustManager()
  }
}
