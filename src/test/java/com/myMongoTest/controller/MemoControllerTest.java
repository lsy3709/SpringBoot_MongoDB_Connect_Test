package com.myMongoTest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.util.Collections;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.PageImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myMongoTest.config.LoginFailureLoggingHandler;
import com.myMongoTest.config.LoginRateLimitFilter;
import com.myMongoTest.config.LoginRedirectAuthenticationSuccessHandler;
import com.myMongoTest.config.SecurityConfig;
import com.myMongoTest.document.Category;
import com.myMongoTest.document.Memo;
import com.myMongoTest.service.AsyncAuditService;
import com.myMongoTest.service.ImageService;
import com.myMongoTest.service.UserService;

/**
 * MemoController 단위 테스트 (MockMvc, 서비스 목).
 */
@WebMvcTest(controllers = MemoController.class)
@Import({ SecurityConfig.class, LoginRedirectAuthenticationSuccessHandler.class,
        LoginFailureLoggingHandler.class, LoginRateLimitFilter.class })
@DisplayName("MemoController 단위 테스트")
class MemoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private ImageService imageService;

	@MockBean
	private AsyncAuditService asyncAuditService;

	@Test
	@DisplayName("GET /findAllMemo 인증 시 200 + 리스트")
	void findAllMemo_authenticated_returnsList() throws Exception {
		when(userService.mongoFindAllMemo()).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/findAllMemo").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("GET /findAllMemoPageable 인증 시 200 + Page")
	void findAllMemoPageable_authenticated_returnsPage() throws Exception {
		when(userService.mongoFindMemoPage(any(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

		mockMvc.perform(get("/findAllMemoPageable").param("page", "0").param("size", "20")
						.with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("GET /updateFormMemo/{id} 인증 시 updateForm 뷰 + 카테고리 목록 포함")
	void updateFormMemo_authenticated_returnsViewWithCategories() throws Exception {
		String id = new ObjectId().toString();
		Memo memo = new Memo();
		memo.setId(id);
		memo.setTitle("제목");
		memo.setMessage("내용");
		memo.setCategoryId("cat1");
		Category cat = new Category();
		cat.setId("cat1");
		cat.setName("팬트리");
		when(userService.mongoFindOneMemo(any(ObjectId.class))).thenReturn(memo);
		when(userService.mongoFindAllCategory()).thenReturn(List.of(cat));

		mockMvc.perform(get("/updateFormMemo/" + id).with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("updateForm"))
				.andExpect(model().attributeExists("memo"))
				.andExpect(model().attributeExists("categories"));

		verify(userService).mongoFindAllCategory();
	}

	@Test
	@DisplayName("POST /updateMemo — categoryId 포함 시 서비스에 전달되어 카테고리 변경 가능")
	void updateMemo_withCategoryId_callsServiceWithCategoryId() throws Exception {
		String id = new ObjectId().toString();
		Memo body = new Memo();
		body.setId(id);
		body.setTitle("제목");
		body.setMessage("내용");
		body.setCategoryId("newCatId");

		mockMvc.perform(post("/updateMemo")
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(body))
						.with(user("admin").roles("ADMIN"))
						.with(csrf()))
				.andExpect(status().isOk());

		verify(userService).mongoMemoUpdate(org.mockito.ArgumentMatchers.argThat(m -> id.equals(m.getId()) && "newCatId".equals(m.getCategoryId())));
	}
}
