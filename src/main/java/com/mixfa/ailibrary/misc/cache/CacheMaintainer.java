package com.mixfa.ailibrary.misc.cache;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

@Service
public class CacheMaintainer {
    private final List<WeakReference<MaintainableCache>> caches = new LinkedList<>();

    public void register(MaintainableCache byUserCache) {
        synchronized (caches) {
            caches.add(new WeakReference<>(byUserCache));
        }
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void maintain() {
        synchronized (caches) {
            var iterator = caches.iterator();
            while (iterator.hasNext()) {
                var ref = iterator.next();

                var element = ref.get();
                if (element == null) {
                    iterator.remove();
                    continue;
                }
                try {
                    element.maintainCache();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
