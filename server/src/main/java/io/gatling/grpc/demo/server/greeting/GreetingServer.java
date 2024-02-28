package io.gatling.grpc.demo.server.greeting;

import java.io.File;
import java.io.IOException;

import io.grpc.Grpc;
import io.grpc.Server;
import io.grpc.TlsServerCredentials;

public class GreetingServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        // If only providing a private key, you can use TlsServerCredentials.create() instead of
        // interacting with the Builder.
        var certsFolder = new File("/Users/guillaumegaly/Documents/Workspaces/grpc/gatling-grpc-demo/certs");
        var caCert = new File(certsFolder, "ca.pem");
        var serverCert = new File(certsFolder, "server1.pem");
        var serverPrivateKey = new File(certsFolder, "server1.key");

        var serverCredentials = TlsServerCredentials.newBuilder()
                .keyManager(serverCert, serverPrivateKey)
                .trustManager(caCert)
                .clientAuth(TlsServerCredentials.ClientAuth.REQUIRE)
                .build();

        Server server = Grpc.newServerBuilderForPort(50051, serverCredentials)
                .addService(new GreetingServiceImpl())
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
