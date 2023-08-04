package io.gatling.grpc.demo.server.echo;

import io.grpc.*;

import java.io.IOException;

public class EchoServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server =
            ServerBuilder.forPort(50053)
                .addService(new EchoServiceImpl())
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
