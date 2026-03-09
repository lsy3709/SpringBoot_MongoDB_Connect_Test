package com.myMongoTest.support;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * admin1234 의 BCrypt 해시를 출력. MongoDB에 평문이 들어갔을 때 한 번만 실행해 해시를 복사해 사용.
 * 사용: ./gradlew test --tests BcryptHashPrintTest.printAdmin1234Hash
 * 그 다음 MongoDB: db.user2.updateOne( { email: "admin" }, { $set: { password: "여기에_출력된_해시" } } )
 */
@Disabled("필요할 때만 @Disabled 제거 후 실행")
class BcryptHashPrintTest {

    @Test
    void printAdmin1234Hash() {
        String rawPassword = "admin1234";
        String hash = new BCryptPasswordEncoder().encode(rawPassword);
        System.out.println("=== MongoDB에 넣을 password 값 (admin1234 BCrypt 해시) ===");
        System.out.println(hash);
        System.out.println("=== MongoDB 쉘에서 실행 ===");
        System.out.println("db.user2.updateOne( { email: \"admin\" }, { $set: { password: \"" + hash + "\" } } )");
    }
}
