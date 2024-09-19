package io.gatling.grpc.demo

import io.gatling.grpc.demo.greeting.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Session
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.grpc.GrpcDsl.*
import io.grpc.Status
import java.time.Duration

class GreetingSimulation : Simulation() {

  private val baseGrpcProtocol =
    grpc
      .forAddress("localhost", 50051)
      .channelCredentials("#{channelCredentials}")
      .overrideAuthority("gatling-grpc-demo-test-server")

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
      .feed(Feeders.channelCredentials().circular())
      .feed(Feeders.randomNames())
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
      .feed(Feeders.channelCredentials().circular())
      .feed(Feeders.randomNames())
      .exec(
        grpc("Greet w/ Deadlines")
          .unary(GreetingServiceGrpc.getGreetWithDeadlineMethod())
          .send(greetRequest)
          .deadlineAfter(Duration.ofMillis(100))
          .check(statusCode().shouldBe(Status.Code.DEADLINE_EXCEEDED))
      )

  // ./gradlew -Dgrpc.scenario=unary gatlingRun --simulation io.gatling.grpc.demo.GreetingSimulation
  // ./gradlew -Dgrpc.scenario=deadlines gatlingRun --simulation io.gatling.grpc.demo.GreetingSimulation

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
