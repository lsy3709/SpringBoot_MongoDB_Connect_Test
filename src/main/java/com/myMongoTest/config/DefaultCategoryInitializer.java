package com.myMongoTest.config;

import com.myMongoTest.document.Category;
import com.myMongoTest.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 기동 시 기본 카테고리 "냉장고" 생성 및
 * categoryId가 없는 기존 메모에 해당 categoryId 부여 (마이그레이션).
 */
@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class DefaultCategoryInitializer implements ApplicationRunner {

	private static final String DEFAULT_CATEGORY_NAME = "냉장고";

	private final UserService userService;

	@Override
	public void run(ApplicationArguments args) {
		var categories = userService.mongoFindAllCategory();
		String fridgeId;
		if (categories.isEmpty()) {
			Category fridge = new Category();
			fridge.setName(DEFAULT_CATEGORY_NAME);
			fridge.setSortOrder(0);
			userService.mongoCategoryInsert(fridge);
			fridgeId = fridge.getId();
			log.info("기본 카테고리 생성: {}", DEFAULT_CATEGORY_NAME);
		} else {
			fridgeId = categories.stream()
					.filter(c -> DEFAULT_CATEGORY_NAME.equals(c.getName()))
					.findFirst()
					.map(Category::getId)
					.orElse(categories.get(0).getId());
		}
		long migrated = userService.mongoMigrateMemoCategoryId(fridgeId);
		if (migrated > 0) {
			log.info("기존 메모에 기본 categoryId 부여: {}건", migrated);
		}
	}
}
