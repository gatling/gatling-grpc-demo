package io.gatling.grpc.demo.server.greeting;

import java.io.IOException;

import io.gatling.grpc.demo.server.Configuration;

import io.grpc.*;

public class GreetingServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerCredentials credentials = Configuration.credentials();
        Server server = Grpc.newServerBuilderForPort(50051, credentials)
            .addService(new GreetingServiceImpl())
            .build();
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received shutdown request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));
        server.awaitTermination();
    }
}
