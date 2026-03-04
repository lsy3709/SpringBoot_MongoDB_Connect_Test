package com.myMongoTest.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Locale;

import com.myMongoTest.support.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 단위 테스트")
class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private WebRequest webRequest;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(messageSource);
    }

    @Test
    @DisplayName("IllegalArgumentException 시 400 + ErrorResponse 형식")
    void handleIllegalArgument_returns400AndBody() {
        ResponseEntity<ErrorResponse> result = handler.handleIllegalArgument(new IllegalArgumentException("invalid"));
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("invalid", result.getBody().getMessage());
        assertEquals(400, result.getBody().getStatus());
    }

    @Test
    @DisplayName("Exception 시 500 + 메시지 키 기반 메시지")
    void handleException_returns500AndBody() {
        when(webRequest.getLocale()).thenReturn(Locale.KOREAN);
        when(messageSource.getMessage(eq("error.server"), any(), any(Locale.class)))
                .thenReturn("서버 오류가 발생했습니다.");
        ResponseEntity<ErrorResponse> result = handler.handleException(new RuntimeException("unexpected"), webRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("서버 오류가 발생했습니다.", result.getBody().getMessage());
        assertEquals(500, result.getBody().getStatus());
    }
}
