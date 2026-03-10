package com.myMongoTest.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.myMongoTest.document.Memo;

/**
 * 메모 Pageable 조회용 Repository.
 * 기존 MongoTemplate 기반 cursor 페이지네이션과 병행 사용.
 */
public interface MemoRepository extends MongoRepository<Memo, String> {

    Page<Memo> findByCategoryIdOrderByIdDesc(String categoryId, Pageable pageable);

    Page<Memo> findAllByOrderByIdDesc(Pageable pageable);
}
