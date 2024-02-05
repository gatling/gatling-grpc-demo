package io.gatling.grpc.demo

import java.time.Duration

import io.gatling.grpc.demo.Feeders.randomNames
import io.gatling.grpc.demo.greeting.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Session
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.grpc.GrpcDsl.*

import io.grpc.Status

class GreetingSimulation : Simulation() {

  private val baseGrpcProtocol = Configuration.baseGrpcProtocol("localhost", 50051)

  private val greetRequest = { session: Session ->
    greetRequest {
      greeting = greeting {
        firstName = session.getString("firstName") ?: "null"
        lastName = session.getString("lastName") ?: "null"
      }
    }
  }

  private val unary =
    scenario("Greet Unary")
      .feed(randomNames())
      .exec(
        grpc("Greet")
          .unary(GreetingServiceGrpc.getGreetMethod())
          .send(greetRequest)
          .check(
            statusCode().shouldBe(Status.Code.OK),
            response(GreetResponse::getResult).isEL("Hello #{firstName} #{lastName}")
          )
      )

  private val deadlines =
    scenario("Greet w/ Deadlines")
      .feed(randomNames())
      .exec(
        grpc("Greet w/ Deadlines")
          .unary(GreetingServiceGrpc.getGreetWithDeadlineMethod())
          .send(greetRequest)
          .deadlineAfter(Duration.ofMillis(100))
          .check(statusCode().shouldBe(Status.Code.DEADLINE_EXCEEDED))
      )

  // ./gradlew -Dgrpc.scenario=unary gatlingRun-io.gatling.grpc.demo.GreetingSimulation
  // ./gradlew -Dgrpc.scenario=deadlines gatlingRun-io.gatling.grpc.demo.GreetingSimulation

  init {
    val name = System.getProperty("grpc.scenario")
    val scn =
      if ("deadlines" == name) {
        deadlines
      } else {
        unary
      }
    setUp(scn.injectOpen(atOnceUsers(1))).protocols(baseGrpcProtocol)
  }
}
