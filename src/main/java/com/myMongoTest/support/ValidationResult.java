package com.myMongoTest.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 검증 실패 시 메시지 키와 인자. MessageSource로 메시지 조회 시 사용.
 */
@Getter
@AllArgsConstructor
public class ValidationResult {
    private final String messageCode;
    private final Object[] args;

    public static ValidationResult of(String messageCode, Object... args) {
        return new ValidationResult(messageCode, args == null || args.length == 0 ? new Object[0] : args);
    }
}
