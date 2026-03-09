package com.myMongoTest.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 미인증 사용자가 보호된 리소스 접근 시 401 대신 로그인 페이지로 리다이렉트.
 * 브라우저 환경에서 401이면 빈 오류 화면만 보이므로 /login으로 보내 재로그인 유도.
 */
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String LOGIN_PAGE = "/login";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.sendRedirect(request.getContextPath() + LOGIN_PAGE);
    }

}