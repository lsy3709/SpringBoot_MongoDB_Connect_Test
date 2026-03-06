package com.myMongoTest.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.myMongoTest.document.Memo;
import com.myMongoTest.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 프로파일 "sample-data" 로 기동 시 실제 DB에 메모 샘플 100건 삽입.
 * 사용: --spring.profiles.active=sample-data (한 번 실행 후 프로파일 제거 권장)
 */
@Slf4j
@Order(2)
@Component
@Profile("sample-data")
@RequiredArgsConstructor
public class SampleDataLoader implements ApplicationRunner {

    private final UserService userService;
    private final MongoTemplate mongoTemplate;

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

    @Override
    public void run(ApplicationArguments args) {
        log.info("===== SampleDataLoader 시작 (profile=sample-data) =====");
        String dbName = mongoTemplate.getDb().getName();
        log.info("삽입 대상 DB: {}, 컬렉션: memo", dbName);

        try {
            int count = 100;
            LocalDate baseDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

            for (int i = 0; i < count; i++) {
                Memo memo = new Memo();
                memo.setTitle(SAMPLE_TITLES.get(i % SAMPLE_TITLES.size()) + "_" + (i + 1));
                memo.setMessage(SAMPLE_MESSAGES.get(i % SAMPLE_MESSAGES.size()));
                memo.setExpiryDate(baseDate.plusDays(i % 30).format(formatter));
                memo.setTags(SAMPLE_TAGS.get(i % SAMPLE_TAGS.size()));
                memo.setCategoryId(i % 3 == 0 ? null : "cat-" + (i % 3));

                userService.mongoMemoInsert(memo);
            }

            long actual = mongoTemplate.count(new Query(), Memo.class);
            log.info("===== 샘플 메모 {}건 삽입 완료. 현재 memo 컬렉션 총 {}건 =====", count, actual);
        } catch (Exception e) {
            log.error("SampleDataLoader 삽입 실패. MongoDB 연결 및 profile=sample-data 확인.", e);
            throw new RuntimeException("샘플 데이터 삽입 실패", e);
        }
    }
}
