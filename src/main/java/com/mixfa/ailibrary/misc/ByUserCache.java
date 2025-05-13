package com.mixfa.ailibrary.misc;

import com.mixfa.ailibrary.model.user.Account;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ByUserCache<T> {
    private final Map<String, Map<Long, T>> cache = new ConcurrentHashMap<>();

    public Map<Long, T> getCache(String cacheName) {
        return cache.computeIfAbsent(cacheName, _ -> new ConcurrentHashMap<>());
    }

    public T getOrPut(String cacheName, Function<Long, T> supplier) {
        var cache = getCache(cacheName);
        var usersId = Account.getAuthenticated().id();

        return cache.computeIfAbsent(usersId, supplier);
    }
}
