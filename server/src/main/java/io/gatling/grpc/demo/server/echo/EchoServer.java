package io.gatling.grpc.demo.server.echo;

import java.io.IOException;
import java.security.cert.CertificateException;

import io.grpc.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class EchoServer {
    public static void main(String[] args) throws IOException, InterruptedException, CertificateException {
        SelfSignedCertificate ssc = new SelfSignedCertificate("localhost");
        Server server = ServerBuilder.forPort(50053)
                .addService(new EchoServiceImpl())
                .useTransportSecurity(ssc.certificate(), ssc.privateKey())
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
