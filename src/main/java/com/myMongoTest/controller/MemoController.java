package com.myMongoTest.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.myMongoTest.DTO.MemoPageResponse;
import com.myMongoTest.DTO.SearchDB;
import com.myMongoTest.document.Memo;
import com.myMongoTest.service.ImageService;
import com.myMongoTest.service.StoredFileInfo;
import com.myMongoTest.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * 메모 CRUD·검색·첨부 이미지 전용 컨트롤러.
 */
@Controller
@RequiredArgsConstructor
public class MemoController {

	private final UserService userService;
	private final ImageService imageService;

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
		Memo memo = userService.mongoFindOneMemo(objectId);
		model.addAttribute("memo", memo);
		model.addAttribute("categories", userService.mongoFindAllCategory());
		return "updateForm";
	}

	@ResponseBody
	@DeleteMapping("/dbDelete/{id}/{imageFileName}")
	public String delete(@PathVariable String id, @PathVariable String imageFileName) {
		userService.deleteDb("_id", id);
		imageService.deleteImage(imageFileName);
		return id;
	}
}
