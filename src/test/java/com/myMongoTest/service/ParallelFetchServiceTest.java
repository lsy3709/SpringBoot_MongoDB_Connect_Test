package com.myMongoTest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.myMongoTest.document.Category;
import com.myMongoTest.document.Memo;

/**
 * ParallelFetchService 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ParallelFetchService 단위 테스트")
class ParallelFetchServiceTest {

    @Mock
    private UserService userService;

    /** supplyAsync를 현재 스레드에서 실행 (테스트용) */
    private static final Executor SAME_THREAD = Runnable::run;

    private ParallelFetchService parallelFetchService;

    @BeforeEach
    void setUp() {
        parallelFetchService = new ParallelFetchService(userService, SAME_THREAD);
    }

    @Test
    @DisplayName("fetchMemoAndCategories 호출 시 memo + categories 반환")
    void fetchMemoAndCategories_returnsBoth() {
        ObjectId id = new ObjectId();
        Memo memo = new Memo();
        memo.setId(id.toString());
        memo.setTitle("테스트");
        Category cat = new Category();
        cat.setId("c1");
        cat.setName("카테고리");

        when(userService.mongoFindOneMemo(id)).thenReturn(memo);
        when(userService.mongoFindAllCategory()).thenReturn(List.of(cat));

        ParallelFetchService.MemoAndCategories result = parallelFetchService.fetchMemoAndCategories(id);

        assertThat(result.memo()).isEqualTo(memo);
        assertThat(result.categories()).containsExactly(cat);
        verify(userService).mongoFindOneMemo(id);
        verify(userService).mongoFindAllCategory();
    }

    @Test
    @DisplayName("memo 조회 예외 시 null fallback")
    void fetchMemoAndCategories_memoException_returnsNullMemo() {
        ObjectId id = new ObjectId();
        when(userService.mongoFindOneMemo(id)).thenThrow(new RuntimeException("DB 오류"));
        when(userService.mongoFindAllCategory()).thenReturn(List.of());

        ParallelFetchService.MemoAndCategories result = parallelFetchService.fetchMemoAndCategories(id);

        assertThat(result.memo()).isNull();
        assertThat(result.categories()).isEmpty();
    }
}
