package io.gatling.grpc

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.grpc.Predef._
import io.gatling.grpc.demo.greeting._

import io.grpc.{ CallOptions, Status }

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
          status.is(Status.Code.OK),
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
        .callOptions(CallOptions.DEFAULT.withDeadlineAfter(100, MILLISECONDS))
        .check(status.is(Status.Code.DEADLINE_EXCEEDED))
    )

  // eval sys.props("grpc.scenario") = "serverStreaming"
  // Gatling / testOnly io.gatling.grpc.GreetSimulation

  private val scn = sys.props.get("grpc.scenario") match {
    case Some("deadlines") => deadlines
    case _                 => unary
  }

  setUp(
    scn.inject(
      atOnceUsers(1)
//      constantConcurrentUsers(100).during(30.seconds)
//      rampUsers(10).during(10.seconds)
    )
  ).protocols(baseGrpcProtocol)
}
