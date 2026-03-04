package com.myMongoTest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


// CustomAuthenticationEntryPoint 임포트 경로가 맞는지 확인해 주세요.
// import com.myMongoTest.config.CustomAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 💡 참고: UserService는 현재 SecurityFilterChain 내에서 직접 사용되지 않으므로
    // 불필요한 @Autowired는 제거하는 것이 좋습니다. (UserDetailsService를 구현했다면 스프링이 알아서 찾습니다.)
    // @Autowired
    // UserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1. 로그인 및 로그아웃 설정 (람다식 사용)
        http
                .formLogin(form -> form
                        .loginPage("/login")                 // 사용자 정의 로그인 페이지 경로
                        .defaultSuccessUrl("/admin", true)   // 로그인 성공 시 이동할 기본 경로
                        .usernameParameter("email")          // 로그인 아이디 파라미터명 (기본값 username 대신 email 사용)
                        .failureUrl("/login/error")          // 로그인 실패 시 이동할 경로
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 훨씬 직관적이고 깔끔함
                        .logoutSuccessUrl("/")               // 로그아웃 성공 시 이동할 경로
                        .invalidateHttpSession(true)         // (추천) 로그아웃 시 세션 날리기
                );

        // 2. URL 경로별 권한 설정 (authorizeHttpRequests & requestMatchers 사용)
        http
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스(css, js, 이미지)는 권한 없이 누구나 접근 허용
                        .requestMatchers("/css/**", "/js/**", "/image/**", "/images/**").permitAll()
                        // 메인 페이지, 로그인/회원가입 관련 페이지 누구나 접근 허용
                        .requestMatchers("/", "/main", "/login", "/joinUser", "/joinForm", "/findAll", "/error").permitAll()
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