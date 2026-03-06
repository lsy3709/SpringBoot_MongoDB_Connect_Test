package com.myMongoTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.myMongoTest.document.Memo;
import com.myMongoTest.service.UserService;

/**
 * 샘플 메모 데이터 100개를 DB에 넣는 통합 테스트.
 * Docker(Testcontainers) 필요. 실행: ENABLE_MONGO_INTEGRATION=true
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("샘플 데이터 100개 삽입 테스트")
@EnabledIfEnvironmentVariable(named = "ENABLE_MONGO_INTEGRATION", matches = "true")
class SampleDataInsertTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:6.0"))
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getConnectionString);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final List<String> SAMPLE_TITLES = Arrays.asList(
            "우유", "달걀", "김치", "두부", "당근", "양파", "감자", "고구마", "사과", "바나나",
            "닭가슴살", "돼지고기", "소고기", "참치캔", "라면", "쌀", "식용유", "간장", "고추장", "된장"
    );

    private static final List<String> SAMPLE_MESSAGES = Arrays.asList(
            "냉장 보관", "냉동 보관", "유통기한 확인", "빨리 드세요", "신선함 유지",
            "개봉 후 냉장", "직사광선 피하기", "습기 주의", "냉장 2~5도"
    );

    private static final List<List<String>> SAMPLE_TAGS = Arrays.asList(
            Arrays.asList("냉장", "유제품"),
            Arrays.asList("냉장", "단백질"),
            Arrays.asList("냉장", "반찬"),
            Arrays.asList("냉장", "반찬"),
            Arrays.asList("냉장", "채소"),
            Arrays.asList("냉장", "채소"),
            Arrays.asList("냉장", "채소"),
            Arrays.asList("냉장", "채소"),
            Arrays.asList("냉장", "과일"),
            Arrays.asList("냉장", "과일"),
            Arrays.asList("냉동", "육류"),
            Arrays.asList("냉동", "육류"),
            Arrays.asList("냉동", "육류"),
            Arrays.asList("냉장", "통조림"),
            Arrays.asList("실온", "면"),
            Arrays.asList("실온", "곡물"),
            Arrays.asList("실온", "조리유"),
            Arrays.asList("실온", "양념"),
            Arrays.asList("냉장", "양념"),
            Arrays.asList("냉장", "양념")
    );

    @Test
    @DisplayName("메모 샘플 데이터 100개 삽입 후 개수 검증")
    void insert100SampleMemos() {
        // 기존 memo 컬렉션 비우기 (테스트 DB이므로)
        mongoTemplate.remove(new Query(), Memo.class);

        int count = 100;
        LocalDate baseDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        for (int i = 0; i < count; i++) {
            Memo memo = new Memo();
            memo.setTitle(SAMPLE_TITLES.get(i % SAMPLE_TITLES.size()) + "_" + (i + 1));
            memo.setMessage(SAMPLE_MESSAGES.get(i % SAMPLE_MESSAGES.size()));
            memo.setExpiryDate(baseDate.plusDays(i % 30).format(formatter));
            memo.setTags(SAMPLE_TAGS.get(i % SAMPLE_TAGS.size()));
            // 카테고리는 비우거나, 테스트용 ID 지정 가능
            memo.setCategoryId(i % 3 == 0 ? null : "cat-" + (i % 3));

            userService.mongoMemoInsert(memo);
        }

        long actual = mongoTemplate.count(new Query(), Memo.class);
        assertThat(actual).isEqualTo(count);
    }
}
