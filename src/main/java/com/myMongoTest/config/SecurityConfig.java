package com.myMongoTest.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final LoginRedirectAuthenticationSuccessHandler loginSuccessHandler;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(LoginRedirectAuthenticationSuccessHandler loginSuccessHandler,
                          @Qualifier("userDetailsServiceForSecurity") UserDetailsService userDetailsService) {
        this.loginSuccessHandler = loginSuccessHandler;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 0. CSRF 설정 — Spring Security 6.x 지연(deferred) 토큰 문제 방지
        //    CsrfTokenRequestAttributeHandler: 토큰을 즉시 로드하여 Thymeleaf 폼에서 확실히 사용 가능
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                        .csrfTokenRequestHandler(csrfHandler)
                );

        // 1. 로그인 및 로그아웃 설정 (성공 시 중간 경유 페이지 사용으로 리다이렉트 오류 방지)
        http
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler)
                        .usernameParameter("email")
                        .failureUrl("/login/error")
                )
                .rememberMe(rm -> rm
                        .key("smart-inventory-remember-me")
                        .tokenValiditySeconds(60 * 60 * 24 * 14)  // 14일
                        .userDetailsService(userDetailsService)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("remember-me")  // 자동 로그인 쿠키 삭제
                );

        // 2. URL 경로별 권한 설정 (authorizeHttpRequests & requestMatchers 사용)
        http
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스(css, js, 이미지)는 권한 없이 누구나 접근 허용
                        .requestMatchers("/css/**", "/js/**", "/image/**", "/images/**").permitAll()
                        // 메인 페이지, 로그인/회원가입 관련 페이지 누구나 접근 허용
                        .requestMatchers("/", "/main", "/login", "/login/error", "/joinUser", "/joinForm", "/error").permitAll()
                        // 로그인 성공 후 세션 확정을 위한 중간 리다이렉트 페이지 (인증 필요)
                        .requestMatchers("/login/redirect").authenticated()
                        // /admin 경로는 "ADMIN" 권한(역할)을 가진 사용자만 접근 가능
                        .requestMatchers("/admin").hasRole("ADMIN")
                        // 그 외의 모든 요청은 로그인(인증)한 사용자만 접근 가능
                        .anyRequest().authenticated()
                );

        // 3. 예외 처리 설정
        http
                .exceptionHandling(exception -> exception
                        // 인증되지 않은 사용자가 보호된 리소스에 접근할 때 처리할 커스텀 클래스 지정
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                );

        return http.build();
    }

    // 비밀번호 암호화를 위한 인코더 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}