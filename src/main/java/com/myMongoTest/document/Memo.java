package com.myMongoTest.document;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Document("memo")
public class Memo {

	@Id
	private String id;

	private String title;
	private String message;
	private String dateField;
	private String imageFileObjectId;
	private String imageFileName;

	/** 카테고리 ID (냉장고, 팬트리 등 탭 구분) */
	private String categoryId;

	/** 유통기한 (yyyy-MM-dd) */
	private String expiryDate;

	/** 태그 목록 (검색·필터용) */
	private List<String> tags;
}