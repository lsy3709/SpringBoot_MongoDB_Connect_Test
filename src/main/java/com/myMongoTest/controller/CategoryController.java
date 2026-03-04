package com.myMongoTest.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.myMongoTest.document.Category;
import com.myMongoTest.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * 카테고리(탭) CRUD API.
 */
@Controller
@RequiredArgsConstructor
public class CategoryController {

	private final UserService userService;

	@ResponseBody
	@GetMapping("/categories")
	public List<Category> listCategories() {
		return userService.mongoFindAllCategory();
	}

	@ResponseBody
	@PostMapping("/categories")
	public ResponseEntity<Category> createCategory(@RequestBody Category category) {
		if (category.getSortOrder() == 0) {
			List<Category> list = userService.mongoFindAllCategory();
			category.setSortOrder(list.isEmpty() ? 0 : list.get(list.size() - 1).getSortOrder() + 1);
		}
		userService.mongoCategoryInsert(category);
		return ResponseEntity.ok(category);
	}

	@ResponseBody
	@PutMapping("/categories/{id}")
	public ResponseEntity<String> updateCategory(@PathVariable String id, @RequestBody Category category) {
		category.setId(id);
		userService.mongoCategoryUpdate(category);
		return ResponseEntity.ok("success");
	}

	@ResponseBody
	@DeleteMapping("/categories/{id}")
	public ResponseEntity<String> deleteCategory(@PathVariable String id) {
		userService.mongoCategoryDelete(id);
		return ResponseEntity.ok(id);
	}
}
