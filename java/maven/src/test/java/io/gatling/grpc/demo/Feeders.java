package io.gatling.grpc.demo;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class Feeders {

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

    public static Supplier<Iterator<Map<String, Object>>> groups() {
        return () -> new Iterator<>() {
            int i = 1;
            @Override
            public boolean hasNext() {
                return true;
            }
            @Override
            public Map<String, Object> next() {
                return Map.of("groupName", (i++) % 100);
            }
        };
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
