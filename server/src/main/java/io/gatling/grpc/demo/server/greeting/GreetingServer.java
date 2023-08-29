package io.gatling.grpc.demo.server.greeting;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class GreetingServer {
    public static void main(String[] args) throws CertificateException, IOException, InterruptedException {
        SelfSignedCertificate ssc = new SelfSignedCertificate("localhost");
        Server server = ServerBuilder.forPort(50051)
                .addService(new GreetingServiceImpl())
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
