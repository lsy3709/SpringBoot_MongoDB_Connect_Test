package com.myMongoTest.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.myMongoTest.DTO.MemoPageResponse;
import com.myMongoTest.DTO.SearchDB;
import com.myMongoTest.document.Memo;
import com.myMongoTest.service.ImageService;
import com.myMongoTest.service.ParallelFetchService;
import com.myMongoTest.service.StoredFileInfo;
import com.myMongoTest.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.Data;

/**
 * 메모 CRUD·검색·첨부 이미지 전용 컨트롤러.
 */
@Controller
@RequiredArgsConstructor
public class MemoController {

	private final UserService userService;
	private final ImageService imageService;
	private final ParallelFetchService parallelFetchService;

	@ResponseBody
	@PostMapping("/insertMemoWithImage")
	public ResponseEntity<String> insertMemoWithImage(@RequestPart(value = "key") Memo memo,
	                                                   @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
		memo.setDateField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		StoredFileInfo stored = imageService.storeForMemo(file);
		if (stored != null) {
			memo.setImageFileObjectId(stored.getObjectIdString());
			memo.setImageFileName(stored.getFileName());
		}
		userService.mongoMemoInsert(memo);
		return ResponseEntity.ok().build();
	}

	@ResponseBody
	@PostMapping("/insertMemo")
	public ResponseEntity<String> insertMemo(@RequestBody Memo memo) {
		memo.setDateField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		userService.mongoMemoInsert(memo);
		return ResponseEntity.ok("success");
	}

	@ResponseBody
	@PostMapping("/updateMemo")
	public ResponseEntity<String> updateMemo(@RequestBody Memo memo) {
		userService.mongoMemoUpdate(memo);
		return ResponseEntity.ok("success");
	}

	@ResponseBody
	@PostMapping("/updateWithMemo")
	public ResponseEntity<String> updateWithMemo(@RequestPart(value = "key") Memo memo,
	                                             @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
		ObjectId objectId2 = new ObjectId(memo.getId());
		Memo loadMemo = userService.mongoFindOneMemo(objectId2);

		if (file != null) {
			imageService.deleteImage(loadMemo.getImageFileName());
			StoredFileInfo stored = imageService.storeForMemo(file);
			if (stored != null) {
				memo.setImageFileObjectId(stored.getObjectIdString());
				memo.setImageFileName(stored.getFileName());
			}
		} else {
			memo.setImageFileName(loadMemo.getImageFileName());
		}
		userService.mongoMemoUpdate(memo);
		return ResponseEntity.ok("success");
	}

	@ResponseBody
	@GetMapping("/findAllMemo")
	public List<Memo> listMemo() {
		return userService.mongoFindAllMemo();
	}

	/**
	 * Pageable 기반 전체 목록 (offset 페이지네이션).
	 */
	@ResponseBody
	@GetMapping("/findAllMemoPageable")
	public Page<Memo> listMemoPageable(@RequestParam(required = false) String categoryId,
	                                   @RequestParam(defaultValue = "0") int page,
	                                   @RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, Math.min(size, 50));
		return userService.mongoFindMemoPage(categoryId, pageable);
	}

	/**
	 * 커서 기반 전체 목록 페이지네이션 (무한 스크롤용).
	 */
	@ResponseBody
	@GetMapping("/findAllMemoPage")
	public MemoPageResponse listMemoPage(@RequestParam(required = false) String categoryId,
	                                      @RequestParam(required = false) String lastId,
	                                      @RequestParam(defaultValue = "10") int limit) {
		return userService.mongoFindMemoCursor(categoryId, lastId, Math.min(limit, 50));
	}

	@ResponseBody
	@PostMapping("/searchDb")
	public List<Memo> searchList(@RequestBody SearchDB searchDB) {
		return userService.mongoSearchFindAll(searchDB);
	}

	/**
	 * 커서 기반 검색 목록 페이지네이션 (무한 스크롤용).
	 */
	@ResponseBody
	@PostMapping("/searchDbPage")
	public MemoPageResponse searchListPage(@RequestBody SearchDB searchDB,
	                                        @RequestParam(required = false) String lastId,
	                                        @RequestParam(defaultValue = "10") int limit) {
		String catId = searchDB != null ? searchDB.getCategoryId() : null;
		return userService.mongoSearchMemoCursor(searchDB, catId, lastId, Math.min(limit, 50));
	}

	@RequestMapping("/updateFormMemo/{id}")
	public String updateFormMemo(Model model, @PathVariable String id) {
		ObjectId objectId = new ObjectId(id);
		ParallelFetchService.MemoAndCategories result = parallelFetchService.fetchMemoAndCategories(objectId);
		model.addAttribute("memo", result.memo());
		model.addAttribute("categories", result.categories());
		return "updateForm";
	}

	@ResponseBody
	@DeleteMapping("/dbDelete/{id}/{imageFileName}")
	public String delete(@PathVariable String id, @PathVariable String imageFileName) {
		userService.deleteDb("_id", id);
		imageService.deleteImage(imageFileName);
		return id;
	}

	/**
	 * 관리자 화면(인벤토리 전체 목록)에서 선택한 메모들을 일괄 삭제.
	 * - 요청 바디에 id, imageFileName 목록을 JSON으로 전달.
	 * - 각 항목마다 개별 삭제 수행 (실패한 항목은 로그만 남기고 계속 진행).
	 */
	@ResponseBody
	@PostMapping("/dbDeleteBatch")
	public String deleteBatch(@RequestBody DeleteBatchRequest request) {
		if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
			return "0";
		}
		int success = 0;
		for (DeleteItem item : request.getItems()) {
			if (item == null || item.getId() == null || item.getId().isBlank()) {
				continue;
			}
			try {
				userService.deleteDb("_id", item.getId());
				if (item.getImageFileName() != null && !item.getImageFileName().isBlank()) {
					imageService.deleteImage(item.getImageFileName());
				}
				success++;
			} catch (Exception e) {
				// 삭제 실패 시에도 다른 항목 처리는 계속 진행
			}
		}
		return String.valueOf(success);
	}

	/**
	 * 일괄 삭제 요청용 DTO.
	 */
	@Data
	public static class DeleteBatchRequest {
		private List<DeleteItem> items;
	}

	@Data
	public static class DeleteItem {
		private String id;
		private String imageFileName;
	}
}
