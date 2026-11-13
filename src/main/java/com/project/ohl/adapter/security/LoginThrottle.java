package com.project.ohl.adapter.security;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LoginThrottle {
    private record Attempts(int count, Instant until) {}
    private final Map<String, Attempts> attempts = new HashMap<>();

    public synchronized void check(String key) {
        reserve(key, 5);
    }

    public synchronized void checkRegistration(String address) {
        reserve("registration:" + address, 30);
    }

    private void reserve(String key, int limit) {
        attempts.entrySet().removeIf(entry -> entry.getValue().until().isBefore(Instant.now()));
        var value = attempts.get(key);
        if (attempts.size() >= 10000 || value != null && value.count() >= limit)
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Try again in 15 minutes.");
        attempts.put(key, new Attempts(value == null ? 1 : value.count() + 1,
                value == null ? Instant.now().plusSeconds(900) : value.until()));
    }

    public synchronized void succeeded(String key) { attempts.remove(key); }
}
