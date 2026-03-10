package com.myMongoTest.support;

import java.util.regex.Pattern;

/**
 * MongoDB regex 검색 시 ReDoS·NoSQL Injection 방지를 위한 유틸.
 * 사용자 입력을 정규식 패턴으로 쓰기 전에 이스케이프 처리.
 */
public final class RegexEscape {

    private RegexEscape() {}

    /**
     * 부분 일치(LIKE '%값%')용 정규식 패턴으로 변환.
     * 특수문자를 이스케이프하여 ReDoS·injection 방지.
     */
    public static String toPartialMatchPattern(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return "";
        }
        String escaped = Pattern.quote(userInput.trim());
        return ".*" + escaped + ".*";
    }
}
