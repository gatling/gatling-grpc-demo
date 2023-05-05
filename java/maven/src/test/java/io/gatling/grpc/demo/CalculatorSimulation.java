package io.gatling.grpc.demo;

import io.gatling.grpc.demo.calculator.*;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.grpc.*;

import io.grpc.Status;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.grpc.GrpcDsl.*;

public class CalculatorSimulation extends Simulation {

    GrpcProtocolBuilder baseGrpcProtocol =
        Configuration.baseGrpcProtocol("localhost", 50052);

    ScenarioBuilder unary =
        scenario("Calculator Unary")
            .exec(
                grpc("Sum")
                    .unary(CalculatorServiceGrpc.getSumMethod())
                    .send(
                        SumRequest.newBuilder()
                            .setFirstNumber(1)
                            .setSecondNumber(2)
                            .build()
                    )
                    .check(
                        response(SumResponse::getSumResult).is(3)
                    )
            );

    //  private val serverStream = grpc("Prime Number Decomposition")
    //    .serverStream("serverStream") // will end up as session attribute, giving it a name is not
    // useful?
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
    //    // Will still perform sessionCombiner with the data from previous request, duplicating
    // stuff...
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
    //    .clientStream("clientStream") // will end up as session attribute, giving it a name is not
    // useful?
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

    ScenarioBuilder deadlines =
        scenario("Calculator w/ Deadlines")
            .exec(
                grpc("Square Root")
                    .unary(CalculatorServiceGrpc.getSquareRootMethod())
                    .send(SquareRootRequest.newBuilder().setNumber(-2).build())
                    .check(status().is(Status.Code.INVALID_ARGUMENT)));

    {
        String name = System.getProperty("grpc.scenario");
        ScenarioBuilder scn;
        if (name == null) {
            scn = unary;
        } else {
            scn = switch (name) {
                // case "serverStreaming" -> serverStreaming;
                // case "clientStreaming" -> clientStreaming;
                // case "biDirectionalStreaming" -> biDirectionalStreaming;
                case "deadlines" -> deadlines;
                default -> unary;
            };
        }

        setUp(
            scn.injectOpen(atOnceUsers(1))
        ).protocols(baseGrpcProtocol);
    }
}
