package com.myMongoTest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 설정. API 엔드포인트(/findAllMemo 등)에 대한 크로스 오리진 요청 허용 범위를 제한.
 * 프로덕션에서는 allowedOrigins를 실제 프론트 도메인으로 제한 권장.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(parseOrigins(allowedOrigins));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 정적 리소스·API 경로에만 CORS 적용 (폼 로그인은 same-origin 유지)
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> parseOrigins(String value) {
        if (value == null || value.isBlank() || "*".equals(value.trim())) {
            return List.of("*");
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
