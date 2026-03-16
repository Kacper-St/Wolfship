package com.example.backend.common.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Component
public class PasswordGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    private final SecureRandom random = new SecureRandom();

    public String generate(int length) {
        return random.ints(length, 0, CHARS.length())
                .mapToObj(CHARS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
