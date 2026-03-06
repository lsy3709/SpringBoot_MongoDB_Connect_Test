package com.myMongoTest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.myMongoTest.document.Memo;
import com.myMongoTest.service.UserService;

/**
 * MemoController 통합 테스트 — 실제 MongoDB(Testcontainers) 사용.
 * Docker 필요. 실행: ENABLE_MONGO_INTEGRATION=true
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("MemoController 통합 테스트 (실제 DB)")
@EnabledIfEnvironmentVariable(named = "ENABLE_MONGO_INTEGRATION", matches = "true")
class MemoControllerIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:6.0"))
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getConnectionString);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    @DisplayName("GET /findAllMemo — 실제 DB 조회")
    void findAllMemo_usesRealDb() throws Exception {
        mongoTemplate.remove(new Query(), Memo.class);

        Memo memo = new Memo();
        memo.setTitle("통합테스트제목");
        memo.setMessage("통합테스트메시지");
        memo.setExpiryDate("2025-12-31");
        memo.setCategoryId(null);
        userService.mongoMemoInsert(memo);

        ResultActions result = mockMvc.perform(get("/findAllMemo").with(user("admin").roles("ADMIN")));
        result.andExpect(status().isOk());

        long count = mongoTemplate.count(new Query(), Memo.class);
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("GET /findAllMemoPage — 실제 DB 커서 페이지네이션")
    void findAllMemoPage_usesRealDb() throws Exception {
        mongoTemplate.remove(new Query(), Memo.class);
        for (int i = 0; i < 3; i++) {
            Memo m = new Memo();
            m.setTitle("페이지테스트_" + i);
            m.setMessage("내용");
            userService.mongoMemoInsert(m);
        }

        mockMvc.perform(get("/findAllMemoPage").param("limit", "10").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        long count = mongoTemplate.count(new Query(), Memo.class);
        assertThat(count).isEqualTo(3);
    }
}
