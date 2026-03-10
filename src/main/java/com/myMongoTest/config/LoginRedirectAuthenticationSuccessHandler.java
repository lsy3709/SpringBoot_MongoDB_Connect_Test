package com.myMongoTest.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 성공 후 세션 쿠키가 확실히 붙은 상태에서 목적지로 보내기 위해
 * 중간 경유 URL(/login/redirect)로 먼저 리다이렉트한 뒤, 해당 페이지에서 최종 목적지로 보낸다.
 * <p>역할별 기본 목적지: ADMIN → /admin, USER → / (403 방지)
 */
@Component
@Slf4j
public class LoginRedirectAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String REDIRECT_PATH = "/login/redirect";
    private static final String ADMIN_TARGET = "/admin";
    private static final String USER_TARGET = "/";
    private static final String TARGET_PARAM = "target";

    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        boolean isAdmin = hasAdminRole(authentication);

        String targetUrl = isAdmin ? ADMIN_TARGET : USER_TARGET;
        if (savedRequest != null && savedRequest.getRedirectUrl() != null && !savedRequest.getRedirectUrl().isEmpty()) {
            String saved = savedRequest.getRedirectUrl();
            // /admin 하위 경로는 ADMIN만 접근 가능 → USER면 / 로 대체
            if (saved.startsWith("/admin") && !isAdmin) {
                targetUrl = USER_TARGET;
            } else {
                targetUrl = saved;
            }
        }

        String redirectUrl = REDIRECT_PATH + "?" + TARGET_PARAM + "="
                + java.net.URLEncoder.encode(targetUrl, java.nio.charset.StandardCharsets.UTF_8);
        String username = authentication != null && authentication.getPrincipal() instanceof UserDetails ud
                ? ud.getUsername() : (authentication != null ? authentication.getPrincipal().toString() : null);
        log.info("[로그인 성공] username={}, role={}, redirectTarget={}", username, isAdmin ? "ADMIN" : "USER", targetUrl);
        response.sendRedirect(redirectUrl);
    }

    private boolean hasAdminRole(Authentication authentication) {
        if (authentication == null) return false;
        Collection<? extends GrantedAuthority> auths = authentication.getAuthorities();
        if (auths == null) return false;
        return auths.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
