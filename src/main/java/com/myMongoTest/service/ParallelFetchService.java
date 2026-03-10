package com.myMongoTest.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.myMongoTest.document.Category;
import com.myMongoTest.document.Memo;

import org.springframework.beans.factory.annotation.Qualifier;

import lombok.extern.slf4j.Slf4j;

/**
 * 독립적인 여러 조회를 병렬로 실행.
 * CompletableFuture + 타임아웃·예외 처리.
 */
@Slf4j
@Service
public class ParallelFetchService {

    private static final int TIMEOUT_SECONDS = 10;

    private final UserService userService;
    private final Executor taskExecutor;

    public ParallelFetchService(UserService userService,
                                @Qualifier("taskExecutor") Executor taskExecutor) {
        this.userService = userService;
        this.taskExecutor = taskExecutor;
    }

    /**
     * 메모 1건 + 카테고리 전체를 병렬로 조회 (updateForm 등에서 응답 시간 단축).
     * 타임아웃·예외 시 null fallback.
     */
    public MemoAndCategories fetchMemoAndCategories(ObjectId memoId) {
        CompletableFuture<Memo> memoFuture = CompletableFuture
                .supplyAsync(() -> userService.mongoFindOneMemo(memoId), taskExecutor)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.warn("[병렬 조회] memo 조회 실패 memoId={}", memoId, ex);
                    return null;
                });
        CompletableFuture<List<Category>> categoriesFuture = CompletableFuture
                .supplyAsync(userService::mongoFindAllCategory, taskExecutor)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.warn("[병렬 조회] categories 조회 실패", ex);
                    return List.of();
                });

        Memo memo = memoFuture.join();
        List<Category> categories = categoriesFuture.join();
        return new MemoAndCategories(memo, categories);
    }

    public record MemoAndCategories(Memo memo, List<Category> categories) {}
}
