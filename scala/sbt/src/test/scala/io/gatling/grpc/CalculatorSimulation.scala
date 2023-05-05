package io.gatling.grpc

import io.gatling.core.Predef._
import io.gatling.grpc.Predef._
import io.gatling.grpc.demo.calculator._

import io.grpc.Status

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
          response((result: SumResponse) => result.sumResult).is(3)
        )
    )

//  private val serverStream = grpc("Prime Number Decomposition")
//    .serverStream("serverStream") // will end up as session attribute, giving it a name is not useful?
//
//  private val serverStreaming = scenario("Calculator Server Streaming")
//    .exec(_.set("primeFactors", List.empty[Long]))
//    .exec(
//      serverStream
//        .start(CalculatorServiceGrpc.METHOD_PRIME_NUMBER_DECOMPOSITION) {
//          PrimeNumberDecompositionRequest(
//            number = 109987656890L
//          )
//        }
//        .endCheck(statusCode.is(Status.Code.OK))
//        .extract(_.primeFactor.some)(_.saveAs("primeFactor"))
//        // FIXME (session, session) -> Validation[Session] ?
//        .sessionCombiner { (main, branch) =>
//          val primeFactors = main("primeFactors").as[List[Long]]
//          val latestPrimeFactor = branch("primeFactor").as[Long]
//          main.set("primeFactors", primeFactors :+ latestPrimeFactor)
//            .remove("primeFactor")
//        }
//        .timestampExtractor { (session, message, t) =>
//          println(s"$t: $message")
//          // TimestampExtractor.IgnoreMessage
//          t
//        }
//    )
//    // NextMessage also handles StreamEnd
//    // There is no way to loop until stream end
//    .exec(serverStream.copy(requestName = "next message").reconciliate(waitFor = NextMessage))
//    .exec(serverStream.copy(requestName = "next message").reconciliate(waitFor = NextMessage))
//    .exec(serverStream.copy(requestName = "next message").reconciliate(waitFor = NextMessage))
//    .exec(serverStream.copy(requestName = "next message").reconciliate(waitFor = NextMessage))
//    .exec(serverStream.copy(requestName = "stream end").reconciliate(waitFor = StreamEnd))
//    // Fails if stream has completed
//    // Will still perform sessionCombiner with the data from previous request, duplicating stuff...
//    // .exec(serverStream.cancelStream)
//    .exec { session =>
//      for {
//        primeFactors <- session("primeFactors").validate[List[Long]]
//      } yield {
//        println(s"primeFactors: $primeFactors")
//        session
//      }
//    }
//
//  private val clientStream = grpc("Compute Average")
//    .clientStream("clientStream") // will end up as session attribute, giving it a name is not useful?
//
//  private val clientStreaming = scenario("Calculator Client Streaming")
//    .exec(
//      clientStream
//        .connect(CalculatorServiceGrpc.METHOD_COMPUTE_AVERAGE)
//        .extract(_.average.some)(_.saveAs("average"))
//    )
//    .repeat(10) {
//      pause(100.milliseconds, 200.milliseconds)
//        .exec(clientStream.send { _ =>
//          ComputeAverageRequest(
//            number = ThreadLocalRandom.current.nextInt(0, 1000)
//          )
//        })
//    }
//    .exec(clientStream.completeAndWait)
//    .exec { session =>
//      for {
//        average <- session("average").validate[Double]
//      } yield {
//        println(s"average: $average")
//        session
//      }
//    }
//
//  val biDirectionalStream = grpc("Find Maximum")
//    .bidiStream("biDirectionalStream")
//
//  private val biDirectionalStreaming = scenario("Calculator Bi Directional Streaming")
//    .exec(
//      biDirectionalStream
//        .connect(CalculatorServiceGrpc.METHOD_FIND_MAXIMUM)
//        .endCheck(statusCode.is(Status.Code.OK))
//        .extract(_.maximum.some)(_.saveAs("maximum"))
//        // FIXME (session, session) -> Validation[Session] ?
//        .sessionCombiner { (main, branch) =>
//          val maximum = main("maximum").as[Int]
//          val latestMaximum = branch("maximum").as[Int]
//          println(s"received maximum: $latestMaximum (prev: $maximum)")
//          main.set("maximum", latestMaximum)
//        }
//        .timestampExtractor { (session, message, t) =>
//          println(s"$t: $message")
//          //TimestampExtractor.IgnoreMessage
//          t
//        }
//    )
//    .repeat(10) {
//      pause(100.milliseconds, 200.milliseconds)
//        .exec(biDirectionalStream.send { _ =>
//          val number = ThreadLocalRandom.current.nextInt(0, 1000)
//          println(s"sending: $number")
//          FindMaximumRequest(
//            number = number
//          )
//        })
//    }
//    .exec(biDirectionalStream.copy(requestName = "Complete").complete)
//    .exec { session =>
//      for {
//        maximum <- session("maximum").validate[Int]
//      } yield {
//        println(s"maximum: $maximum")
//        session
//      }
//    }

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
          status.is(Status.Code.INVALID_ARGUMENT)
        )
    )

  private val scn = sys.props.get("grpc.scenario") match {
    // case Some("serverStreaming")        => serverStreaming
    // case Some("clientStreaming")        => clientStreaming
    // case Some("biDirectionalStreaming") => biDirectionalStreaming
    case Some("deadlines") => deadlines
    case _                 => unary
  }

  setUp(
    scn.inject(
      atOnceUsers(1)
    )
  ).protocols(baseGrpcProtocol)
}
