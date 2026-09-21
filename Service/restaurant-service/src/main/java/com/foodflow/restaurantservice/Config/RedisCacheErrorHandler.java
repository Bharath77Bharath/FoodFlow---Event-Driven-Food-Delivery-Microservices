package com.foodflow.restaurantservice.Config;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(
            RuntimeException exception,
            Cache cache,
            Object key
    ) {
        System.out.println(
                "Redis unavailable. Falling back to database for key: " + key
        );
    }

    @Override
    public void handleCachePutError(
            RuntimeException exception,
            Cache cache,
            Object key,
            Object value
    ) {
        System.out.println(
                "Redis unavailable. Could not cache key: " + key
        );
    }

    @Override
    public void handleCacheEvictError(
            RuntimeException exception,
            Cache cache,
            Object key
    ) {
        System.out.println(
                "Redis unavailable. Could not evict key: " + key
        );
    }

    @Override
    public void handleCacheClearError(
            RuntimeException exception,
            Cache cache
    ) {
        System.out.println(
                "Redis unavailable. Could not clear cache"
        );
    }
}