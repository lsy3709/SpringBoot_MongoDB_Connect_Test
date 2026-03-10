package com.myMongoTest.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RegexEscape 단위 테스트")
class RegexEscapeTest {

    @Test
    void 일반_문자열_이스케이프() {
        String result = RegexEscape.toPartialMatchPattern("hello");
        assertThat(result).isEqualTo(".*\\Qhello\\E.*");
    }

    @Test
    void 정규식_특수문자_이스케이프() {
        // . * + ? [ ] ( ) { } | \ ^ $ 등이 리터럴로 취급되어야 함 (ReDoS 방지)
        String result = RegexEscape.toPartialMatchPattern("a.*+?[](){}|\\^$b");
        assertThat(result).contains("\\Q");
        assertThat(result).contains("\\E");
        // 검색어 내부가 \Q...\E로 감싸져 있어야 함
        assertThat(result).matches(".*\\\\Q.*\\\\E.*");
    }

    @Test
    void null_또는_blank_시_빈문자열() {
        assertThat(RegexEscape.toPartialMatchPattern(null)).isEmpty();
        assertThat(RegexEscape.toPartialMatchPattern("")).isEmpty();
        assertThat(RegexEscape.toPartialMatchPattern("   ")).isEmpty();
    }

    @Test
    void 공백_trim_후_처리() {
        String result = RegexEscape.toPartialMatchPattern("  test  ");
        assertThat(result).isEqualTo(".*\\Qtest\\E.*");
    }
}
