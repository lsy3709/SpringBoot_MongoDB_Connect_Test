package com.myMongoTest.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.myMongoTest.service.AsyncAuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 실패 시 원인을 로그로 남겨 디버깅용으로 사용.
 * 감사 로그는 비동기로 기록하여 응답 지연 최소화.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFailureLoggingHandler implements AuthenticationFailureHandler {

    private final AsyncAuditService asyncAuditService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) {
        String email = request.getParameter("email");
        asyncAuditService.logLoginFailureAsync(email, exception.getClass().getSimpleName(), exception.getMessage());
        // 기존처럼 /login/error 로 리다이렉트
        try {
            response.sendRedirect(request.getContextPath() + "/login/error");
        } catch (Exception e) {
            log.error("리다이렉트 실패", e);
        }
    }
}
