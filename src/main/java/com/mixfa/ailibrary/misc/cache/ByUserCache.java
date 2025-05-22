package com.mixfa.ailibrary.misc.cache;

import com.mixfa.ailibrary.model.user.Account;
import org.apache.tomcat.util.collections.ManagedConcurrentWeakHashMap;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


public class ByUserCache<T> implements MaintainableCache {
    private final Map<String, ManagedConcurrentWeakHashMap<String, T>> cache = new ConcurrentHashMap<>();

    public ByUserCache(CacheMaintainer maintainer) {
        Objects.requireNonNull(maintainer);
        maintainer.register(this);
    }

    @Override
    public void maintainCache() {
        for (var entry : cache.entrySet())
            entry.getValue().maintain();
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
