package com.myMongoTest.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myMongoTest.document.Memo;
import com.myMongoTest.service.MemoExcelService;
import com.myMongoTest.service.MemoExcelService.ImportResult;
import com.myMongoTest.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 메모 엑셀 내보내기·가져오기. ADMIN, ADUSER만 접근.
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class MemoExcelController {

    private static final int MAX_IMPORT_FILE_SIZE_MB = 10;

    private final UserService userService;
    private final MemoExcelService memoExcelService;

    @GetMapping("/export/memos.xlsx")
    public ResponseEntity<InputStreamResource> exportMemos() throws IOException {
        List<Memo> memos = userService.mongoFindAllMemo();
        var categories = userService.mongoFindAllCategory();
        byte[] bytes = memoExcelService.exportToExcel(memos, categories);
        String filename = "메모내보내기_" + System.currentTimeMillis() + ".xlsx";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(bytes.length)
                .body(new InputStreamResource(new ByteArrayInputStream(bytes)));
    }

    @PostMapping("/import/memos")
    public String importMemos(@RequestParam("file") MultipartFile file,
                             RedirectAttributes ra) {
        if (file == null || file.isEmpty()) {
            ra.addFlashAttribute("importErrorMsg", "파일을 선택해 주세요.");
            return "redirect:/admin/inventory";
        }
        if (file.getSize() > MAX_IMPORT_FILE_SIZE_MB * 1024 * 1024) {
            ra.addFlashAttribute("importErrorMsg", "파일 크기는 " + MAX_IMPORT_FILE_SIZE_MB + "MB 이하여야 합니다.");
            return "redirect:/admin/inventory";
        }

        try (InputStream in = file.getInputStream()) {
            ImportResult result = memoExcelService.importFromExcel(in);
            StringBuilder msg = new StringBuilder();
            msg.append("메모 ").append(result.importedCount()).append("건");
            if (result.categoriesCreated() > 0) {
                msg.append(", 탭 ").append(result.categoriesCreated()).append("개");
            }
            msg.append(" 가져왔습니다.");
            ra.addFlashAttribute("importSuccessMsg", msg.toString());
            if (!result.errors().isEmpty()) {
                ra.addFlashAttribute("importWarnMsg", String.join(" / ", result.errors()));
            }
        } catch (IOException e) {
            log.warn("[엑셀 가져오기] 실패", e);
            ra.addFlashAttribute("importErrorMsg", "파일 읽기 실패: " + e.getMessage());
        }

        return "redirect:/admin/inventory";
    }
}
