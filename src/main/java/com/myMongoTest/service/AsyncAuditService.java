package com.myMongoTest.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 비동기 감사 로깅. 로그인 실패 등 non-blocking 로그 기록.
 */
@Slf4j
@Service
public class AsyncAuditService {

    /**
     * 로그인 실패 이벤트 비동기 로깅 (request 처리 흐름과 분리).
     */
    @Async("taskExecutor")
    public void logLoginFailureAsync(String email, String exceptionType, String message) {
        log.warn("[비동기 감사] 로그인 실패 email={}, 예외={}, 메시지={}", email, exceptionType, message);
    }
}
