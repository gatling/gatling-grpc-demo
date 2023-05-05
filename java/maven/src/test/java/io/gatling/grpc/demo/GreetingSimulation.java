package io.gatling.grpc.demo;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.gatling.grpc.demo.greeting.*;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.grpc.GrpcProtocolBuilder;

import io.grpc.CallOptions;
import io.grpc.Status;

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

    // From https://gatling.io/docs/gatling/reference/current/http/websocket/
    // > If you want to deal with several WebSockets per virtual users, you have to give them a name
    // and pass this name on each ws operation:
    // ws("WS Operation").wsName("myCustomName");

    // We could do the same thing for rpcs but i guess it's easier to reuse a variable than to
    // reference a rpc using a string key

    // Should already type as SomethingAction?
//    GrpcUnaryServiceActionBuilder<GreetManyTimesRequest, GreetManyTimesResponse> serverStream =
//         grpc("Greet Many Times")
//                .serverStream(GreetServiceGrpc.getGreetManyTimesMethod());
//
//    // check sur receive? pour tester individuellement les réponses? wait until complete?
//
//        ScenarioBuilder serverStreaming = scenario("Greet Server Streaming")
//            .feed(csv("users.csv"))
//            .exec(session -> session.set("results", new ArrayList<>()))
//            .exec(
//                serverStream
//                    // start? startWith
//                    .payload(
//                        GreetManyTimesRequest.newBuilder()
//                            .setGreeting(
//                                Greeting.newBuilder()
//                                    .setFirstName("Guillaume")
//                                    .setLastName("Corré")
//                                    .build()
//                            )
//                            .build()
//                    )
//     .check(statusCode.is(Status.Code.OK))
//     .check(response().is(GreetManyTimesResponse.newBuilder().setResult("Hello ${firstName}
//     ${lastName}")).saveAs("greetResponse")))
//     from sessionCombiner to reconcile with ReconcilePolicy?, reconciliate changes too
//     .reconcile { (main, branch) =>
//       val results = main("results").as[List[String]]
//       val latestResult = branch("result").as[String]
//       main.set("results", results :+ latestResult)
//         .remove("result")
//     }
//     .timestampPolicy?
//     .include trailer?
//     .timestampExtractor { (session, message, t) =>
//       println(s"$t: $message")
//       //TimestampExtractor.IgnoreMessage
//       t
//     }
//            )
//        .repeat(10) {
//          exec(
//            serverStream.await(10, unit?).on(customCheck???) // handles StreamEnd/NextMessage
//     refine with awaitFor(NextMessage, 10, unit?)?
//        }
//     await(10.seconds).on(responseCheck1, responseCheck2, ...) waiting n times
//     or repeat(n).on(await(10.seconds)) with default check
//     or awaitUntilComplete with default check or all the check
//            .exec(session -> {
//                List<String> results = session.getList("results");
//                System.out.println("results: " + results);
//                return session;
//            });

    //    GrpcUnaryServiceActionBuilder<LongGreetRequest, LongGreetResponse> clientStream = grpc("Long
    // Greet")
    //        .clientStream(GreetServiceGrpc.getLongGreetMethod());

    //    ScenarioBuilder clientStreaming = scenario("Greet Client Streaming")
    //        .exec(session -> session
    /// clientStream
    // .start()
    //      .check(response()._.result.some)(_.saveAs("result"))
    //        )
    //    .repeat(10) {
    //      pause(100.milliseconds, 200.milliseconds)
    //        .exec(clientStream.send(
    //        LongGreetRequest(
    //            greeting = Some(Greeting(
    //              firstName = "Guillaume",
    //              lastName = "Corré"
    //            ))
    //          )
    //        })
    // trailer check possible here?
    //    }
    // awaitUntilEnd?
    //    .exec(clientStream.completeAndWait)
    //        .exec(session -> {
    //            String result = session.getString("result");
    //            System.out.println("result: " + result);
    //            return session;
    //        });

    //    GrpcUnaryServiceActionBuilder<GreetEveryoneRequest, GreetEveryoneResponse>
    // biDirectionalStream = grpc("Greet Everyone")
    //        .biDirectionalStream(GreetServiceGrpc.getGreetEveryoneMethod());

    //    ScenarioBuilder biDirectionalStreaming = scenario("Greet Bi Directional Streaming")
    //        .exec(session -> session.set("results", new ArrayList<String>()))
    //    .exec(
    //      biDirectionalStream
    //        .start() // no payload, use send?
    //        .check(statusCode.is(Status.Code.OK)) // end check
    //        .check(_.result.some)(_.saveAs("result")) // on every response

    // same as before
    // from sessionCombiner to reconcile with ReconcilePolicy?, reconciliate changes too
    // .reconcile { (main, branch) =>
    //   val results = main("results").as[List[String]]
    //   val latestResult = branch("result").as[String]
    //   main.set("results", results :+ latestResult)
    //     .remove("result")
    // }
    // .timestampPolicy?
    // .include trailer?
    // .timestampExtractor { (session, message, t) =>
    //   println(s"$t: $message")
    //   //TimestampExtractor.IgnoreMessage
    //   t
    // }
    //        .repeat(10).on(
    //            pause(Duration.ofMillis(100), Duration.ofMillis(200))
    //            .exec(biDirectionalStream.send(
    //              GreetEveryoneRequest(
    //                greeting = Some(Greeting(
    //                  firstName = "Guillaume",
    //                  lastName = "Corré"
    //                ))
    //              )
    //            ))
    //        )
    //    .exec(biDirectionalStream.copy(requestName = "Complete").complete)
    //        .exec(session -> {
    //            List<String> results = session.getList("results");
    //            System.out.println("results: " + results);
    //            return session;
    //        });

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
            scn.injectOpen(
                constantUsersPerSec(10).during(Duration.of(2, ChronoUnit.MINUTES))
            )
        ).protocols(baseGrpcProtocol);
    }
}
