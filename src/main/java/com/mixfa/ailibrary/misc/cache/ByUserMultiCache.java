package com.mixfa.ailibrary.misc.cache;

import com.mixfa.ailibrary.model.user.Account;
import org.apache.tomcat.util.collections.ManagedConcurrentWeakHashMap;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


public class ByUserMultiCache<T> implements MaintainableCache {
    private final Map<String, ManagedConcurrentWeakHashMap<String, T>> cache = new ConcurrentHashMap<>();

    public ByUserMultiCache(CacheMaintainer maintainer) {
        Objects.requireNonNull(maintainer);
        maintainer.register(this);
    }

    @Override
    public void maintainCache() {
        for (var entry : cache.entrySet())
            entry.getValue().maintain();
    }

    public void evict(String cacheName) {
        var cache = getCache(cacheName);
        var userId = Account.getAuthenticated().id();

        cache.remove(userId);
    }

    public Map<String, T> getCache(String cacheName) {
        return cache.computeIfAbsent(cacheName, _ -> new ManagedConcurrentWeakHashMap<>());
    }

    public T getOrPut(String cacheName, Function<String, T> supplier) {
        var cache = getCache(cacheName);
        var usersId = Account.getAuthenticated().id();

        return cache.computeIfAbsent(usersId, supplier);
    }
}
