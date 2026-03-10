package com.myMongoTest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.myMongoTest.repository.MemoRepository;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.myMongoTest.DTO.SearchDB;
import com.myMongoTest.document.Memo;

/**
 * mongoFindAllMemo, mongoSearchFindAll의 max limit 적용 검증.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService findAll max limit 단위 테스트")
class UserServiceFindAllLimitTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private GridFsTemplate gridFsTemplate;

    @Mock
    private MemoRepository memoRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(mongoTemplate, gridFsTemplate, memoRepository);
        ReflectionTestUtils.setField(userService, "findAllMaxSize", 500);
    }

    @Test
    @DisplayName("mongoFindAllMemo 호출 시 query.limit(500) 적용")
    void mongoFindAllMemo_appliesLimit() {
        when(mongoTemplate.find(any(Query.class), eq(Memo.class))).thenReturn(Collections.emptyList());

        userService.mongoFindAllMemo();

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Memo.class));
        assertThat(queryCaptor.getValue().getLimit()).isEqualTo(500);
    }

    @Test
    @DisplayName("mongoSearchFindAll title 검색 시 limit 적용")
    void mongoSearchFindAll_titleSearch_appliesLimit() {
        SearchDB searchDB = new SearchDB();
        searchDB.setSearchDB("title");
        searchDB.setSearchContent("테스트");

        when(mongoTemplate.find(any(Query.class), eq(Memo.class))).thenReturn(Collections.emptyList());

        userService.mongoSearchFindAll(searchDB);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Memo.class));
        assertThat(queryCaptor.getValue().getLimit()).isEqualTo(500);
    }

    @Test
    @DisplayName("mongoSearchFindAll message 검색 시 limit 적용")
    void mongoSearchFindAll_messageSearch_appliesLimit() {
        SearchDB searchDB = new SearchDB();
        searchDB.setSearchDB("message");
        searchDB.setSearchContent("내용");

        when(mongoTemplate.find(any(Query.class), eq(Memo.class))).thenReturn(Collections.emptyList());

        userService.mongoSearchFindAll(searchDB);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Memo.class));
        assertThat(queryCaptor.getValue().getLimit()).isEqualTo(500);
    }

    @Test
    @DisplayName("mongoSearchFindAll searchDB null이면 빈 리스트")
    void mongoSearchFindAll_nullSearchDB_returnsEmpty() {
        List<Memo> result = userService.mongoSearchFindAll(null);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllMaxSize 100으로 설정 시 limit 100 적용")
    void mongoFindAllMemo_customLimit() {
        ReflectionTestUtils.setField(userService, "findAllMaxSize", 100);
        when(mongoTemplate.find(any(Query.class), eq(Memo.class))).thenReturn(Collections.emptyList());

        userService.mongoFindAllMemo();

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Memo.class));
        assertThat(queryCaptor.getValue().getLimit()).isEqualTo(100);
    }
}
