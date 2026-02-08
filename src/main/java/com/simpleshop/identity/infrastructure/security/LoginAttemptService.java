package com.simpleshop.identity.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final String UNKNOWN_USER = "<unknown>";

    private final boolean enabled;
    private final int maxAttempts;
    private final Duration window;
    private final Map<String, Deque<Instant>> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> blockedUntil = new ConcurrentHashMap<>();

    public LoginAttemptService(
        @Value("${security.login-rate-limit.enabled:true}") boolean enabled,
        @Value("${security.login-rate-limit.max-attempts:5}") int maxAttempts,
        @Value("${security.login-rate-limit.window-minutes:15}") long windowMinutes
    ) {
        this.enabled = enabled;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.window = Duration.ofMinutes(Math.max(1, windowMinutes));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isBlocked(HttpServletRequest request) {
        if (!enabled) {
            return false;
        }

        String key = key(request);
        Instant now = Instant.now();
        Instant until = blockedUntil.get(key);
        if (until == null) {
            return false;
        }
        if (until.isAfter(now)) {
            return true;
        }
        blockedUntil.remove(key);
        return false;
    }

    public void recordFailure(HttpServletRequest request) {
        if (!enabled) {
            return;
        }

        String key = key(request);
        Instant now = Instant.now();
        Deque<Instant> attempts = failedAttempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        pruneOldAttempts(attempts, now);
        attempts.addLast(now);

        if (attempts.size() >= maxAttempts) {
            blockedUntil.put(key, now.plus(window));
            failedAttempts.remove(key);
        }
    }

    public void clearFailures(HttpServletRequest request) {
        if (!enabled) {
            return;
        }

        clearFailures(clientIp(request), request.getParameter("username"));
    }

    public void clearFailures(String clientIp, String username) {
        if (!enabled) {
            return;
        }

        String key = key(clientIp, username);
        failedAttempts.remove(key);
        blockedUntil.remove(key);
    }

    public void clearAll() {
        failedAttempts.clear();
        blockedUntil.clear();
    }

    private String key(HttpServletRequest request) {
        return key(clientIp(request), request.getParameter("username"));
    }

    private String key(String clientIp, String username) {
        String normalizedIp = (clientIp == null || clientIp.isBlank()) ? "unknown-ip" : clientIp.trim();
        String normalizedUsername = (username == null || username.isBlank())
            ? UNKNOWN_USER
            : username.trim().toLowerCase();
        return normalizedIp + ":" + normalizedUsername;
    }

    private void pruneOldAttempts(Deque<Instant> attempts, Instant now) {
        Instant threshold = now.minus(window);
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(threshold)) {
            attempts.removeFirst();
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
