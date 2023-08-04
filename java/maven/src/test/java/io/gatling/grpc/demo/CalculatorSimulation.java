package io.gatling.grpc.demo;

import io.gatling.grpc.demo.calculator.*;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.grpc.GrpcBidirectionalStreamingServiceBuilder;
import io.gatling.javaapi.grpc.GrpcClientStreamingServiceBuilder;
import io.gatling.javaapi.grpc.GrpcProtocolBuilder;
import io.gatling.javaapi.grpc.GrpcServerStreamingServiceBuilder;
import io.grpc.Status;

import java.util.ArrayList;
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
                        statusCode().is(Status.Code.OK),
                        response(SumResponse::getSumResult).is(3)
                    )
            );

    GrpcServerStreamingServiceBuilder<PrimeNumberDecompositionRequest, PrimeNumberDecompositionResponse> serverStream =
        grpc("Prime Number Decomposition")
            .serverStream(CalculatorServiceGrpc.getPrimeNumberDecompositionMethod())
            .check(
                statusCode().is(Status.Code.OK),
                response(PrimeNumberDecompositionResponse::getPrimeFactor)
                    .transform(p -> p == 2L || p == 5L || p == 17L || p == 97L || p == 6669961L)
                    .is(true)
            );

    ScenarioBuilder serverStreaming = scenario("Calculator Server Streaming")
        .exec(
            serverStream
                .send(
                    PrimeNumberDecompositionRequest.newBuilder()
                        .setNumber(109987656890L)
                        .build()
                )
        )
        .exec(
            serverStream.awaitStreamEnd()
        );

    GrpcClientStreamingServiceBuilder<ComputeAverageRequest, ComputeAverageResponse> clientStream =
        grpc("Compute Average")
            .clientStream(CalculatorServiceGrpc.getComputeAverageMethod())
            .check(
                statusCode().is(Status.Code.OK),
                response(ComputeAverageResponse::getAverage)
                    .saveAs("average")
            );

    ScenarioBuilder clientStreaming = scenario("Calculator Client Streaming")
        .exec(clientStream.start())
        .repeat(10).on(
            exec(clientStream.send(session -> {
                int number = ThreadLocalRandom.current().nextInt(0, 1000);
                return ComputeAverageRequest.newBuilder()
                    .setNumber(number)
                    .build();
                })
        )
        .exec(clientStream.halfClose())
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
                    .saveAs("maximum"));

    ScenarioBuilder bidirectionalStreaming = scenario("Calculator Bidirectional Streaming")
        .exec(bidirectionalStream.start())
        .repeat(10).on(
            exec(bidirectionalStream.send(session -> {
                int number = ThreadLocalRandom.current().nextInt(0, 1000);
                return FindMaximumRequest.newBuilder()
                    .setNumber(number)
                    .build();
            }))
        )
        .exec(bidirectionalStream.awaitStreamEnd((main, forked) -> {
            int latestMaximum = forked.getInt("maximum");
            return main.set("maximum", latestMaximum);
        }))
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

    // mvn gatling:test -Dgrpc.scenario=unary -Dgatling.simulationClass=io.gatling.grpc.demo.CalculatorSimulation
    // mvn gatling:test -Dgrpc.scenario=serverStreaming -Dgatling.simulationClass=io.gatling.grpc.demo.CalculatorSimulation
    // mvn gatling:test -Dgrpc.scenario=clientStreaming -Dgatling.simulationClass=io.gatling.grpc.demo.CalculatorSimulation
    // mvn gatling:test -Dgrpc.scenario=bidirectionalStreaming -Dgatling.simulationClass=io.gatling.grpc.demo.CalculatorSimulation
    // mvn gatling:test -Dgrpc.scenario=deadlines -Dgatling.simulationClass=io.gatling.grpc.demo.CalculatorSimulation

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
