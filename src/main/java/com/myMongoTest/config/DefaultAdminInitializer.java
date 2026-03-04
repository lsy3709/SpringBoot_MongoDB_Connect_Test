package com.myMongoTest.config;

import com.myMongoTest.document.User2;
import com.myMongoTest.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 기동 시 기본 관리자 계정(admin / admin1234)이 없으면 생성한다.
 */
@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class DefaultAdminInitializer implements ApplicationRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin1234";
    private static final String DEFAULT_ADMIN_ROLE = "ADMIN";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userService.mongoFindOneUser2Email(DEFAULT_ADMIN_EMAIL) != null) {
            log.debug("기본 관리자 계정이 이미 존재합니다. skip.");
            return;
        }
        User2 admin = new User2();
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        admin.setRole(DEFAULT_ADMIN_ROLE);
        userService.mongoUser2Insert(admin);
        log.info("기본 관리자 계정 생성 완료: {} / {}", DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
    }
}
