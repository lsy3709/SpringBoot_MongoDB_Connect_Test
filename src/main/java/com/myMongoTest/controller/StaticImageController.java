package com.myMongoTest.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 정적 배경 이미지 제공 (Spring Boot 기본 정적 리소스가 로드되지 않는 환경 대비)
 */
@Controller
public class StaticImageController {

    @GetMapping("/image/balloon.jpg")
    public ResponseEntity<Resource> balloonImage() {
        Resource resource = new ClassPathResource("static/image/balloon.jpg");
        if (resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        }
        return ResponseEntity.notFound().build();
    }
}
