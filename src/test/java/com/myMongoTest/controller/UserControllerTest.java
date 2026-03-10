package com.myMongoTest.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.myMongoTest.config.LoginFailureLoggingHandler;
import com.myMongoTest.config.LoginRateLimitFilter;
import com.myMongoTest.config.LoginRedirectAuthenticationSuccessHandler;
import com.myMongoTest.config.SecurityConfig;
import com.myMongoTest.document.User2;
import com.myMongoTest.service.AsyncAuditService;
import com.myMongoTest.service.ImageService;
import com.myMongoTest.service.UserService;

/**
 * UserController 단위 테스트 (MockMvc, 서비스 목).
 */
@WebMvcTest(controllers = UserController.class)
@Import({ SecurityConfig.class, LoginRedirectAuthenticationSuccessHandler.class,
        LoginFailureLoggingHandler.class, LoginRateLimitFilter.class })
@DisplayName("UserController 단위 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private ImageService imageService;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private AsyncAuditService asyncAuditService;

    @Test
    @DisplayName("GET /joinForm -> joinForm 뷰")
    void joinForm_returnsView() throws Exception {
        mockMvc.perform(get("/joinForm"))
                .andExpect(status().isOk())
                .andExpect(view().name("joinForm"));
    }

    @Test
    @DisplayName("GET /login -> loginForm 뷰")
    void login_returnsView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("loginForm"));
    }

    @Test
    @DisplayName("POST /joinUser 중복 이메일 -> redirect /joinForm")
    void joinUser_duplicateEmail_redirectsToJoinForm() throws Exception {
        when(userService.mongoFindOneUser2Email(anyString())).thenReturn(new User2());

        mockMvc.perform(post("/joinUser")
                        .param("email", "dup@test.com")
                        .param("password", "password1234")
                        .param("role", "USER")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/joinForm"));
    }

    @Test
    @DisplayName("POST /joinUser 성공 -> redirect /login")
    void joinUser_success_redirectsToLogin() throws Exception {
        when(userService.mongoFindOneUser2Email(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        mockMvc.perform(post("/joinUser")
                        .param("email", "new@test.com")
                        .param("password", "password1234")
                        .param("role", "USER")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("로그아웃 후 admin/admin1234 재로그인 정상 동작")
    void logout_thenRelogin_succeeds() throws Exception {
        UserDetails admin = User.builder()
                .username("admin")
                .password("encoded")
                .roles("ADMIN")
                .build();
        when(userService.loadUserByUsername(eq("admin"))).thenReturn(admin);
        when(passwordEncoder.matches(eq("admin1234"), eq("encoded"))).thenReturn(true);

        MockHttpSession session = new MockHttpSession();
        // 1) 로그인
        mockMvc.perform(post("/login")
                        .param("email", "admin")
                        .param("password", "admin1234")
                        .with(csrf())
                        .session(session))
                .andExpect(status().is3xxRedirection());

        // 2) 로그아웃
        mockMvc.perform(post("/logout").with(csrf()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));

        // 3) 재로그인 (새 세션으로 시도)
        mockMvc.perform(post("/login")
                        .param("email", "admin")
                        .param("password", "admin1234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
