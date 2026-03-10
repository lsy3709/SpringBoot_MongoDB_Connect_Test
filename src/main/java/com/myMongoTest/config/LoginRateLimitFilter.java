package com.myMongoTest.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 로그인 시도 IP당 Rate Limiting. (분당 최대 N회 초과 시 429 반환)
 * /login POST 요청에만 적용.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    @Value("${app.login.rate-limit.max-per-minute:20}")
    private int maxPerMinute;

    /** key: clientIp, value: { count, windowStart } */
    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!"/login".equals(request.getRequestURI()) || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        AttemptInfo info = attempts.compute(ip, (k, v) -> {
            long now = System.currentTimeMillis();
            if (v == null || now - v.windowStart > 60_000) {
                return new AttemptInfo(1, now);
            }
            v.count++;
            return v;
        });

        if (info.count > maxPerMinute) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("로그인 시도 횟수 초과. 잠시 후 다시 시도하세요.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private static class AttemptInfo {
        int count;
        long windowStart;

        AttemptInfo(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}
