package io.gatling.grpc

import io.gatling.core.Predef._
import io.gatling.grpc.Predef._
import io.gatling.grpc.demo.calculator
import io.gatling.grpc.demo.calculator._
import io.grpc.{ Metadata, Status }

import java.util.concurrent.ThreadLocalRandom
import scala.concurrent.duration._

class CalculatorSimulation extends Simulation {

  private val baseGrpcProtocol =
    Configuration.baseGrpcProtocol("localhost", 50052)

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
          header(Metadata.Key.of("header-san", Metadata.ASCII_STRING_MARSHALLER)).is("oui"),
          header(Metadata.Key.of("header-san", Metadata.ASCII_STRING_MARSHALLER)).findAll.is(List("oui", "non")),
          header(Metadata.Key.of("header-chan", Metadata.ASCII_STRING_MARSHALLER)).is("nope"),
          trailer(Metadata.Key.of("trailer-san", Metadata.ASCII_STRING_MARSHALLER)).is("salutations maximales 1"),
          trailer(Metadata.Key.of("trailer-san", Metadata.ASCII_STRING_MARSHALLER)).findAll.is(List("salutations maximales 1", "salutations maximales 2")),
          trailer(Metadata.Key.of("trailer-chan", Metadata.ASCII_STRING_MARSHALLER)).is("salutations maximales"),
          response((result: SumResponse) => result.sumResult).is(3)
        )
    )

  private val serverStream = grpc("Prime Number Decomposition")
    .serverStream(CalculatorServiceGrpc.METHOD_PRIME_NUMBER_DECOMPOSITION)
    .check(
      statusCode.is(Status.Code.OK),
      response((result: PrimeNumberDecompositionResponse) => result.primeFactor)
        .saveAs("primeFactor")
    )
    .reconcile { (main, branch) =>
      for {
        primeFactors <- main("primeFactors").validate[List[Long]]
        latestPrimeFactor <- branch("primeFactor").validate[Long]
      } yield {
        main
          .set("primeFactors", primeFactors :+ latestPrimeFactor)
          .remove("primeFactor")
      }
    }
    .responseTimePolicy((_, _, timestamp) => timestamp)

  private val serverStreaming = scenario("Calculator Server Streaming")
    .exec(_.set("primeFactors", List.empty[Long]))
    .exec(
      serverStream
        .send(
          PrimeNumberDecompositionRequest(
            number = 109987656890L
          )
        )
    )
    .exec(
      serverStream
        .await(1.second)
        .check(
          response((result: PrimeNumberDecompositionResponse) => result.primeFactor)
            .is(2L)
        )
    )
    .exec(
      serverStream
        .await(1.second)
        .check(
          response((result: PrimeNumberDecompositionResponse) => result.primeFactor)
            .is(5L)
        )
    )
    .exec(
      serverStream
        .await(1.second)
        .check(
          response((result: PrimeNumberDecompositionResponse) => result.primeFactor)
            .is(17L)
        )
    )
    .exec(
      serverStream
        .await(1.second)
        .check(
          response((result: PrimeNumberDecompositionResponse) => result.primeFactor)
            .is(97L)
        )
    )
    .exec(
      serverStream
        .await(1.second)
        .check(
          response((result: PrimeNumberDecompositionResponse) => result.primeFactor)
            .is(6669961L)
        )
    )
    .exec { session =>
      for {
        primeFactors <- session("primeFactors").validate[List[Long]]
      } yield {
        println(s"primeFactors: $primeFactors")
        session
      }
    }

  private val clientStream = grpc("Compute Average")
    .clientStream(CalculatorServiceGrpc.METHOD_COMPUTE_AVERAGE) // will end up as session attribute, giving it a name is not useful?
    .check(
      response((result: ComputeAverageResponse) => result.average)
        .saveAs("average")
    )

  private val clientStreaming = scenario("Calculator Client Streaming")
    .exec(clientStream.start)
    .repeat(10) {
      pause(100.milliseconds, 200.milliseconds)
        .exec(clientStream.send { _ =>
          ComputeAverageRequest(
            number = ThreadLocalRandom.current.nextInt(0, 1000)
          )
        })
    }
    .exec(clientStream.awaitStreamEnd)
    .exec { session =>
      for {
        average <- session("average").validate[Double]
      } yield {
        println(s"average: $average")
        session
      }
    }

  private val bidirectionalStream = grpc("Find Maximum")
    .bidiStream(CalculatorServiceGrpc.METHOD_FIND_MAXIMUM)
    .check(
      statusCode.is(Status.Code.OK),
      response((result: calculator.FindMaximumResponse) => result.maximum)
        .saveAs("maximum")
    )
    .reconcile { (main, branch) =>
      for {
        maximum <- main("maximum").validate[Int]
        latestMaximum <- branch("maximum").validate[Int]
      } yield {
        println(s"received maximum: $latestMaximum (prev: $maximum)")
        main.set("maximum", latestMaximum)
      }
    }
    .responseTimePolicy((_, _, timestamp) => timestamp)

  private val bidirectionalStreaming = scenario("Calculator Bidirectional Streaming")
    .exec(bidirectionalStream.start)
    .repeat(10) {
      exec(bidirectionalStream.send { _ =>
        val number = ThreadLocalRandom.current.nextInt(0, 1000)
        FindMaximumRequest(
          number = number
        )
      })
    }
    .exec(bidirectionalStream.awaitStreamEnd)
    .exec { session =>
      for {
        maximum <- session("maximum").validate[Int]
      } yield {
        println(s"maximum: $maximum")
        session
      }
    }

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

  // eval sys.props("grpc.scenario") = "serverStreaming"
  // Gatling / testOnly io.gatling.grpc.CalculatorSimulation

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
