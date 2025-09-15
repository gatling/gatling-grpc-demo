package io.gatling.grpc.demo.server.calculator;

import java.io.IOException;
import java.security.cert.CertificateException;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalculatorServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorServer.class);

    public static void main(String[] args) throws CertificateException, IOException, InterruptedException {
        final var server = launch();
        server.awaitTermination();
    }

    public static Server launch() throws CertificateException, IOException {
        final var ssc = new SelfSignedCertificate("localhost");
        final var server = ServerBuilder.forPort(50052)
                .addService(new CalculatorServiceImpl())
                .useTransportSecurity(ssc.certificate(), ssc.privateKey())
                .build();
        server.start();
        LOGGER.info("CalculatorServer started, listening on {}", server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("CalculatorServer received shutdown request");
            server.shutdown();
            LOGGER.info("CalculatorServer successfully stopped");
        }));
        return server;
    }
}
