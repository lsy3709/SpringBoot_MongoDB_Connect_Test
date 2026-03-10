package com.myMongoTest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import com.myMongoTest.document.Memo;
import com.myMongoTest.repository.MemoRepository;

/**
 * mongoMemoInsertBatch 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 메모 배치 삽입 단위 테스트")
class UserServiceMemoBatchTest {

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
    }

    @Test
    @DisplayName("mongoMemoInsertBatch 호출 시 insertAll 1회 호출")
    void mongoMemoInsertBatch_callsInsertAllOnce() {
        Memo m1 = new Memo();
        m1.setTitle("제목1");
        Memo m2 = new Memo();
        m2.setTitle("제목2");

        userService.mongoMemoInsertBatch(List.of(m1, m2));

        ArgumentCaptor<List<?>> captor = ArgumentCaptor.forClass(List.class);
        verify(mongoTemplate).insertAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(((Memo) captor.getValue().get(0)).getDateField()).isNotNull();
    }

    @Test
    @DisplayName("mongoMemoInsertBatch 빈 리스트 시 insertAll 호출 안 함")
    void mongoMemoInsertBatch_emptyList_doesNotCallInsertAll() {
        userService.mongoMemoInsertBatch(List.of());
        verify(mongoTemplate, never()).insertAll(any());
    }

    @Test
    @DisplayName("mongoMemoInsertBatch null 시 호출 안 함")
    void mongoMemoInsertBatch_null_doesNotCallInsertAll() {
        userService.mongoMemoInsertBatch(null);
        verify(mongoTemplate, never()).insertAll(any());
    }
}
