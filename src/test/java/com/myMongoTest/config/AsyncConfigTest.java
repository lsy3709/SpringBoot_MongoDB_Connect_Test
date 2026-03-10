package com.myMongoTest.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = AsyncConfig.class)
@TestPropertySource(properties = {
    "app.async.core-pool-size=3",
    "app.async.max-pool-size=8",
    "app.async.queue-capacity=100",
    "app.async.thread-name-prefix=test-async-"
})
@DisplayName("AsyncConfig 단위 테스트")
class AsyncConfigTest {

    @Autowired(required = false)
    private org.springframework.core.task.TaskExecutor taskExecutor;

    @Test
    @DisplayName("taskExecutor 빈이 존재하고 ThreadPoolTaskExecutor 타입")
    void taskExecutor_bean_existsAndIsThreadPoolTaskExecutor() {
        assertThat(taskExecutor).isNotNull();
        assertThat(taskExecutor).isInstanceOf(ThreadPoolTaskExecutor.class);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) taskExecutor;
        assertThat(executor.getCorePoolSize()).isEqualTo(3);
        assertThat(executor.getMaxPoolSize()).isEqualTo(8);
        assertThat(executor.getQueueCapacity()).isEqualTo(100);
        assertThat(executor.getThreadNamePrefix()).isEqualTo("test-async-");
    }

    @Test
    @DisplayName("taskExecutor 실행 시 스레드 풀에서 처리")
    void taskExecutor_submitsAndExecutes() throws Exception {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) taskExecutor;
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicBoolean executed = new java.util.concurrent.atomic.AtomicBoolean(false);

        executor.execute(() -> {
            executed.set(true);
            latch.countDown();
        });

        latch.await(3, java.util.concurrent.TimeUnit.SECONDS);
        assertThat(executed.get()).isTrue();
    }
}
