package com.myMongoTest.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("파일 미디어 타입 상수·확장자 검증")
class FileMediaTypeConstantsTest {

    @ParameterizedTest
    @ValueSource(strings = { "mp4", "mov", "MOV", "avi", "wmv", "webm", "mkv", "flv" })
    @DisplayName("동영상 확장자만 넘기면 true")
    void isVideoExtension_withVideoExtension_returnsTrue(String ext) {
        assertTrue(FileMediaTypeConstants.isVideoExtension(ext));
    }

    @ParameterizedTest
    @ValueSource(strings = { "file.mp4", "a.MOV", "path/to/video.avi" })
    @DisplayName("동영상 확장자 파일명이면 true")
    void isVideoExtension_withVideoFilename_returnsTrue(String filename) {
        assertTrue(FileMediaTypeConstants.isVideoExtension(filename));
    }

    @ParameterizedTest
    @ValueSource(strings = { "jpg", "jpeg", "png", "gif", "pdf", "txt", "" })
    @DisplayName("비동영상 확장자면 false")
    void isVideoExtension_withNonVideo_returnsFalse(String ext) {
        assertFalse(FileMediaTypeConstants.isVideoExtension(ext));
    }

    @Test
    @DisplayName("null 또는 blank면 false")
    void isVideoExtension_nullOrBlank_returnsFalse() {
        assertFalse(FileMediaTypeConstants.isVideoExtension(null));
        assertFalse(FileMediaTypeConstants.isVideoExtension(""));
        assertFalse(FileMediaTypeConstants.isVideoExtension("   "));
    }
}
