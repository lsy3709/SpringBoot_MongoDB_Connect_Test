package com.myMongoTest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.myMongoTest.document.User2;
import com.myMongoTest.service.ImageService;
import com.myMongoTest.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.myMongoTest.config.LoginRedirectAuthenticationSuccessHandler;
import com.myMongoTest.config.SecurityConfig;

/**
 * UserController 단위 테스트 (MockMvc, 서비스 목).
 */
@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
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
    private LoginRedirectAuthenticationSuccessHandler loginSuccessHandler;

    @MockBean
    private MessageSource messageSource;

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
}
