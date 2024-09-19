package io.gatling.grpc.demo;

import java.time.Duration;
import java.util.function.Function;

import io.gatling.grpc.demo.greeting.GreetRequest;
import io.gatling.grpc.demo.greeting.GreetResponse;
import io.gatling.grpc.demo.greeting.Greeting;
import io.gatling.grpc.demo.greeting.GreetingServiceGrpc;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.grpc.GrpcProtocolBuilder;

import io.grpc.Status;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.grpc.GrpcDsl.*;

public class GreetingSimulation extends Simulation {

    GrpcProtocolBuilder baseGrpcProtocol = grpc.forAddress("localhost", 50051)
            .channelCredentials("#{channelCredentials}")
            .overrideAuthority("gatling-grpc-demo-test-server");

    Function<Session, Greeting> greeting = session -> {
        String firstName = session.getString("firstName");
        String lastName = session.getString("lastName");
        return Greeting.newBuilder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .build();
    };

    ScenarioBuilder unary = scenario("Greet Unary")
            .feed(Feeders.channelCredentials().circular())
            .feed(Feeders.randomNames())
            .exec(grpc("Greet")
                    .unary(GreetingServiceGrpc.getGreetMethod())
                    .send(session -> GreetRequest.newBuilder()
                            .setGreeting(greeting.apply(session))
                            .build())
                    .check(
                            statusCode().is(Status.Code.OK),
                            response(GreetResponse::getResult).isEL("Hello #{firstName} #{lastName}")));

    ScenarioBuilder deadlines = scenario("Greet w/ Deadlines")
            .feed(Feeders.channelCredentials().circular())
            .feed(Feeders.randomNames())
            .exec(grpc("Greet w/ Deadlines")
                    .unary(GreetingServiceGrpc.getGreetWithDeadlineMethod())
                    .send(session -> GreetRequest.newBuilder()
                            .setGreeting(greeting.apply(session))
                            .build())
                    .deadlineAfter(Duration.ofMillis(100))
                    .check(statusCode().is(Status.Code.DEADLINE_EXCEEDED)));

    // ./gradlew -Dgrpc.scenario=unary gatlingRun --simulation io.gatling.grpc.demo.GreetingSimulation
    // ./gradlew -Dgrpc.scenario=deadlines gatlingRun --simulation io.gatling.grpc.demo.GreetingSimulation

    {
        String name = System.getProperty("grpc.scenario");
        ScenarioBuilder scn;
        if ("deadlines".equals(name)) {
            scn = deadlines;
        } else {
            scn = unary;
        }

        setUp(scn.injectOpen(atOnceUsers(5))).protocols(baseGrpcProtocol);
    }
}
