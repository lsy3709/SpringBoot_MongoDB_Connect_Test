package com.myMongoTest.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

@SpringBootTest(classes = SpringCacheConfig.class)
@DisplayName("SpringCacheConfig 단위 테스트")
class SpringCacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("cacheManager 빈이 존재하고 CaffeineCacheManager 타입")
    void cacheManager_beanExists() {
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
    }

    @Test
    @DisplayName("카테고리 캐시 이름이 등록됨")
    void cacheManager_categoriesCacheRegistered() {
        CaffeineCacheManager caffeine = (CaffeineCacheManager) cacheManager;
        assertThat(caffeine.getCacheNames()).contains(SpringCacheConfig.CACHE_CATEGORIES);
    }

    @Test
    @DisplayName("categories 캐시에 put/get 정상 동작")
    void cacheManager_putAndGet() {
        var cache = cacheManager.getCache(SpringCacheConfig.CACHE_CATEGORIES);
        assertThat(cache).isNotNull();

        cache.put("key1", "value1");
        assertThat(cache.get("key1")).isNotNull();
        assertThat(cache.get("key1", String.class)).isEqualTo("value1");
    }

    @Test
    @DisplayName("상수 CACHE_CATEGORIES 값")
    void cacheCategories_constant() {
        assertThat(SpringCacheConfig.CACHE_CATEGORIES).isEqualTo("categories");
    }
}
