package com.myMongoTest.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.myMongoTest.constant.FileMediaTypeConstants;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;

import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

@RequiredArgsConstructor
@Service
public class ImageService {

    /** 상세보기용 표준 이미지 최대 크기 (가로/세로) */
    private static final int STANDARD_MAX_WIDTH = 1280;
    private static final int STANDARD_MAX_HEIGHT = 1280;
    /** JPEG 품질 (0.0 ~ 1.0) */
    private static final float STANDARD_QUALITY = 0.8f;
    /** 저장 포맷: JPEG */
    private static final String IMAGE_FORMAT = "jpg";

    private final GridFsTemplate gridFsTemplate;

    /**
     * 메모 첨부용:
     * - 동영상: 원본 그대로 GridFS에 저장
     * - 이미지: 업로드 원본을 {@link #STANDARD_MAX_WIDTH} x {@link #STANDARD_MAX_HEIGHT} 이하로 리사이즈하고
     *   {@link #STANDARD_QUALITY} 품질의 JPEG로 압축해 GridFS에 저장 (목록·상세 공용으로 사용).
     * 파일이 없거나 파일명이 비어 있으면 null 반환.
     */
    public StoredFileInfo storeForMemo(MultipartFile file) throws IOException {
        if (file == null) {
            return null;
        }
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            return null;
        }
        if (FileMediaTypeConstants.isVideoExtension(filename)) {
            return storeFileAsIs(file);
        }
        return storeImageWithThumbnail(file);
    }

    /**
     * 원본 그대로 GridFS에 저장.
     */
    public StoredFileInfo storeFileAsIs(MultipartFile file) throws IOException {
        if (file == null || file.getOriginalFilename() == null) {
            return null;
        }
        try (InputStream in = file.getInputStream()) {
            ObjectId objectId = gridFsTemplate.store(in, file.getOriginalFilename(), file.getContentType());
            return new StoredFileInfo(objectId.toString(), file.getOriginalFilename());
        }
    }

    /**
     * 이미지를 표준 크기(상세보기용)로 리사이즈·압축 후 GridFS에 저장.
     * 저장 크기를 줄이기 위해 원본을 버리고 최적화된 JPEG만 남긴다.
     */
    public StoredFileInfo storeImageWithThumbnail(MultipartFile file) throws IOException {
        if (file == null || file.getOriginalFilename() == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(STANDARD_MAX_WIDTH, STANDARD_MAX_HEIGHT)
                .outputQuality(STANDARD_QUALITY)
                .outputFormat(IMAGE_FORMAT)
                .toOutputStream(out);
        try (InputStream in = new ByteArrayInputStream(out.toByteArray())) {
            ObjectId objectId = gridFsTemplate.store(in, file.getOriginalFilename(), "image/jpeg");
            return new StoredFileInfo(objectId.toString(), file.getOriginalFilename());
        }
    }

    public List<String> findAllFilenames() {
        GridFSFindIterable files = gridFsTemplate.find(new Query());
        List<String> filenames = new ArrayList<>();
        for (GridFSFile file : files) {
            filenames.add(file.getFilename());
        }
        return filenames;
    }

    /**
     * 파일명으로 GridFS에서 삭제. filename이 null/blank면 아무 작업 안 함.
     */
    public void deleteImage(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        Query query = Query.query(Criteria.where("filename").is(filename));
        gridFsTemplate.delete(query);
    }

    public void deleteImageByObjectId(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            return;
        }
        Query query = Query.query(Criteria.where("_id").is(objectId));
        gridFsTemplate.delete(query);
    }
}
