package com.myMongoTest.service;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.myMongoTest.config.AsyncConfig;

/**
 * AsyncAuditService 단위 테스트.
 */
@SpringBootTest(classes = { AsyncConfig.class, AsyncAuditService.class })
@TestPropertySource(properties = { "app.async.core-pool-size=1", "app.async.max-pool-size=2" })
@DisplayName("AsyncAuditService 단위 테스트")
class AsyncAuditServiceTest {

    @Autowired
    private AsyncAuditService asyncAuditService;

    @Test
    @DisplayName("logLoginFailureAsync 호출 시 예외 없이 반환")
    void logLoginFailureAsync_invoked_returnsImmediately() {
        assertThatCode(() ->
                asyncAuditService.logLoginFailureAsync("test@test.com", "BadCredentialsException", "invalid"))
                .doesNotThrowAnyException();
    }
}
