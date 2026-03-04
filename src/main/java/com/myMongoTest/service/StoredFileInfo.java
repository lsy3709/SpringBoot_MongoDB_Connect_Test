package com.myMongoTest.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GridFS에 저장한 파일의 식별 정보.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoredFileInfo {
    private String objectIdString;
    private String fileName;
}
