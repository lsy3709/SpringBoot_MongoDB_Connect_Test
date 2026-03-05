package com.myMongoTest.config;

import com.myMongoTest.document.User2;
import com.myMongoTest.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 기동 시 기본 관리자 계정(admin / admin1234)이 없으면 생성한다.
 * AWS 등에서 MongoDB 준비 지연 시 재시도하며, 옵션으로 기존 admin 비밀번호 강제 리셋 가능.
 */
@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class DefaultAdminInitializer implements ApplicationRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin1234";
    private static final String DEFAULT_ADMIN_ROLE = "ADMIN";

    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 3000;

    @Value("${app.default-admin.force-reset-password:false}")
    private boolean forceResetPassword;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                doRun();
                return;
            } catch (Exception e) {
                log.warn("기본 관리자 초기화 시도 {} 실패: {} (MongoDB 미준비일 수 있음)", attempt, e.getMessage());
                if (attempt == MAX_RETRIES) {
                    log.error("기본 관리자 초기화 최종 실패. admin 로그인 불가. 수동으로 user2 컬렉션에 계정 추가하거나 재기동하세요.", e);
                    return;
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void doRun() {
        User2 existing = userService.mongoFindOneUser2Email(DEFAULT_ADMIN_EMAIL);
        String encodedPassword = passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD);

        if (existing != null) {
            if (forceResetPassword) {
                userService.mongoUser2UpdatePassword(DEFAULT_ADMIN_EMAIL, encodedPassword);
                log.info("기본 관리자 비밀번호 강제 리셋 완료: {} / {}", DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
            } else {
                log.debug("기본 관리자 계정이 이미 존재합니다. skip.");
            }
            return;
        }

        User2 admin = new User2();
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setPassword(encodedPassword);
        admin.setRole(DEFAULT_ADMIN_ROLE);
        userService.mongoUser2Insert(admin);
        log.info("기본 관리자 계정 생성 완료: {} / {}", DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
    }
}
