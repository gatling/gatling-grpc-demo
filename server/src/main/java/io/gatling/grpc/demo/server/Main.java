package io.gatling.grpc.demo.server;

import java.io.IOException;
import java.security.cert.CertificateException;

import io.gatling.grpc.demo.server.calculator.CalculatorServer;
import io.gatling.grpc.demo.server.echo.EchoServer;
import io.gatling.grpc.demo.server.greeting.GreetingServer;

public class Main {
    public static void main(String[] args) throws IOException, CertificateException, InterruptedException {
        final var greeting = GreetingServer.launch();
        final var calculator = CalculatorServer.launch();
        final var echo = EchoServer.launch();

        greeting.awaitTermination();
        calculator.awaitTermination();
        echo.awaitTermination();
    }
}
