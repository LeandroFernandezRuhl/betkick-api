package com.leandroruhl.betkickapi.service.utility;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CacheService is responsible for invalidating entries in a specific cache
 * named "footballDataCache." It provides methods to invalidate cache entries for a single key or a list of keys.
 */
@AllArgsConstructor
@Service
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;

    /**
     * Invalidates the cache entry for the specified key in the "footballDataCache."
     *
     * @param key The key whose cache entry needs to be invalidated.
     * @param <K> The type of the key.
     */
    public <K> void invalidateCacheForKey(K key) {
        Cache footballCache = cacheManager.getCache("footballDataCache");
        if (footballCache != null) {
            log.info("Key being invalidated: " + key);
            footballCache.evict(key);
        }
    }

    /**
     * Invalidates cache entries for the specified list of keys in the "footballDataCache."
     *
     * @param keys The list of keys whose cache entries need to be invalidated.
     * @param <K>  The type of the keys.
     */
    public <K> void invalidateCacheForKeys(List<K> keys) {
        Cache footballCache = cacheManager.getCache("footballDataCache");
        if (footballCache != null) {
            keys.forEach(key -> log.info("Key being invalidated: " + key));
            keys.forEach(footballCache::evict);
        }
    }
}
