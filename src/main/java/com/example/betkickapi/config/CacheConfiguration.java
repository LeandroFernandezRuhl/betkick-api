package com.example.betkickapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up caching in the application using Caffeine.
 * <br>
 * <br>
 * This class is annotated with {@link Configuration} and {@link EnableCaching} to enable caching functionality.
 * It defines a {@link Bean} method to create and configure a {@link CaffeineCacheManager} named "footballDataCache"
 * as the cache manager for the application. The cache is configured using a {@link Caffeine} cache builder with
 * specific initial capacity and maximum size settings.
 * <br>
 * <br>
 * Caching is a technique used to store and retrieve frequently accessed data quickly, reducing the need to fetch
 * the data from the original source repeatedly.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * Creates and configures a {@link CaffeineCacheManager} named "footballDataCache".
     * <p>
     * This cache manager is set up with a {@link Caffeine} cache builder using specific initial capacity and maximum size settings.
     *
     * @return The configured {@link CacheManager} instance.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("footballDataCache");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Configures the {@link Caffeine} cache builder with specific initial capacity and maximum size settings.
     * <p>
     * The cache builder is designed to accommodate various caching needs within the application.
     * It includes settings for the initial capacity and maximum size of the cache.
     *
     * @return The configured {@link Caffeine} cache builder.
     */
    Caffeine<Object, Object> caffeineCacheBuilder() {
        // List of caches:
        // Each competition's matches list (12) + competitions list (1) + active competitions list (1) +
        // competitions with standings list (1) + non-finished matches list (1) + standings tables for each competition (12) +
        // user leaderboard (1) = 29
        return Caffeine.newBuilder()
                .initialCapacity(29)
                .maximumSize(29);
    }
}
