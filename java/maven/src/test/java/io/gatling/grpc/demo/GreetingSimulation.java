package io.gatling.grpc.demo;

import io.gatling.grpc.demo.greeting.*;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.grpc.*;
import io.grpc.CallOptions;
import io.grpc.Status;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.grpc.GrpcDsl.*;

public class GreetingSimulation extends Simulation {

    GrpcProtocolBuilder baseGrpcProtocol =
        Configuration.baseGrpcProtocol("localhost", 50051);

    Function<Session, Greeting> greeting = session -> {
        String firstName = session.getString("firstName");
        String lastName = session.getString("lastName");
        return Greeting.newBuilder()
            .setFirstName(firstName)
            .setLastName(lastName)
            .build();
    };

    ScenarioBuilder unary = scenario("Greet Unary")
        .feed(Feeders.randomNames())
        .repeat(10).on(
            exec(
                grpc("Greet")
                    .unary(GreetingServiceGrpc.getGreetMethod())
                    .send(session ->
                        GreetRequest.newBuilder()
                            .setGreeting(greeting.apply(session))
                            .build()
                    ).check(
                        status().is(Status.Code.OK),
                        response(GreetResponse::getResult).isEL("Hello #{firstName} #{lastName}")
                    )
            )
        );

    ScenarioBuilder deadlines = scenario("Greet w/ Deadlines")
        .feed(Feeders.randomNames())
        .exec(
            grpc("Greet w/ Deadlines")
                .unary(GreetingServiceGrpc.getGreetWithDeadlineMethod())
                .send(session ->
                    GreetRequest.newBuilder()
                        .setGreeting(greeting.apply(session))
                        .build()
                )
                .callOptions(CallOptions.DEFAULT.withDeadlineAfter(100, TimeUnit.MILLISECONDS))
                .check(
                    status().is(Status.Code.DEADLINE_EXCEEDED)
                )
        );

    {
        String name = System.getProperty("grpc.scenario");
        ScenarioBuilder scn;
        if ("deadlines".equals(name)) {
            scn = deadlines;
        } else {
            scn = unary;
        }

        setUp(
            scn.injectOpen(
                atOnceUsers(1)
            )
        ).protocols(baseGrpcProtocol);
    }
}