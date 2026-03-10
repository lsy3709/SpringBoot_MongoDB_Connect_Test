package com.myMongoTest.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

@SpringBootTest(classes = CorsConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=https://example.com,https://app.example.com")
@DisplayName("CorsConfig 단위 테스트")
class CorsConfigTest {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    @DisplayName("corsConfigurationSource 빈이 존재")
    void corsConfigurationSource_beanExists() {
        assertThat(corsConfigurationSource).isNotNull();
    }

    @Test
    @DisplayName("허용 origin이 설정 값과 일치")
    void corsConfig_allowedOrigins_matchProperties() {
        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(mockRequest("/api/test"));
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins())
                .containsExactlyInAnyOrder("https://example.com", "https://app.example.com");
    }

    @Test
    @DisplayName("허용 메서드에 GET, POST, PUT, DELETE, PATCH 포함")
    void corsConfig_allowedMethods() {
        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(mockRequest("/api/test"));
        assertThat(config.getAllowedMethods())
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "PATCH");
    }

    @Test
    @DisplayName("allowCredentials true")
    void corsConfig_allowCredentials() {
        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(mockRequest("/api/test"));
        assertThat(config.getAllowCredentials()).isTrue();
    }

    @Test
    @DisplayName("maxAge 3600")
    void corsConfig_maxAge() {
        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(mockRequest("/api/test"));
        assertThat(config.getMaxAge()).isEqualTo(3600L);
    }

    private HttpServletRequest mockRequest(String path) {
        HttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        ((org.springframework.mock.web.MockHttpServletRequest) request).setRequestURI(path);
        return request;
    }
}
