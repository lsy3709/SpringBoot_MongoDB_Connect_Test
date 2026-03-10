package com.myMongoTest.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.myMongoTest.document.Memo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 메모 목록을 엑셀(xlsx)로 내보내기·가져오기.
 * 컬럼: 제목, 메모, 등록일, 유통기한, 태그, 카테고리ID, 이미지(셀에 임베드)
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MemoExcelService {

    private static final String SHEET_NAME = "메모";
    private static final int COL_TITLE = 0;
    private static final int COL_MESSAGE = 1;
    private static final int COL_DATE = 2;
    private static final int COL_EXPIRY = 3;
    private static final int COL_TAGS = 4;
    private static final int COL_CATEGORY = 5;
    private static final int COL_IMAGE = 6;
    private static final int IMAGE_COL_WIDTH = 20 * 256;
    private static final int IMAGE_ROW_HEIGHT = 120;
    private static final int MAX_IMPORT_ROWS = 1000;

    private final ImageService imageService;
    private final UserService userService;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 메모 목록을 엑셀 바이트로 생성. 이미지는 GridFS에서 조회해 셀에 삽입.
     */
    public byte[] exportToExcel(List<Memo> memos) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet(SHEET_NAME);
            CreationHelper helper = wb.getCreationHelper();
            Drawing<?> drawing = sheet.createDrawingPatriarch();

            // 헤더
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
                row.setHeightInPoints(IMAGE_ROW_HEIGHT / 15f);
                row.createCell(COL_TITLE).setCellValue(m.getTitle() != null ? m.getTitle() : "");
                row.createCell(COL_MESSAGE).setCellValue(m.getMessage() != null ? m.getMessage() : "");
                row.createCell(COL_DATE).setCellValue(m.getDateField() != null ? m.getDateField() : "");
                row.createCell(COL_EXPIRY).setCellValue(m.getExpiryDate() != null ? m.getExpiryDate() : "");
                row.createCell(COL_TAGS).setCellValue(tagsToString(m.getTags()));
                row.createCell(COL_CATEGORY).setCellValue(m.getCategoryId() != null ? m.getCategoryId() : "");

                if (m.getImageFileName() != null && !m.getImageFileName().isBlank()) {
                    byte[] imgBytes = imageService.getImageBytesByFilename(m.getImageFileName());
                    if (imgBytes != null && imgBytes.length > 0) {
                        int pictureIdx = wb.addPicture(imgBytes, Workbook.PICTURE_TYPE_JPEG);
                        ClientAnchor anchor = helper.createClientAnchor();
                        anchor.setCol1(COL_IMAGE);
                        anchor.setRow1(rowNum);
                        anchor.setCol2(COL_IMAGE + 1);
                        anchor.setRow2(rowNum + 1);
                        anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
                        drawing.createPicture(anchor, pictureIdx);
                    }
                }
                rowNum++;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 엑셀 스트림에서 메모 목록을 읽어 DB에 삽입. 이미지는 시트에서 추출해 GridFS에 저장.
     */
    public ImportResult importFromExcel(InputStream inputStream) throws IOException {
        Map<Integer, byte[]> rowImages = new HashMap<>();
        List<Memo> toInsert = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (XSSFWorkbook wb = new XSSFWorkbook(inputStream)) {
            XSSFSheet sheet = wb.getSheet(SHEET_NAME);
            if (sheet == null) {
                sheet = wb.getSheetAt(0);
            }

            // 시트에 임베드된 이미지 수집 (행 인덱스 -> 이미지 바이트)
            extractImagesByRow(sheet, rowImages);

            int lastRow = sheet.getLastRowNum();
            if (lastRow > MAX_IMPORT_ROWS) {
                errors.add("행 수가 " + MAX_IMPORT_ROWS + "를 초과합니다. 처음 " + MAX_IMPORT_ROWS + "행만 처리합니다.");
                lastRow = MAX_IMPORT_ROWS;
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
                memo.setCategoryId(getCellString(row, COL_CATEGORY));

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

        return new ImportResult(toInsert.size(), errors);
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

    public record ImportResult(int importedCount, List<String> errors) {}
}
