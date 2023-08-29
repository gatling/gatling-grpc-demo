package io.gatling.grpc.demo

import io.gatling.core.Predef._
import io.gatling.grpc.Predef._
import io.gatling.grpc.protocol.GrpcProtocolBuilder

object Configuration {
  def baseGrpcProtocol(host: String, port: Int): GrpcProtocolBuilder =
    grpc
      .forAddress(host, port)
      .useInsecureTrustManager
}
