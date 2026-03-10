package com.myMongoTest.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.myMongoTest.controller.UserController;
import com.myMongoTest.service.AsyncAuditService;
import com.myMongoTest.service.ImageService;
import com.myMongoTest.service.UserService;

/**
 * OWASP A01 Broken Access Control 점검.
 * /admin 등 역할 기반 접근 제어 검증.
 */
@WebMvcTest(controllers = UserController.class)
@Import({ SecurityConfig.class, LoginRedirectAuthenticationSuccessHandler.class,
        LoginFailureLoggingHandler.class, LoginRateLimitFilter.class })
@DisplayName("접근 제어(A01) 점검")
class SecurityAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ImageService imageService;

    @MockBean
    private AsyncAuditService asyncAuditService;

    @Test
    @DisplayName("/admin — ADMIN 역할 없으면 403")
    void admin_withoutAdminRole_returns403() throws Exception {
        mockMvc.perform(get("/admin").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/admin — ADMIN 역할 있으면 200")
    void admin_withAdminRole_returns200() throws Exception {
        mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/findAllMemo — 인증 없으면 302 리다이렉트")
    void findAllMemo_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/findAllMemo"))
                .andExpect(status().is3xxRedirection());
    }
}
