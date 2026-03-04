package com.myMongoTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * MongoDB Testcontainers 기반 통합 테스트.
 * Docker가 필요하며, CI 등에서 실행하려면 ENABLE_MONGO_INTEGRATION=true 로 설정.
 * 로컬에서 Docker 기동 후 해당 환경 변수 설정 시 실행됨.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("MongoDB 통합 테스트 (Testcontainers)")
@EnabledIfEnvironmentVariable(named = "ENABLE_MONGO_INTEGRATION", matches = "true")
class MongoIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:6.0"))
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getConnectionString);
    }

    @Test
    @DisplayName("컨텍스트 로드 및 MongoDB 연결 성공")
    void contextLoads() {
    }
}
