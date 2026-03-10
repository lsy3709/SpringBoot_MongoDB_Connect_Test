package com.myMongoTest.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import lombok.extern.slf4j.Slf4j;

/**
 * 미인증 사용자가 보호된 리소스 접근 시 401 대신 로그인 페이지로 리다이렉트.
 * 브라우저 환경에서 401이면 빈 오류 화면만 보이므로 /login으로 보내 재로그인 유도.
 * A09 Logging: 보안 이벤트(미인증 접근 시도) 로깅.
 */
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String LOGIN_PAGE = "/login";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.warn("[보안 이벤트] 미인증 접근 시도 URI={}, remoteAddr={}",
                request.getRequestURI(), request.getRemoteAddr());
        response.sendRedirect(request.getContextPath() + LOGIN_PAGE);
    }
}