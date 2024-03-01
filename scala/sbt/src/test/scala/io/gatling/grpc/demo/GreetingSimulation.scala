package io.gatling.grpc.demo

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.grpc.Predef._
import io.gatling.grpc.demo.greeting._

import io.grpc.Status

class GreetingSimulation extends Simulation {

  private val baseGrpcProtocol =
    Configuration.baseGrpcProtocol("localhost", 50051)

  private val greeting: Expression[Greeting] = { session =>
    for {
      firstName <- session("firstName").validate[String]
      lastName <- session("lastName").validate[String]
    } yield Greeting(firstName, lastName)
  }

  private val unary = scenario("Greet Unary")
    .feed(Feeders.randomNames)
    .exec(
      grpc("Greet")
        .unary(GreetingServiceGrpc.METHOD_GREET)
        .send { session =>
          for {
            greeting <- greeting(session)
          } yield GreetRequest(greeting = Some(greeting))
        }
        .check(
          statusCode.is(Status.Code.OK),
          response((response: GreetResponse) => response.result)
            .is("Hello #{firstName} #{lastName}")
        )
    )

  private val deadlines = scenario("Greet w/ Deadlines")
    .feed(Feeders.randomNames)
    .exec(
      grpc("Greet w/ Deadlines")
        .unary(GreetingServiceGrpc.METHOD_GREET_WITH_DEADLINE)
        .send { session =>
          for {
            greeting <- greeting(session)
          } yield GreetRequest(greeting = Some(greeting))
        }
        .deadlineAfter(100.milliseconds)
        .check(statusCode.is(Status.Code.DEADLINE_EXCEEDED))
    )

  // In sbt interactive mode, use one of:
  // eval sys.props("grpc.scenario") = "unary"
  // eval sys.props("grpc.scenario") = "deadlines"

  // Then:
  // Gatling / testOnly io.gatling.grpc.demo.GreetingSimulation

  private val scn = sys.props.get("grpc.scenario") match {
    case Some("deadlines") => deadlines
    case _                 => unary
  }

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(baseGrpcProtocol)
}
