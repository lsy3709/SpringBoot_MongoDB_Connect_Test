package com.myMongoTest.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;

/**
 * CustomAuthenticationEntryPoint 단위 테스트.
 * A09 보안 이벤트 로깅 동작 검증.
 */
@DisplayName("CustomAuthenticationEntryPoint 단위 테스트")
class CustomAuthenticationEntryPointTest {

    private CustomAuthenticationEntryPoint entryPoint;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private AuthenticationException authException;

    @BeforeEach
    void setUp() {
        entryPoint = new CustomAuthenticationEntryPoint();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        authException = mock(AuthenticationException.class);

        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/findAllMemo");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
    }

    @Test
    @DisplayName("commence 호출 시 /login으로 리다이렉트")
    void commence_sendsRedirectToLogin() throws Exception {
        assertThatCode(() -> entryPoint.commence(request, response, authException))
                .doesNotThrowAnyException();

        verify(response).sendRedirect("/login");
    }

    @Test
    @DisplayName("contextPath 있으면 리다이렉트 경로에 포함")
    void commence_includesContextPath() throws Exception {
        when(request.getContextPath()).thenReturn("/app");

        entryPoint.commence(request, response, authException);

        verify(response).sendRedirect("/app/login");
    }
}
