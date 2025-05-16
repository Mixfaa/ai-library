package com.mixfa.ailibrary.misc;

import com.mixfa.ailibrary.model.user.Account;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class PerUserRateLimiter {
    private final RateLimiterConfig rateLimiterConfig;
    private final Map<Long, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    public boolean acquirePermission() {
        var userID = Account.getAuthenticated().id();
        var limiter = rateLimiters.computeIfAbsent(userID, key -> RateLimiter.of("rateLimiter:" + key, rateLimiterConfig));

        return limiter.acquirePermission();
    }
}
