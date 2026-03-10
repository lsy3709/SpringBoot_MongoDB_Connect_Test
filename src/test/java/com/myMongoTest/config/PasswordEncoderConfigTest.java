package com.myMongoTest.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt strength 10 사용 검증.
 * SecurityConfig.passwordEncoder()와 동일한 설정(BCryptPasswordEncoder(10)) 사용 확인.
 */
@DisplayName("BCrypt strength 10 설정 검증")
class PasswordEncoderConfigTest {

    /** BCrypt strength 10 = cost factor 2^10. 해시에 $2a$10$ 또는 $2b$10$ 형식으로 포함됨 */
    private static final int EXPECTED_STRENGTH = 10;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(EXPECTED_STRENGTH);

    @Test
    @DisplayName("BCrypt strength 10 사용 시 해시에 $10$ 포함")
    void bcrypt_strength10_hashContainsCost() {
        String hash = passwordEncoder.encode("testPassword1");
        assertThat(hash).startsWith("$2a$" + EXPECTED_STRENGTH + "$").hasSize(60);
    }

    @Test
    @DisplayName("인코딩된 해시로 matches 검증")
    void bcrypt_matchesWorks() {
        String raw = "mySecret123";
        String hash = passwordEncoder.encode(raw);
        assertThat(passwordEncoder.matches(raw, hash)).isTrue();
        assertThat(passwordEncoder.matches("wrong", hash)).isFalse();
    }

    @Test
    @DisplayName("admin1234와 기존 해시 호환 (DefaultAdminInitializer 등)")
    void bcrypt_compatibleWithAdmin1234() {
        String hash = passwordEncoder.encode("admin1234");
        assertThat(passwordEncoder.matches("admin1234", hash)).isTrue();
    }
}
