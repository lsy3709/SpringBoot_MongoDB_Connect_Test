package com.myMongoTest.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Spring Cache 설정. Caffeine 사용.
 * 카테고리 목록 등 변경 빈도가 낮은 조회에 적용.
 */
@Configuration
@EnableCaching
public class SpringCacheConfig {

    public static final String CACHE_CATEGORIES = "categories";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CACHE_CATEGORIES);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(10, TimeUnit.MINUTES));
        return cacheManager;
    }
}
