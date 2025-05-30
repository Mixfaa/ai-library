package com.mixfa.ailibrary.misc.cache;

import com.mixfa.ailibrary.model.user.Account;
import org.apache.tomcat.util.collections.ManagedConcurrentWeakHashMap;

import java.util.Objects;
import java.util.function.Function;

public class ByUserCache<T> implements MaintainableCache {
    private final ManagedConcurrentWeakHashMap<String, T> cache = new ManagedConcurrentWeakHashMap<>();

    public ByUserCache(CacheMaintainer maintainer) {
        Objects.requireNonNull(maintainer);
        maintainer.register(this);
    }

    @Override
    public void maintainCache() {
        cache.maintain();
    }

    public void evict(String cacheName) {
        var userId = Account.getAuthenticated().id();
        cache.remove(userId);
    }

    public void set(String userId, T value) {
        cache.put(userId, value);
    }

    public void set(T value) {
        var userId = Account.getAuthenticated().id();
        cache.put(userId, value);
    }

    public T getOrPut(Function<String, T> supplier) {
        var usersId = Account.getAuthenticated().id();
        return cache.computeIfAbsent(usersId, supplier);
    }
}
