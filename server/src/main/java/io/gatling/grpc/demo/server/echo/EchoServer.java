package io.gatling.grpc.demo.server.echo;

import java.io.IOException;
import java.security.cert.CertificateException;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EchoServer.class);

    public static void main(String[] args) throws IOException, InterruptedException, CertificateException {
        final var server = launch();
        server.awaitTermination();
    }

    public static Server launch() throws CertificateException, IOException {
        final var ssc = new SelfSignedCertificate("localhost");
        final var server = ServerBuilder.forPort(50053)
                .addService(new EchoServiceImpl())
                .useTransportSecurity(ssc.certificate(), ssc.privateKey())
                .build();
        server.start();
        LOGGER.info("EchoServer started, listening on {}", server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("EchoServer received shutdown request");
            server.shutdown();
            LOGGER.info("EchoServer successfully stopped");
        }));
        return server;
    }
}
