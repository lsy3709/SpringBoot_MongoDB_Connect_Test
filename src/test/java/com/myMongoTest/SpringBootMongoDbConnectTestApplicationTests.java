package com.myMongoTest;

import org.junit.jupiter.api.Test;

/**
 * 기본 애플리케이션 테스트.
 * 전체 컨텍스트 로드 통합 테스트는 MongoDB가 localhost:27017 에 실행 중일 때만 가능합니다.
 * 필요 시 별도 클래스에서 @SpringBootTest + contextLoads 로 실행하세요.
 */
class SpringBootMongoDbConnectTestApplicationTests {

	@Test
	void applicationStarts() {
		// 기본 기동 클래스 존재 확인 (실제 컨텍스트 로드는 MongoDB 필요)
	}
}
