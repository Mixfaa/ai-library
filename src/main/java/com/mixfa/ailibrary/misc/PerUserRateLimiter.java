package com.mixfa.ailibrary.misc;

import com.mixfa.ailibrary.misc.cache.CacheMaintainer;
import com.mixfa.ailibrary.misc.cache.MaintainableCache;
import com.mixfa.ailibrary.model.user.Account;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.apache.tomcat.util.collections.ManagedConcurrentWeakHashMap;

public class PerUserRateLimiter implements MaintainableCache {
    private final RateLimiterConfig rateLimiterConfig;
    private final ManagedConcurrentWeakHashMap<String, RateLimiter> rateLimiters = new ManagedConcurrentWeakHashMap<>();

    public PerUserRateLimiter(RateLimiterConfig rateLimiterConfig, CacheMaintainer cacheMaintainer) {
        this.rateLimiterConfig = rateLimiterConfig;
        cacheMaintainer.register(this);
    }

    @Override
    public void maintainCache() {
        rateLimiters.maintain();
    }

    public boolean acquirePermission() {
        var userID = Account.getAuthenticated().id();
        var limiter = rateLimiters.computeIfAbsent(userID, key -> RateLimiter.of("PerUserRateLimiter:" + key, rateLimiterConfig));

        return limiter.acquirePermission();
    }
}
