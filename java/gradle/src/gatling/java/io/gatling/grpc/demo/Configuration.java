package io.gatling.grpc.demo;

import io.gatling.javaapi.grpc.GrpcProtocolBuilder;

import static io.gatling.javaapi.grpc.GrpcDsl.grpc;

public class Configuration {

    private Configuration() {
        // Do nothing.
    }

    public static GrpcProtocolBuilder baseGrpcProtocol(String host, int port) {
        return grpc.forAddress(host, port).useInsecureTrustManager();
    }

    public static GrpcProtocolBuilder baseGrpcProtocolWithMutualAuth(String host, int port) {
        return grpc.forAddress(host, port)
            .channelCredentials("#{channelCredentials}")
            .overrideAuthority("gatling-grpc-demo-test-server");
    }
}
