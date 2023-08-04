package io.gatling.grpc.demo.server.greeting;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GreetingServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50051)
                .addService(new GreetingServiceImpl())
                .useTransportSecurity(
                        ClassLoader.getSystemResourceAsStream("ssl/server.crt"),
                        ClassLoader.getSystemResourceAsStream("ssl/server.pem"))
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
