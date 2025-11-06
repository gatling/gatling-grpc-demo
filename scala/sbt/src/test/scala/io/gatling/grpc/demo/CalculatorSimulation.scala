package io.gatling.grpc.demo

import java.util.concurrent.ThreadLocalRandom

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.grpc.Predef._
import io.gatling.grpc.demo.calculator._

import io.grpc.Status

class CalculatorSimulation extends Simulation {

  private val calculatorServer =
    grpc.serverConfiguration("calculator")
      .forAddress("localhost", 50052)

  private val baseGrpcProtocol =
    grpc.serverConfigurations(calculatorServer)

  private val unary = scenario("Calculator Unary")
    .exec(
      grpc("Sum")
        .unary(CalculatorServiceGrpc.METHOD_SUM)
        .send(
          SumRequest(
            firstNumber = 1,
            secondNumber = 2
          )
        )
        .check(
          response((result: SumResponse) => result.sumResult).is(3)
        )
    )

  private val serverStream = grpc("Prime Number Decomposition")
    .serverStream(CalculatorServiceGrpc.METHOD_PRIME_NUMBER_DECOMPOSITION)
    .check(
      statusCode.is(Status.Code.OK),
      response((response: PrimeNumberDecompositionResponse) => response.primeFactor)
        .transform(p => p == 2L || p == 5L || p == 17L || p == 97L || p == 6669961L)
        .is(true)
    )

  private val serverStreaming = scenario("Calculator Server Streaming")
    .exec(
      serverStream.send(
        PrimeNumberDecompositionRequest(number = 109987656890L)
      ),
      serverStream.awaitStreamEnd
    )

  private val clientStream = grpc("Compute Average")
    .clientStream(CalculatorServiceGrpc.METHOD_COMPUTE_AVERAGE) // will end up as session attribute, giving it a name is not useful?
    .check(
      statusCode.is(Status.Code.OK),
      response((result: ComputeAverageResponse) => result.average)
        .saveAs("average")
    )

  private val clientStreaming = scenario("Calculator Client Streaming")
    .exec(
      clientStream.start,
      repeat(10) {
        pause(100.milliseconds, 200.milliseconds)
          .exec(clientStream.send { _ =>
            val number = ThreadLocalRandom.current.nextInt(0, 1000)
            ComputeAverageRequest(
              number = number
            )
          })
      },
      clientStream.halfClose,
      clientStream.awaitStreamEnd { (main, forked) =>
        for {
          average <- forked("average").validate[Double]
        } yield main.set("average", average)
      },
      exec { session =>
        for {
          average <- session("average").validate[Double]
        } yield {
          println(s"average: $average")
          session
        }
      }
    )

  private val bidirectionalStream = grpc("Find Maximum")
    .bidiStream(CalculatorServiceGrpc.METHOD_FIND_MAXIMUM)
    .check(
      statusCode.is(Status.Code.OK),
      response((result: FindMaximumResponse) => result.maximum)
        .saveAs("maximum")
    )

  private val bidirectionalStreaming = scenario("Calculator Bidirectional Streaming")
    .exec(
      bidirectionalStream.start,
      repeat(10) {
        exec(bidirectionalStream.send { _ =>
          val number = ThreadLocalRandom.current.nextInt(0, 1000)
          FindMaximumRequest(
            number = number
          )
        })
      },
      bidirectionalStream.halfClose,
      bidirectionalStream.awaitStreamEnd { (main, forked) =>
        for {
          maximum <- forked("maximum").validate[Int]
        } yield main.set("maximum", maximum)
      },
      exec { session =>
        for {
          maximum <- session("maximum").validate[Int]
        } yield {
          println(s"maximum: $maximum")
          session
        }
      }
    )

  private val deadlines = scenario("Calculator w/ Deadlines")
    .exec(
      grpc("Square Root")
        .unary(CalculatorServiceGrpc.METHOD_SQUARE_ROOT)
        .send(
          SquareRootRequest(
            number = -2
          )
        )
        .check(
          statusCode.is(Status.Code.INVALID_ARGUMENT)
        )
    )

  // In sbt interactive mode, use one of:
  // eval sys.props("grpc.scenario") = "unary"
  // eval sys.props("grpc.scenario") = "serverStreaming"
  // eval sys.props("grpc.scenario") = "clientStreaming"
  // eval sys.props("grpc.scenario") = "bidirectionalStreaming"
  // eval sys.props("grpc.scenario") = "deadlines"

  // Then:
  // Gatling / testOnly io.gatling.grpc.demo.CalculatorSimulation

  private val scn = sys.props.get("grpc.scenario") match {
    case Some("serverStreaming")        => serverStreaming
    case Some("clientStreaming")        => clientStreaming
    case Some("bidirectionalStreaming") => bidirectionalStreaming
    case Some("deadlines")              => deadlines
    case _                              => unary
  }

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(baseGrpcProtocol)
}
