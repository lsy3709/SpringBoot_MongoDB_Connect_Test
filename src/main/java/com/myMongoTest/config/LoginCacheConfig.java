package com.myMongoTest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

/**
 * 로그인 페이지는 캐시하지 않도록 설정.
 * 로그아웃 후 재방문 시 새 CSRF 토큰을 담은 폼을 받도록 하여 403 방지.
 */
@Configuration
public class LoginCacheConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebContentInterceptor interceptor = new WebContentInterceptor();
        interceptor.addCacheMapping(CacheControl.noStore().mustRevalidate(), "/login", "/login/error");
        registry.addInterceptor(interceptor).addPathPatterns("/login", "/login/error");
    }
}
