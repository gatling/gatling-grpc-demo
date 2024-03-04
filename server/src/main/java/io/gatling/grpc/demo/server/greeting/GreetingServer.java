package io.gatling.grpc.demo.server.greeting;

import java.io.IOException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;

import io.gatling.grpc.demo.server.Configuration;

import io.grpc.*;

public class GreetingServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerCredentials credentials = Configuration.credentials();
        Server server = Grpc.newServerBuilderForPort(50051, credentials)
                .addService(ServerInterceptors.intercept(new GreetingServiceImpl(), new MyInterceptor()))
                .build();
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received shutdown request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));
        server.awaitTermination();
    }

    public static class MyInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
            try {
                var sslSession = call.getAttributes().get(Grpc.TRANSPORT_ATTR_SSL_SESSION);
                var certificate = (X509Certificate) sslSession.getPeerCertificates()[0];
                System.out.println("Received greeting from: " + certificate.getSubjectX500Principal());
            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            }
            return next.startCall(call, headers);
        }
    }
}
