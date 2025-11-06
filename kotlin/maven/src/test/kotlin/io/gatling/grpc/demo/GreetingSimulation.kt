package io.gatling.grpc.demo

import io.gatling.grpc.demo.greeting.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Session
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.grpc.GrpcDsl.*
import io.grpc.Status
import java.time.Duration

class GreetingSimulation : Simulation() {

  private val greetingServer =
    grpc
      .serverConfiguration("greeting")
      .forAddress("localhost", 50051)
      .channelCredentials("#{channelCredentials}")
      .overrideAuthority("gatling-grpc-demo-test-server")

  private val baseGrpcProtocol = grpc.serverConfigurations(greetingServer)

  private val greeting = { session: Session ->
    val firstName = session.getString("firstName")
    val lastName = session.getString("lastName")
    Greeting.newBuilder().setFirstName(firstName).setLastName(lastName).build()
  }

  private val unary =
    scenario("Greet Unary")
      .feed(Feeders.channelCredentials.circular())
      .feed(Feeders.randomNames)
      .exec(
        grpc("Greet")
          .unary(GreetingServiceGrpc.getGreetMethod())
          .send { session -> GreetRequest.newBuilder().setGreeting(greeting(session)).build() }
          .check(
            statusCode().shouldBe(Status.Code.OK),
            response(GreetResponse::getResult).isEL("Hello #{firstName} #{lastName}"),
          )
      )

  private val deadlines =
    scenario("Greet w/ Deadlines")
      .feed(Feeders.channelCredentials.circular())
      .feed(Feeders.randomNames)
      .exec(
        grpc("Greet w/ Deadlines")
          .unary(GreetingServiceGrpc.getGreetWithDeadlineMethod())
          .send { session -> GreetRequest.newBuilder().setGreeting(greeting(session)).build() }
          .deadlineAfter(Duration.ofMillis(100))
          .check(statusCode().shouldBe(Status.Code.DEADLINE_EXCEEDED))
      )

  // spotless:off
  // ./mvnw gatling:test -Dgrpc.scenario=unary -Dgatling.simulationClass=io.gatling.grpc.demo.GreetingSimulation
  // ./mvnw gatling:test -Dgrpc.scenario=deadlines -Dgatling.simulationClass=io.gatling.grpc.demo.GreetingSimulation
  // spotless:on

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
