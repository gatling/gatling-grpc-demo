package io.gatling.grpc.demo.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import io.grpc.ServerCredentials;
import io.grpc.TlsServerCredentials;

public class Configuration {
    private static InputStream getSystemResourceAsStream(String resource) {
        return Objects.requireNonNull(
            ClassLoader.getSystemResourceAsStream(resource));
    }
    private static InputStream certificateAuthority() {
        return getSystemResourceAsStream("certs/ca.crt");
    }
    public static InputStream serverCertificate() {
        return getSystemResourceAsStream("certs/server.crt");
    }
    public static InputStream serverPrivateKey() {
        return getSystemResourceAsStream("certs/server.key");
    }
    public static ServerCredentials credentials() throws IOException {
        return TlsServerCredentials.newBuilder()
            .keyManager(serverCertificate(), serverPrivateKey())
            .trustManager(certificateAuthority())
            .clientAuth(TlsServerCredentials.ClientAuth.REQUIRE)
            .build();
    }
}
