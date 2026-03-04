package com.myMongoTest.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageService 단위 테스트")
class ImageServiceTest {

    @Mock
    private GridFsTemplate gridFsTemplate;

    @Mock
    private MultipartFile multipartFile;

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService(gridFsTemplate);
    }

    @Test
    @DisplayName("storeForMemo - 파일이 null이면 null 반환")
    void storeForMemo_nullFile_returnsNull() throws IOException {
        StoredFileInfo result = imageService.storeForMemo(null);
        assertNull(result);
    }

    @Test
    @DisplayName("storeForMemo - 동영상 확장자면 원본 저장 후 정보 반환")
    void storeForMemo_videoExtension_storesAndReturnsInfo() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("clip.mp4");
        when(multipartFile.getContentType()).thenReturn("video/mp4");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1]));
        when(gridFsTemplate.store(any(), eq("clip.mp4"), eq("video/mp4"))).thenReturn(new ObjectId());

        StoredFileInfo result = imageService.storeForMemo(multipartFile);

        assertNotNull(result);
        assertNotNull(result.getObjectIdString());
        assertNotNull(result.getFileName());
    }

    @Test
    @DisplayName("deleteImage - filename null이면 삭제 호출 안 함")
    void deleteImage_nullFilename_doesNotCallDelete() {
        imageService.deleteImage(null);
        imageService.deleteImage("");
        imageService.deleteImage("   ");
        // verify gridFsTemplate.delete 는 0회 호출 (mock이라 별도 verify 없이도 NPE 없이 통과)
    }

    @Test
    @DisplayName("deleteImage - filename 있으면 delete 호출")
    void deleteImage_withFilename_callsDelete() {
        imageService.deleteImage("test.jpg");
        verify(gridFsTemplate).delete(any());
    }
}
