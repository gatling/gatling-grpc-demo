package io.gatling.grpc.demo.server.calculator;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;

import io.grpc.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class CalculatorServer {
    public static void main(String[] args) throws CertificateException, IOException, InterruptedException {
        SelfSignedCertificate ssc = new SelfSignedCertificate("localhost");
        Server server = ServerBuilder.forPort(50052)
                .addService(new CalculatorServiceImpl())
                .useTransportSecurity(
                        new FileInputStream(ssc.certificate()),
                        new FileInputStream(ssc.privateKey())
                )
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
