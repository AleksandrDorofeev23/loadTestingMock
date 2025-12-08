package org.example;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MockApiController {
    private final MeterRegistry meterRegistry;
    private final Map<String, String> users = new HashMap<>();
    private final Map<String, String> tokens = new HashMap<>();

    public MockApiController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestParam String username, @RequestParam String password) {
        users.put(username, password);
        meterRegistry.counter("api.requests", "endpoint", "register").increment();
        return Map.of("message", "User registered successfully");
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String username, @RequestParam String password) {
        if (users.containsKey(username) && users.get(username).equals(password)) {
            String token = JwtUtil.generateToken(username);
            tokens.put(token, username);
            meterRegistry.counter("api.requests", "endpoint", "login").increment();
            return Map.of("token", token);
        }
        return Map.of("error", "Invalid credentials");
    }

    @GetMapping("/user")
    public Map<String, String> getUser(@RequestHeader("Authorization") String token) {
        long startTime = System.currentTimeMillis();
        meterRegistry.counter("api.requests", "endpoint", "user").increment();

        if (tokens.containsKey(token)) {
            String username = tokens.get(token);
            long duration = System.currentTimeMillis() - startTime;
            meterRegistry.timer("api.response.time", "endpoint", "user").record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
            return Map.of("username", username, "full_name", "John Doe");
        }
        return Map.of("error", "Invalid token");
    }
}