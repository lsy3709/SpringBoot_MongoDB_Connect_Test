package com.myMongoTest.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginRateLimitFilter 단위 테스트")
class LoginRateLimitFilterTest {

    private LoginRateLimitFilter filter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new LoginRateLimitFilter();
        // 분당 2회로 제한해 테스트 용이
        ReflectionTestUtils.setField(filter, "maxPerMinute", 2);
    }

    @Test
    @DisplayName("GET /login 요청은 그대로 통과")
    void doFilter_GET_login_passesThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
        assertThat(response.getStatus()).isLessThan(400);
    }

    @Test
    @DisplayName("POST /other 경로는 그대로 통과")
    void doFilter_POST_otherPath_passesThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/other");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    @DisplayName("POST /login 허용 횟수 이내면 통과")
    void doFilter_POST_login_withinLimit_passesThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);
        filter.doFilter(request, response, filterChain);

        verify(filterChain, org.mockito.Mockito.times(2)).doFilter(any(), any());
        assertThat(response.getStatus()).isLessThan(400);
    }

    @Test
    @DisplayName("POST /login 허용 횟수 초과 시 429 반환")
    void doFilter_POST_login_exceedsLimit_returns429() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 3; i++) {
            filter.doFilter(request, response, filterChain);
        }

        verify(filterChain, org.mockito.Mockito.times(2)).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("로그인 시도 횟수 초과");
    }

    @Test
    @DisplayName("X-Forwarded-For 헤더가 있으면 첫 번째 IP로 식별")
    void doFilter_XForwardedFor_usesFirstIp() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.5, 70.41.3.18");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);
        filter.doFilter(request, response, filterChain);
        filter.doFilter(request, response, filterChain);

        verify(filterChain, org.mockito.Mockito.times(2)).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("서로 다른 IP는 각각 별도 카운트")
    void doFilter_differentIps_separateCount() throws ServletException, IOException {
        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/login");
            req.setRemoteAddr("10.0.0." + (100 + i));
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, filterChain);
        }

        verify(filterChain, org.mockito.Mockito.times(2)).doFilter(any(), any());
    }
}
