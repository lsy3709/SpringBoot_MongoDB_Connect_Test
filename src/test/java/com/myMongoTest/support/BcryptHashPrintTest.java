package com.myMongoTest.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 해시 생성·검증 단위 테스트.
 * printAdmin1234Hash: MongoDB에 평문이 들어갔을 때 해시 출력용 (필요 시 @Disabled 제거 후 실행).
 */
class BcryptHashPrintTest {

    private static final String DEFAULT_ADMIN_PASSWORD = "admin1234";

    @Test
    @DisplayName("admin1234 인코딩 시 유효한 BCrypt 해시 생성 및 matches 검증")
    void encode_admin1234_producesValidHashThatMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(DEFAULT_ADMIN_PASSWORD);

        assertThat(hash).isNotNull();
        assertThat(hash).startsWith("$2a$");
        assertThat(hash).hasSize(60);

        assertThat(encoder.matches(DEFAULT_ADMIN_PASSWORD, hash)).isTrue();
        assertThat(encoder.matches("wrong", hash)).isFalse();
    }

    @Test
    @DisplayName("동일 비밀번호도 솔트가 달라 서로 다른 해시 생성")
    void encode_samePassword_differentHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash1 = encoder.encode(DEFAULT_ADMIN_PASSWORD);
        String hash2 = encoder.encode(DEFAULT_ADMIN_PASSWORD);

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(encoder.matches(DEFAULT_ADMIN_PASSWORD, hash1)).isTrue();
        assertThat(encoder.matches(DEFAULT_ADMIN_PASSWORD, hash2)).isTrue();
    }

    @Test
    @DisplayName("DefaultAdminInitializer와 동일한 비밀번호로 생성한 해시 검증")
    void encode_matchesDefaultAdminInitializerPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(DEFAULT_ADMIN_PASSWORD);
        // Spring Security 로그인 시 matches(입력비밀번호, DB해시) 호출과 동일 조건
        assertThat(encoder.matches(DEFAULT_ADMIN_PASSWORD, hash)).isTrue();
    }

    /**
     * MongoDB에 넣을 BCrypt 해시 출력. build/admin1234-bcrypt-hash.txt 에도 저장됨.
     * 해시 확인: cat build/admin1234-bcrypt-hash.txt
     */
    @Test
    @DisplayName("admin1234 BCrypt 해시 출력 (MongoDB 수동 수정용)")
    void printAdmin1234Hash() throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(DEFAULT_ADMIN_PASSWORD);
        System.out.println("=== MongoDB에 넣을 password 값 (admin1234 BCrypt 해시) ===");
        System.out.println(hash);
        System.out.println("=== MongoDB 쉘에서 실행 ===");
        System.out.println("db.user2.updateOne( { email: \"admin\" }, { $set: { password: \"" + hash + "\" } } )");
        // Gradle에서 stdout이 안 보일 수 있어 파일로도 저장
        Path outDir = Paths.get("build").toAbsolutePath();
        Files.createDirectories(outDir);
        Path outFile = outDir.resolve("admin1234-bcrypt-hash.txt");
        Files.writeString(outFile, hash);
        System.out.println("=== 해시 파일 저장: " + outFile + " === (확인: cat build/admin1234-bcrypt-hash.txt)");
    }
}
