package io.gatling.grpc.demo;

import io.gatling.grpc.demo.calculator.*;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.grpc.GrpcBidirectionalStreamingServiceBuilder;
import io.gatling.javaapi.grpc.GrpcClientStreamingServiceBuilder;
import io.gatling.javaapi.grpc.GrpcProtocolBuilder;
import io.gatling.javaapi.grpc.GrpcServerStreamingServiceBuilder;
import io.grpc.Status;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

    GrpcServerStreamingServiceBuilder<PrimeNumberDecompositionRequest, PrimeNumberDecompositionResponse> serverStream =
        grpc("Prime Number Decomposition")
            .serverStream(CalculatorServiceGrpc.getPrimeNumberDecompositionMethod())
            .check(
                statusCode().is(Status.Code.OK),
                response(PrimeNumberDecompositionResponse::getPrimeFactor)
                    .saveAs("primeFactor")
            )
            .reconcile((main, branch) -> {
                List<Long> primeFactors = main.getList("primeFactors");
                Long latestPrimeFactor = branch.getLongWrapper("primeFactor");
                primeFactors.add(latestPrimeFactor);
                return main.set("primeFactors", primeFactors)
                    .remove("primeFactor");
            })
            .responseTimePolicy((session, message, timestamp) -> timestamp);

    ScenarioBuilder serverStreaming = scenario("Calculator Server Streaming")
        .exec(session -> session.set("primeFactors", new ArrayList<Long>()))
        .exec(
            serverStream
                .send(
                    PrimeNumberDecompositionRequest.newBuilder()
                        .setNumber(109987656890L)
                        .build()
                )
        )
        .exec(
            serverStream.await(Duration.ofSeconds(1))
                .check(
                    response(PrimeNumberDecompositionResponse::getPrimeFactor)
                        .is(2L)
                )
        )
        .exec(
            serverStream.await(Duration.ofSeconds(1))
                .check(
                    response(PrimeNumberDecompositionResponse::getPrimeFactor)
                        .is(5L)
                )
        )
        .exec(
            serverStream.await(Duration.ofSeconds(1))
                .check(
                    response(PrimeNumberDecompositionResponse::getPrimeFactor)
                        .is(17L)
                )
        )
        .exec(
            serverStream.await(Duration.ofSeconds(1))
                .check(
                    response(PrimeNumberDecompositionResponse::getPrimeFactor)
                        .is(97L)
                )
        )
        .exec(
            serverStream.await(Duration.ofSeconds(1))
                .check(
                    response(PrimeNumberDecompositionResponse::getPrimeFactor)
                        .is(6669961L)
                )
        )
        .exec(session -> {
            List<Long> primeFactors = session.getList("primeFactors");
            System.out.println("primeFactors: " + primeFactors);
            return session;
        });

    GrpcClientStreamingServiceBuilder<ComputeAverageRequest, ComputeAverageResponse> clientStream =
        grpc("Compute Average")
            .clientStream(CalculatorServiceGrpc.getComputeAverageMethod())
            .check(
                response(ComputeAverageResponse::getAverage).saveAs("average")
            );

    ScenarioBuilder clientStreaming = scenario("Calculator Client Streaming")
        .exec(clientStream.start())
        .repeat(10).on(
            exec(clientStream.send(session ->
                ComputeAverageRequest.newBuilder()
                    .setNumber(ThreadLocalRandom.current().nextInt(0, 1000))
                    .build())
            )
        )
        .exec(clientStream.awaitStreamEnd())
        .exec(session -> {
            double average = session.getDouble("average");
            System.out.println("average: " + average);
            return session;
        });

    GrpcBidirectionalStreamingServiceBuilder<FindMaximumRequest, FindMaximumResponse> bidirectionalStream =
        grpc("Find Maximum")
            .bidiStream(CalculatorServiceGrpc.getFindMaximumMethod())
            .check(
                statusCode().is(Status.Code.OK),
                response(FindMaximumResponse::getMaximum)
                    .saveAs("maximum"))
            .reconcile((main, branch) -> {
                int maximum = main.getInt("maximum");
                int latestMaximum = branch.getInt("maximum");
                System.out.println("received maximum: " + latestMaximum + "(prev: " + maximum + ")");
                return main.set("maximum", latestMaximum);
            })
            .responseTimePolicy((session, message, timestamp) -> timestamp);

    ScenarioBuilder bidirectionalStreaming = scenario("Calculator Bidirectional Streaming")
        .exec(bidirectionalStream.start())
        .repeat(10).on(
            exec(bidirectionalStream.send(session ->
                FindMaximumRequest.newBuilder()
                    .setNumber(ThreadLocalRandom.current().nextInt(0, 1000))
                    .build()
            ))
        )
        .exec(bidirectionalStream.awaitStreamEnd())
        .exec(session -> {
            int maximum = session.getInt("maximum");
            System.out.println("maximum: " + maximum);
            return session;
        });

    ScenarioBuilder deadlines =
        scenario("Calculator w/ Deadlines")
            .exec(
                grpc("Square Root")
                    .unary(CalculatorServiceGrpc.getSquareRootMethod())
                    .send(
                        SquareRootRequest.newBuilder()
                            .setNumber(-2)
                            .build()
                    )
                    .check(
                        statusCode().is(Status.Code.INVALID_ARGUMENT)
                    )
            );

    {
        String name = System.getProperty("grpc.scenario");
        ScenarioBuilder scn;
        if (name == null) {
            scn = unary;
        } else {
            scn = switch (name) {
                case "serverStreaming" -> serverStreaming;
                case "clientStreaming" -> clientStreaming;
                case "bidirectionalStreaming" -> bidirectionalStreaming;
                case "deadlines" -> deadlines;
                default -> unary;
            };
        }

        setUp(
            scn.injectOpen(atOnceUsers(1))
        ).protocols(baseGrpcProtocol);
    }
}
