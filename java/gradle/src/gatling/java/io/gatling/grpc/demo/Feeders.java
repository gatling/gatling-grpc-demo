package io.gatling.grpc.demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.FeederBuilder;

import io.grpc.ChannelCredentials;
import io.grpc.TlsChannelCredentials;

public class Feeders {

    private static final List<ChannelCredentials> availableChannelCredentials = new ArrayList<>();

    static {
        try {
            for (int i = 1; i <= 3; i++) {
                ChannelCredentials channelCredentials = TlsChannelCredentials.newBuilder()
                        .keyManager(
                                ClassLoader.getSystemResourceAsStream("certs/client" + i + ".crt"),
                                ClassLoader.getSystemResourceAsStream("certs/client" + i + ".key"))
                        .trustManager(ClassLoader.getSystemResourceAsStream("certs/ca.crt"))
                        .build();
                availableChannelCredentials.add(channelCredentials);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FeederBuilder<Object> channelCredentials() {
        List<Map<String, Object>> records = availableChannelCredentials.stream()
                .map(channelCredentials -> Map.<String, Object>of("channelCredentials", channelCredentials))
                .toList();
        return CoreDsl.listFeeder(records);
    }

    private static String random(String alphabet, int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            int index = ThreadLocalRandom.current().nextInt(alphabet.length());
            s.append(alphabet.charAt(index));
        }
        return s.toString();
    }

    private static String randomString(int n) {
        return random("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", n);
    }

    public static Supplier<Iterator<Map<String, Object>>> randomNames() {
        return () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Map<String, Object> next() {
                return Map.of("firstName", randomString(20), "lastName", randomString(20));
            }
        };
    }
}
