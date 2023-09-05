package io.gatling.grpc.demo

import io.gatling.grpc.demo.Feeders.randomNames
import io.gatling.grpc.demo.greeting.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Session
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.grpc.GrpcDsl.*
import io.grpc.CallOptions
import io.grpc.Status
import java.util.concurrent.TimeUnit

class GreetingSimulation : Simulation() {

  private val baseGrpcProtocol = Configuration.baseGrpcProtocol("localhost", 50051)

  private val greeting = { session: Session ->
    val firstName = session.getString("firstName")
    val lastName = session.getString("lastName")
    Greeting.newBuilder().setFirstName(firstName).setLastName(lastName).build()
  }

  private val unary =
    scenario("Greet Unary")
      .feed(randomNames())
      .repeat(10)
      .on(
        exec(
          grpc("Greet")
            .unary(GreetingServiceGrpc.getGreetMethod())
            .send { session -> GreetRequest.newBuilder().setGreeting(greeting(session)).build() }
            .check(
              statusCode().shouldBe(Status.Code.OK),
              response(GreetResponse::getResult).isEL("Hello #{firstName} #{lastName}")
            )
        )
      )

  private val deadlines =
    scenario("Greet w/ Deadlines")
      .feed(randomNames())
      .exec(
        grpc("Greet w/ Deadlines")
          .unary(GreetingServiceGrpc.getGreetWithDeadlineMethod())
          .send { session -> GreetRequest.newBuilder().setGreeting(greeting(session)).build() }
          .callOptions(CallOptions.DEFAULT.withDeadlineAfter(100, TimeUnit.MILLISECONDS))
          .check(statusCode().shouldBe(Status.Code.DEADLINE_EXCEEDED))
      )

  /* mvn gatling:test -Dgrpc.scenario=unary \
       -Dgatling.simulationClass=io.gatling.grpc.demo.GreetingSimulation
  */
  /* mvn gatling:test -Dgrpc.scenario=deadlines \
       -Dgatling.simulationClass=io.gatling.grpc.demo.GreetingSimulation
  */

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
