package com.myMongoTest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.myMongoTest.service.UserService;

/**
 * SecurityConfig의 rememberMe에서 사용할 UserDetailsService 빈을 명시적으로 등록.
 * Qualifier로 주입 시 테스트에서 Mock 교체가 쉽도록 함.
 */
@Configuration
public class UserDetailsServiceConfig {

    @Bean("userDetailsServiceForSecurity")
    public UserDetailsService userDetailsServiceForSecurity(UserService userService) {
        return userService;
    }
}
