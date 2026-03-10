package com.myMongoTest.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.myMongoTest.document.Category;
import com.myMongoTest.document.Memo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 메모·탭(카테고리) 목록을 엑셀(xlsx)로 내보내기·가져오기.
 * 시트: "탭"(카테고리), "메모". 이미지는 셀 안에 맞춰 삽입.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MemoExcelService {

    /** 메모 시트명 */
    private static final String SHEET_MEMO = "메모";
    /** 탭(카테고리) 시트명 */
    private static final String SHEET_TABS = "탭";

    private static final int COL_TITLE = 0;
    private static final int COL_MESSAGE = 1;
    private static final int COL_DATE = 2;
    private static final int COL_EXPIRY = 3;
    private static final int COL_TAGS = 4;
    private static final int COL_CATEGORY = 5;
    private static final int COL_IMAGE = 6;
    /** 이미지 셀 열 너비 (문자 단위 × 256) */
    private static final int IMAGE_COL_WIDTH = 20 * 256;
    /** 이미지 행 높이 (포인트). 셀 안에 이미지가 맞도록 고정 */
    private static final float IMAGE_ROW_HEIGHT_POINTS = 80f;
    /** 셀 기준 픽셀(대략): 열 20문자 ≈ 140px, 행 80pt ≈ 107px */
    private static final double CELL_APPROX_WIDTH_PX = 140;
    private static final double CELL_APPROX_HEIGHT_PX = 107;
    private static final int MAX_IMPORT_ROWS = 1000;

    /** 탭 시트 컬럼: ID, 이름, 정렬순서 */
    private static final int TAB_COL_ID = 0;
    private static final int TAB_COL_NAME = 1;
    private static final int TAB_COL_SORT_ORDER = 2;

    private final ImageService imageService;
    private final UserService userService;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 메모·탭(카테고리) 목록을 엑셀 바이트로 생성.
     * 1) "메모" 시트: 제목, 메모, 등록일, 유통기한, 태그, 카테고리ID, 이미지(셀에 맞춰 임베드)
     * 2) "탭" 시트: ID, 이름, 정렬순서
     * 기본으로 "메모" 시트가 첫 번째/활성 탭으로 열리도록 설정.
     */
    public byte[] exportToExcel(List<Memo> memos, List<Category> categories) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            // 1. 메모 시트 (이미지 셀 맞춤)
            writeMemoSheet(wb, memos);

            // 2. 탭(카테고리) 시트
            writeTabsSheet(wb, categories != null ? categories : List.of());

            // 엑셀을 열었을 때 "메모" 시트가 기본으로 보이도록 설정
            int memoIdx = wb.getSheetIndex(SHEET_MEMO);
            if (memoIdx >= 0) {
                wb.setActiveSheet(memoIdx);
                wb.setSelectedTab(memoIdx);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    /** 탭 시트 작성: ID, 이름, 정렬순서 */
    private void writeTabsSheet(XSSFWorkbook wb, List<Category> categories) {
        XSSFSheet sheet = wb.createSheet(SHEET_TABS);
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(TAB_COL_ID).setCellValue("ID");
        headerRow.createCell(TAB_COL_NAME).setCellValue("이름");
        headerRow.createCell(TAB_COL_SORT_ORDER).setCellValue("정렬순서");

        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(TAB_COL_ID).setCellValue(c.getId() != null ? c.getId() : "");
            row.createCell(TAB_COL_NAME).setCellValue(c.getName() != null ? c.getName() : "");
            row.createCell(TAB_COL_SORT_ORDER).setCellValue(c.getSortOrder());
        }
    }

    /** 메모 시트 작성. 이미지는 한 셀에 맞춰 리사이즈 후 삽입 */
    private void writeMemoSheet(XSSFWorkbook wb, List<Memo> memos) throws IOException {
        XSSFSheet sheet = wb.createSheet(SHEET_MEMO);
        CreationHelper helper = wb.getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(COL_TITLE).setCellValue("제목");
        headerRow.createCell(COL_MESSAGE).setCellValue("메모");
        headerRow.createCell(COL_DATE).setCellValue("등록일");
        headerRow.createCell(COL_EXPIRY).setCellValue("유통기한");
        headerRow.createCell(COL_TAGS).setCellValue("태그");
        headerRow.createCell(COL_CATEGORY).setCellValue("카테고리ID");
        headerRow.createCell(COL_IMAGE).setCellValue("이미지");

        sheet.setColumnWidth(COL_IMAGE, IMAGE_COL_WIDTH);

        int rowNum = 1;
        for (Memo m : memos) {
            Row row = sheet.createRow(rowNum);
            row.setHeightInPoints(IMAGE_ROW_HEIGHT_POINTS);
            row.createCell(COL_TITLE).setCellValue(m.getTitle() != null ? m.getTitle() : "");
            row.createCell(COL_MESSAGE).setCellValue(m.getMessage() != null ? m.getMessage() : "");
            row.createCell(COL_DATE).setCellValue(m.getDateField() != null ? m.getDateField() : "");
            row.createCell(COL_EXPIRY).setCellValue(m.getExpiryDate() != null ? m.getExpiryDate() : "");
            row.createCell(COL_TAGS).setCellValue(tagsToString(m.getTags()));
            row.createCell(COL_CATEGORY).setCellValue(m.getCategoryId() != null ? m.getCategoryId() : "");

            if (m.getImageFileName() != null && !m.getImageFileName().isBlank()) {
                byte[] imgBytes = imageService.getImageBytesByFilename(m.getImageFileName());
                if (imgBytes != null && imgBytes.length > 0) {
                    insertImageFitInCell(wb, sheet, drawing, helper, imgBytes, rowNum);
                }
            }
            rowNum++;
        }
    }

    /** 이미지를 한 셀(COL_IMAGE, rowNum) 안에 맞춰 삽입.
     *  - 엑셀에서 셀 크기를 변경하면 이미지도 같이 이동·리사이즈되도록 MOVE_AND_RESIZE 사용.
     */
    private void insertImageFitInCell(Workbook wb, Sheet sheet, Drawing<?> drawing,
                                      CreationHelper helper, byte[] imgBytes, int rowNum) throws IOException {
        int pictureType = Workbook.PICTURE_TYPE_JPEG;
        if (imgBytes.length >= 4) {
            byte b0 = imgBytes[0], b1 = imgBytes[1], b2 = imgBytes[2];
            if (b0 == (byte) 0x89 && b1 == 'P' && b2 == 'N') pictureType = Workbook.PICTURE_TYPE_PNG;
        }
        int pictureIdx = wb.addPicture(imgBytes, pictureType);

        ClientAnchor anchor = helper.createClientAnchor();
        // 좌상단 기준으로 이미지 배치. 열/행 경계는 resize가 계산하도록 둔다.
        anchor.setCol1(COL_IMAGE);
        anchor.setRow1(rowNum);
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

        Picture picture = drawing.createPicture(anchor, pictureIdx);
        double scale = computeScaleToFitCell(imgBytes);
        picture.resize(scale);
    }

    /** 셀(CELL_APPROX_WIDTH_PX × CELL_APPROX_HEIGHT_PX) 안에 들어가도록 스케일 계산 */
    private double computeScaleToFitCell(byte[] imgBytes) throws IOException {
        try (InputStream in = new ByteArrayInputStream(imgBytes)) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) return 1.0;
            int w = img.getWidth();
            int h = img.getHeight();
            if (w <= 0 || h <= 0) return 1.0;
            double sx = CELL_APPROX_WIDTH_PX / w;
            double sy = CELL_APPROX_HEIGHT_PX / h;
            return Math.min(1.0, Math.min(sx, sy));
        }
    }

    /**
     * 엑셀 스트림에서 탭(카테고리)·메모를 읽어 DB에 삽입.
     * 1) "탭" 시트가 있으면 먼저 읽어 카테고리 새로 생성 (원본 ID → 새 ID 매핑)
     * 2) "메모" 시트에서 메모 읽고, 카테고리ID는 매핑된 새 ID로 치환 후 저장. 이미지는 GridFS에 저장.
     */
    public ImportResult importFromExcel(InputStream inputStream) throws IOException {
        Map<Integer, byte[]> rowImages = new HashMap<>();
        List<Memo> toInsert = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int categoriesCreated = 0;

        try (XSSFWorkbook wb = new XSSFWorkbook(inputStream)) {
            // 1. "탭" 시트가 있으면 먼저 처리 → 새 카테고리 생성, 원본 ID → 새 ID 매핑
            Map<String, String> oldCategoryIdToNewId = new HashMap<>();
            XSSFSheet tabsSheet = wb.getSheet(SHEET_TABS);
            if (tabsSheet != null) {
                categoriesCreated = importTabsSheet(tabsSheet, oldCategoryIdToNewId, errors);
            }

            // 2. "메모" 시트 처리
            XSSFSheet sheet = wb.getSheet(SHEET_MEMO);
            if (sheet == null) {
                sheet = wb.getSheetAt(0);
            }
            extractImagesByRow(sheet, rowImages);

            int lastRow = Math.min(sheet.getLastRowNum(), MAX_IMPORT_ROWS);
            if (sheet.getLastRowNum() > MAX_IMPORT_ROWS) {
                errors.add("메모 행 수가 " + MAX_IMPORT_ROWS + "를 초과합니다. 처음 " + MAX_IMPORT_ROWS + "행만 처리합니다.");
            }

            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String title = getCellString(row, COL_TITLE);
                if (title == null || title.isBlank()) continue;

                Memo memo = new Memo();
                memo.setTitle(title.trim());
                memo.setMessage(getCellString(row, COL_MESSAGE));
                memo.setDateField(getCellString(row, COL_DATE));
                if (memo.getDateField() == null || memo.getDateField().isBlank()) {
                    memo.setDateField(LocalDateTime.now().format(DATE_FORMAT));
                }
                memo.setExpiryDate(getCellString(row, COL_EXPIRY));
                memo.setTags(parseTags(getCellString(row, COL_TAGS)));

                String categoryIdFromExcel = getCellString(row, COL_CATEGORY);
                if (categoryIdFromExcel != null && !categoryIdFromExcel.isBlank()) {
                    String newId = oldCategoryIdToNewId.get(categoryIdFromExcel.trim());
                    memo.setCategoryId(newId != null ? newId : categoryIdFromExcel.trim());
                }

                byte[] imgBytes = rowImages.get(r);
                if (imgBytes != null && imgBytes.length > 0) {
                    try {
                        String filename = "import_" + System.currentTimeMillis() + "_" + r + ".jpg";
                        StoredFileInfo stored = imageService.storeFromBytes(imgBytes, filename);
                        if (stored != null) {
                            memo.setImageFileObjectId(stored.getObjectIdString());
                            memo.setImageFileName(stored.getFileName());
                        }
                    } catch (IOException e) {
                        log.warn("[엑셀 가져오기] 행 {} 이미지 저장 실패: {}", r + 1, e.getMessage());
                    }
                }

                try {
                    userService.mongoMemoInsert(memo);
                    toInsert.add(memo);
                } catch (Exception e) {
                    errors.add("행 " + (r + 1) + ": " + e.getMessage());
                }
            }
        }

        return new ImportResult(toInsert.size(), categoriesCreated, errors);
    }

    /** "탭" 시트 읽어 카테고리 생성. 원본 ID → 새 ID 매핑 저장. 생성된 개수 반환 */
    private int importTabsSheet(XSSFSheet tabsSheet, Map<String, String> oldIdToNewId, List<String> errors) {
        int count = 0;
        int lastRow = tabsSheet.getLastRowNum();
        for (int r = 1; r <= lastRow; r++) {
            Row row = tabsSheet.getRow(r);
            if (row == null) continue;
            String idFromExcel = getCellString(row, TAB_COL_ID);
            String name = getCellString(row, TAB_COL_NAME);
            if (name == null || name.isBlank()) continue;
            int sortOrder = 0;
            try {
                Cell soCell = row.getCell(TAB_COL_SORT_ORDER);
                if (soCell != null && soCell.getCellType() == CellType.NUMERIC) {
                    sortOrder = (int) soCell.getNumericCellValue();
                }
            } catch (Exception ignored) { }

            Category category = new Category();
            category.setName(name.trim());
            category.setSortOrder(sortOrder);
            try {
                userService.mongoCategoryInsert(category);
                String newId = category.getId();
                if (newId != null && idFromExcel != null && !idFromExcel.isBlank()) {
                    oldIdToNewId.put(idFromExcel.trim(), newId);
                }
                count++;
            } catch (Exception e) {
                errors.add("탭 행 " + (r + 1) + " (" + name + "): " + e.getMessage());
            }
        }
        return count;
    }

    private void extractImagesByRow(XSSFSheet sheet, Map<Integer, byte[]> rowImages) {
        if (sheet.getDrawingPatriarch() == null) return;
        XSSFDrawing drawing = (XSSFDrawing) sheet.getDrawingPatriarch();
        for (XSSFShape shape : drawing.getShapes()) {
            if (!(shape instanceof XSSFPicture)) continue;
            XSSFPicture pic = (XSSFPicture) shape;
            XSSFClientAnchor anchor = (XSSFClientAnchor) pic.getAnchor();
            int row = anchor.getRow1();
            if (pic.getPictureData() != null && pic.getPictureData().getData() != null) {
                rowImages.put(row, pic.getPictureData().getData());
            }
        }
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return nullToEmpty(cell.getStringCellValue());
        if (cell.getCellType() == CellType.NUMERIC) {
            double n = cell.getNumericCellValue();
            return (n == (long) n) ? String.valueOf((long) n) : String.valueOf(n);
        }
        return "";
    }

    private static String nullToEmpty(String s) {
        return s != null ? s.trim() : "";
    }

    private String tagsToString(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "";
        return String.join(",", tags);
    }

    private List<String> parseTags(String s) {
        if (s == null || s.isBlank()) return List.of();
        List<String> list = new ArrayList<>();
        for (String t : s.split(",")) {
            String trimmed = t.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        return list;
    }

    /** 가져오기 결과: 메모 건수, 새로 생성된 탭(카테고리) 수, 오류 메시지 목록 */
    public record ImportResult(int importedCount, int categoriesCreated, List<String> errors) {}
}
