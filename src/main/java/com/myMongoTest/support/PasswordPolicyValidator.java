package com.myMongoTest.support;

/**
 * 회원가입·비밀번호 변경 시 비밀번호 정책 검증.
 * 반환값은 메시지 키(및 인자)이며, MessageSource로 실제 메시지 조회 시 사용.
 */
public final class PasswordPolicyValidator {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 100;

    /** 메시지 키 (messages.properties) */
    public static final String CODE_REQUIRED = "validation.password.required";
    public static final String CODE_MIN_LENGTH = "validation.password.minLength";
    public static final String CODE_MAX_LENGTH = "validation.password.maxLength";
    public static final String CODE_COMPLEXITY = "validation.password.complexity";

    private PasswordPolicyValidator() {}

    /**
     * 비밀번호가 정책을 만족하는지 검사.
     * @param password 검사할 비밀번호 (null 가능)
     * @return 정책 위반 시 메시지 키·인자(ValidationResult), 통과 시 null
     */
    public static ValidationResult validate(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.of(CODE_REQUIRED);
        }
        if (password.length() < MIN_LENGTH) {
            return ValidationResult.of(CODE_MIN_LENGTH, MIN_LENGTH);
        }
        if (password.length() > MAX_LENGTH) {
            return ValidationResult.of(CODE_MAX_LENGTH, MAX_LENGTH);
        }
        // 최소 1개 문자 + 1개 숫자
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            return ValidationResult.of(CODE_COMPLEXITY);
        }
        return null;
    }
}
