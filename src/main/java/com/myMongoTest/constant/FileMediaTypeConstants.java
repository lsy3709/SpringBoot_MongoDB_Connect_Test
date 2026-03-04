package com.myMongoTest.constant;

import java.util.Set;

/**
 * 업로드 파일의 미디어 타입(동영상/이미지 등) 구분용 확장자 상수.
 * 동영상은 썸네일 생성 없이 원본 저장, 이미지는 썸네일 생성 후 저장하는 등 로직 분기에 사용.
 */
public final class FileMediaTypeConstants {

    private FileMediaTypeConstants() {}

    /** 동영상으로 취급하는 확장자 (소문자). 썸네일 미생성, 원본만 저장. */
    public static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "mov", "avi", "wmv", "webm", "mkv", "flv"
    );

    /**
     * 파일명(또는 확장자)이 동영상 확장자인지 여부.
     * @param filenameOrExtension 파일명(예: "a.MOV") 또는 확장자만(예: "mov")
     * @return 동영상 확장자이면 true
     */
    public static boolean isVideoExtension(String filenameOrExtension) {
        if (filenameOrExtension == null || filenameOrExtension.isBlank()) {
            return false;
        }
        String ext = filenameOrExtension;
        int lastDot = filenameOrExtension.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < filenameOrExtension.length() - 1) {
            ext = filenameOrExtension.substring(lastDot + 1);
        }
        return VIDEO_EXTENSIONS.contains(ext.toLowerCase());
    }
}
