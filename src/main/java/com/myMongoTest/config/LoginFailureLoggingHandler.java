package com.myMongoTest.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 실패 시 원인을 로그로 남겨 디버깅용으로 사용.
 */
@Slf4j
@Component
public class LoginFailureLoggingHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) {
        String email = request.getParameter("email");
        log.warn("[로그인 실패] email={}, 예외={}, 메시지={}", email, exception.getClass().getSimpleName(), exception.getMessage());
        // 기존처럼 /login/error 로 리다이렉트
        try {
            response.sendRedirect(request.getContextPath() + "/login/error");
        } catch (Exception e) {
            log.error("리다이렉트 실패", e);
        }
    }
}
