package com.myMongoTest.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.myMongoTest.config.SecurityConfig;
import com.myMongoTest.document.Memo;
import com.myMongoTest.service.ImageService;
import com.myMongoTest.service.UserService;

/**
 * MemoController 단위 테스트 (MockMvc, 서비스 목).
 */
@WebMvcTest(controllers = MemoController.class)
@Import(SecurityConfig.class)
@DisplayName("MemoController 단위 테스트")
class MemoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private ImageService imageService;

	@MockBean
	private com.myMongoTest.config.LoginRedirectAuthenticationSuccessHandler loginSuccessHandler;

	@Test
	@DisplayName("GET /findAllMemo 인증 시 200 + 리스트")
	void findAllMemo_authenticated_returnsList() throws Exception {
		when(userService.mongoFindAllMemo()).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/findAllMemo").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("GET /updateFormMemo/{id} 인증 시 updateForm 뷰")
	void updateFormMemo_authenticated_returnsView() throws Exception {
		String id = new ObjectId().toString();
		Memo memo = new Memo();
		memo.setId(id);
		memo.setTitle("제목");
		memo.setMessage("내용");
		when(userService.mongoFindOneMemo(org.mockito.ArgumentMatchers.any(ObjectId.class))).thenReturn(memo);

		mockMvc.perform(get("/updateFormMemo/" + id).with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("updateForm"));
	}
}
