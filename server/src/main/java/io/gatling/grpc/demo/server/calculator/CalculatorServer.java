package io.gatling.grpc.demo.server.calculator;

import io.grpc.*;

import java.io.IOException;

public class CalculatorServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server =
                ServerBuilder.forPort(50052)
                        .addService(new CalculatorServiceImpl())
                        .useTransportSecurity(
                                ClassLoader.getSystemResourceAsStream("ssl/server.crt"),
                                ClassLoader.getSystemResourceAsStream("ssl/server.pem"))
                        .build();
        server.start();
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    System.out.println("Received shutdown request");
                                    server.shutdown();
                                    System.out.println("Successfully stopped the server");
                                }));
        server.awaitTermination();
    }
}
