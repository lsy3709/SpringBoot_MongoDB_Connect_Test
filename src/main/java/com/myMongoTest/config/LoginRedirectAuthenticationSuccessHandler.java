package com.myMongoTest.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 성공 후 바로 302로 /admin을 보내면, 일부 환경에서 세션 쿠키가 따라가지 않아
 * 첫 요청에서 인증 실패가 난다. 이를 피하기 위해 중간 경유 URL(/login/redirect)로 보낸 뒤,
 * 해당 페이지에서 다시 /admin(또는 원래 요청 URL)으로 리다이렉트한다.
 */
@Component
@Slf4j
public class LoginRedirectAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String REDIRECT_PATH = "/login/redirect";
    private static final String DEFAULT_TARGET = "/admin";
    private static final String TARGET_PARAM = "target";

    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        String targetUrl = DEFAULT_TARGET;
        if (savedRequest != null && savedRequest.getRedirectUrl() != null && !savedRequest.getRedirectUrl().isEmpty()) {
            targetUrl = savedRequest.getRedirectUrl();
        }
        String redirectUrl = REDIRECT_PATH + "?" + TARGET_PARAM + "=" + java.net.URLEncoder.encode(targetUrl, java.nio.charset.StandardCharsets.UTF_8);
        String username = null;
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal != null) {
            username = principal.toString();
        }
        log.info("[로그인 성공] username={}, redirectTarget={}, redirectUrl={}", username, targetUrl, redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
