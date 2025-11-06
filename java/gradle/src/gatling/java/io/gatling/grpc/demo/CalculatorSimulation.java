package io.gatling.grpc.demo;

import java.util.concurrent.ThreadLocalRandom;

import io.gatling.grpc.demo.calculator.*;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.grpc.*;

import io.grpc.Status;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.grpc.GrpcDsl.*;

public class CalculatorSimulation extends Simulation {

    GrpcServerConfigurationBuilder calculatorServer =
            grpc.serverConfiguration("calculator").forAddress("localhost", 50052);

    GrpcProtocolBuilder baseGrpcProtocol = grpc.serverConfigurations(calculatorServer);

    ScenarioBuilder unary = scenario("Calculator Unary")
            .exec(grpc("Sum")
                    .unary(CalculatorServiceGrpc.getSumMethod())
                    .send(SumRequest.newBuilder()
                            .setFirstNumber(1)
                            .setSecondNumber(2)
                            .build())
                    .check(
                            statusCode().is(Status.Code.OK),
                            response(SumResponse::getSumResult).is(3)));

    GrpcServerStreamingServiceBuilder<PrimeNumberDecompositionRequest, PrimeNumberDecompositionResponse> serverStream =
            grpc("Prime Number Decomposition")
                    .serverStream(CalculatorServiceGrpc.getPrimeNumberDecompositionMethod())
                    .check(
                            statusCode().is(Status.Code.OK),
                            response(PrimeNumberDecompositionResponse::getPrimeFactor)
                                    .transform(p -> p == 2L || p == 5L || p == 17L || p == 97L || p == 6669961L)
                                    .is(true));

    ScenarioBuilder serverStreaming = scenario("Calculator Server Streaming")
            .exec(
                    serverStream.send(PrimeNumberDecompositionRequest.newBuilder()
                            .setNumber(109987656890L)
                            .build()),
                    serverStream.awaitStreamEnd());

    GrpcClientStreamingServiceBuilder<ComputeAverageRequest, ComputeAverageResponse> clientStream =
            grpc("Compute Average")
                    .clientStream(CalculatorServiceGrpc.getComputeAverageMethod())
                    .check(
                            statusCode().is(Status.Code.OK),
                            response(ComputeAverageResponse::getAverage).saveAs("average"));

    ScenarioBuilder clientStreaming = scenario("Calculator Client Streaming")
            .exec(
                    clientStream.start(),
                    repeat(10).on(clientStream.send(session -> {
                        int number = ThreadLocalRandom.current().nextInt(0, 1000);
                        return ComputeAverageRequest.newBuilder()
                                .setNumber(number)
                                .build();
                    })),
                    clientStream.halfClose(),
                    clientStream.awaitStreamEnd((main, forked) -> {
                        double average = forked.getDouble("average");
                        return main.set("average", average);
                    }),
                    exec(session -> {
                        double average = session.getDouble("average");
                        System.out.println("average: " + average);
                        return session;
                    }));

    GrpcBidirectionalStreamingServiceBuilder<FindMaximumRequest, FindMaximumResponse> bidirectionalStream =
            grpc("Find Maximum")
                    .bidiStream(CalculatorServiceGrpc.getFindMaximumMethod())
                    .check(
                            statusCode().is(Status.Code.OK),
                            response(FindMaximumResponse::getMaximum).saveAs("maximum"));

    ScenarioBuilder bidirectionalStreaming = scenario("Calculator Bidirectional Streaming")
            .exec(
                    bidirectionalStream.start(),
                    repeat(10).on(bidirectionalStream.send(session -> {
                        int number = ThreadLocalRandom.current().nextInt(0, 1000);
                        return FindMaximumRequest.newBuilder().setNumber(number).build();
                    })),
                    bidirectionalStream.halfClose(),
                    bidirectionalStream.awaitStreamEnd((main, forked) -> {
                        int latestMaximum = forked.getInt("maximum");
                        return main.set("maximum", latestMaximum);
                    }),
                    exec(session -> {
                        int maximum = session.getInt("maximum");
                        System.out.println("maximum: " + maximum);
                        return session;
                    }));

    ScenarioBuilder deadlines = scenario("Calculator w/ Deadlines")
            .exec(grpc("Square Root")
                    .unary(CalculatorServiceGrpc.getSquareRootMethod())
                    .send(SquareRootRequest.newBuilder().setNumber(-2).build())
                    .check(statusCode().is(Status.Code.INVALID_ARGUMENT)));

    // spotless:off
    // ./gradlew -Dgrpc.scenario=unary gatlingRun --simulation io.gatling.grpc.demo.CalculatorSimulation
    // ./gradlew -Dgrpc.scenario=serverStreaming gatlingRun --simulation io.gatling.grpc.demo.CalculatorSimulation
    // ./gradlew -Dgrpc.scenario=clientStreaming gatlingRun --simulation io.gatling.grpc.demo.CalculatorSimulation
    // ./gradlew -Dgrpc.scenario=bidirectionalStreaming gatlingRun --simulation io.gatling.grpc.demo.CalculatorSimulation
    // ./gradlew -Dgrpc.scenario=deadlines gatlingRun --simulation io.gatling.grpc.demo.CalculatorSimulation
    // spotless:on

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

        setUp(scn.injectOpen(atOnceUsers(1))).protocols(baseGrpcProtocol);
    }
}
