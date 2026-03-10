package com.myMongoTest.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("비밀번호 정책 검증")
class PasswordPolicyValidatorTest {

    @Test
    @DisplayName("null 또는 빈 문자열이면 required 코드 반환")
    void validate_nullOrEmpty_returnsRequiredCode() {
        ValidationResult r1 = PasswordPolicyValidator.validate(null);
        ValidationResult r2 = PasswordPolicyValidator.validate("");
        assertNotNull(r1);
        assertNotNull(r2);
        assertEquals(PasswordPolicyValidator.CODE_REQUIRED, r1.getMessageCode());
    }

    @Test
    @DisplayName("8자 미만이면 minLength 코드·인자 반환")
    void validate_short_returnsMinLengthCode() {
        ValidationResult r = PasswordPolicyValidator.validate("1234567");
        assertNotNull(r);
        assertEquals(PasswordPolicyValidator.CODE_MIN_LENGTH, r.getMessageCode());
        assertNotNull(r.getArgs());
        assertEquals(PasswordPolicyValidator.MIN_LENGTH, r.getArgs()[0]);
    }

    @ParameterizedTest
    @ValueSource(strings = { "a1234567", "admin1234", "longpassword123" })
    @DisplayName("8자 이상·영문+숫자 포함이면 null 반환(통과)")
    void validate_valid_returnsNull(String password) {
        assertNull(PasswordPolicyValidator.validate(password));
    }

    @Test
    @DisplayName("숫자만 8자 이상이면 complexity 코드 반환")
    void validate_digitsOnly_returnsComplexityCode() {
        ValidationResult r = PasswordPolicyValidator.validate("12345678");
        assertNotNull(r);
        assertEquals(PasswordPolicyValidator.CODE_COMPLEXITY, r.getMessageCode());
    }

    @Test
    @DisplayName("영문만 8자 이상이면 complexity 코드 반환")
    void validate_lettersOnly_returnsComplexityCode() {
        ValidationResult r = PasswordPolicyValidator.validate("password");
        assertNotNull(r);
        assertEquals(PasswordPolicyValidator.CODE_COMPLEXITY, r.getMessageCode());
    }

    @Test
    @DisplayName("100자 초과면 maxLength 코드 반환")
    void validate_tooLong_returnsMaxLengthCode() {
        String longPassword = "a".repeat(PasswordPolicyValidator.MAX_LENGTH + 1);
        ValidationResult r = PasswordPolicyValidator.validate(longPassword);
        assertNotNull(r);
        assertEquals(PasswordPolicyValidator.CODE_MAX_LENGTH, r.getMessageCode());
        assertEquals(PasswordPolicyValidator.MAX_LENGTH, r.getArgs()[0]);
    }
}
