package com.example.betkickapi.service.utility;

import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CacheService {

    private final CacheManager cacheManager;

    public <K> void invalidateCacheForKey(K key) {
        Cache footballCache = cacheManager.getCache("footballDataCache");
        if (footballCache != null) {
            System.out.println("Key being invalidated: " + key);
            footballCache.evict(key);
        }
    }

    public <K> void invalidateCacheForKeys(List<K> keys) {
        Cache footballCache = cacheManager.getCache("footballDataCache");
        if (footballCache != null) {
            keys.forEach(key -> System.out.println("Key being invalidated: " + key));
            keys.forEach(footballCache::evict);
        }
    }
}