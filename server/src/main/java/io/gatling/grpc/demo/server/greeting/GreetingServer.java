package io.gatling.grpc.demo.server.greeting;

import java.io.IOException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;

import io.gatling.grpc.demo.server.Configuration;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreetingServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreetingServer.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        final var server = launch();
        server.awaitTermination();
    }

    public static Server launch() throws IOException {
        final var credentials = Configuration.credentials();
        final var server = Grpc.newServerBuilderForPort(50051, credentials)
                .addService(ServerInterceptors.intercept(new GreetingServiceImpl(), new MyInterceptor()))
                .build();
        server.start();
        LOGGER.info("GreetingServer started, listening on {}", server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("GreetingServer received shutdown request");
            server.shutdown();
            LOGGER.info("GreetingServer successfully stopped");
        }));
        return server;
    }

    public static class MyInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
            try {
                final var sslSession = call.getAttributes().get(Grpc.TRANSPORT_ATTR_SSL_SESSION);
                final var certificate = (X509Certificate) sslSession.getPeerCertificates()[0];
                LOGGER.trace("Received greeting from: {}", certificate.getSubjectX500Principal());
            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            }
            return next.startCall(call, headers);
        }
    }
}
