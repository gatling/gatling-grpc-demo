package io.gatling.grpc.demo

import io.gatling.grpc.demo.calculator.*
import io.gatling.javaapi.core.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.grpc.*
import io.gatling.javaapi.grpc.GrpcDsl.*
import io.grpc.*
import java.util.concurrent.ThreadLocalRandom

class CalculatorSimulation : Simulation() {

  private val baseGrpcProtocol = grpc.forAddress("localhost", 50052)

  private val unary =
    scenario("Calculator Unary")
      .exec(
        grpc("Sum")
          .unary(CalculatorServiceGrpc.getSumMethod())
          .send(
            sumRequest {
              firstNumber = 1
              secondNumber = 2
            }
          )
          .check(
            statusCode().shouldBe(Status.Code.OK),
            response(SumResponse::getSumResult).shouldBe(3),
          )
      )

  private val serverStream =
    grpc("Prime Number Decomposition")
      .serverStream(CalculatorServiceGrpc.getPrimeNumberDecompositionMethod())
      .check(
        statusCode().shouldBe(Status.Code.OK),
        response(PrimeNumberDecompositionResponse::getPrimeFactor)
          .transform { p -> p == 2L || p == 5L || p == 17L || p == 97L || p == 6669961L }
          .shouldBe(true),
      )

  private val serverStreaming =
    scenario("Calculator Server Streaming")
      .exec(
        serverStream.send(primeNumberDecompositionRequest { number = 109987656890L }),
        serverStream.awaitStreamEnd(),
      )

  private val clientStream =
    grpc("Compute Average")
      .clientStream(CalculatorServiceGrpc.getComputeAverageMethod())
      .check(
        statusCode().shouldBe(Status.Code.OK),
        response(ComputeAverageResponse::getAverage).saveAs("average"),
      )

  private val clientStreaming =
    scenario("Calculator Client Streaming")
      .exec(
        clientStream.start(),
        repeat(10)
          .on(
            clientStream.send { _ ->
              val number = ThreadLocalRandom.current().nextInt(0, 1000)
              ComputeAverageRequest.newBuilder().setNumber(number).build()
            }
          ),
        clientStream.halfClose(),
        clientStream.awaitStreamEnd { main, forked ->
          val average = forked.getDouble("average")
          main.set("average", average)
        },
        exec { session ->
          val average = session.getDouble("average")
          println("average: $average")
          session
        },
      )

  private val bidirectionalStream =
    grpc("Find Maximum")
      .bidiStream(CalculatorServiceGrpc.getFindMaximumMethod())
      .check(
        statusCode().shouldBe(Status.Code.OK),
        response(FindMaximumResponse::getMaximum).saveAs("maximum"),
      )

  private val bidirectionalStreaming =
    scenario("Calculator Bidirectional Streaming")
      .exec(
        bidirectionalStream.start(),
        repeat(10)
          .on(
            bidirectionalStream.send { _ ->
              findMaximumRequest { number = ThreadLocalRandom.current().nextInt(0, 1000) }
            }
          ),
        bidirectionalStream.halfClose(),
        bidirectionalStream.awaitStreamEnd { main, forked ->
          val latestMaximum = forked.getInt("maximum")
          main.set("maximum", latestMaximum)
        },
        exec { session ->
          val maximum = session.getInt("maximum")
          println("maximum: $maximum")
          session
        },
      )

  private val deadlines =
    scenario("Calculator w/ Deadlines")
      .exec(
        grpc("Square Root")
          .unary(CalculatorServiceGrpc.getSquareRootMethod())
          .send(squareRootRequest { number = -2 })
          .check(statusCode().shouldBe(Status.Code.INVALID_ARGUMENT))
      )

  // spotless:off
  // ./gradlew -Dgrpc.scenario=unary gatlingRun --simulation io.gatling.grpc.demo.CalculatorSimulation
  // ./gradlew -Dgrpc.scenario=serverStreaming gatlingRun --simulation io.gatling.grpc.demo.CalculatorSimulation
  // ./gradlew -Dgrpc.scenario=clientStreaming gatlingRun --simulation io.gatling.grpc.demo.CalculatorSimulation
  // ./gradlew -Dgrpc.scenario=bidirectionalStreaming gatlingRun --simulation io.gatling.grpc.demo.CalculatorSimulation
  // ./gradlew -Dgrpc.scenario=deadlines gatlingRun --simulation io.gatling.grpc.demo.CalculatorSimulation
  // spotless:on

  init {
    val name = System.getProperty("grpc.scenario")
    val scn =
      if (name == null) {
        unary
      } else {
        when (name) {
          "serverStreaming" -> serverStreaming
          "clientStreaming" -> clientStreaming
          "bidirectionalStreaming" -> bidirectionalStreaming
          "deadlines" -> deadlines
          else -> unary
        }
      }
    setUp(scn.injectOpen(atOnceUsers(1))).protocols(baseGrpcProtocol)
  }
}
