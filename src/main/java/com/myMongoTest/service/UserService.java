package com.myMongoTest.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.myMongoTest.DTO.MemoPageResponse;
import com.myMongoTest.DTO.SearchDB;
import com.myMongoTest.document.Category;
import com.myMongoTest.document.Memo;
import com.myMongoTest.document.User2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService{

    private final MongoTemplate mongoTemplate;
    
    private final GridFsTemplate gridFsTemplate;

    // Convert Date to String
    public String dateToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    // Convert String to Date
    public Date stringToDate(String dateString) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.parse(dateString);
    }
    
  // 메모 하나 추가. 등록일(dateField)은 항상 서버 현재 시각으로 자동 설정.
    public void mongoMemoInsert(Memo memo) {
    	memo.setDateField(LocalDateTime.now().format(
    			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mongoTemplate.insert(memo);
    }
    
  //하나 추가. 
    
    public void mongoUser2Insert(User2 user) {
        mongoTemplate.insert(user);
    }
    
    
    //전체 검색 (메모)
    public List<Memo> mongoFindAllMemo() {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "_id"));
        List<Memo> memoList = mongoTemplate.find(query, Memo.class);
        return memoList;
    }

    /**
     * 커서 기반 전체 목록 페이지네이션 (categoryId 필터, 최신순, 10개씩).
     */
    public MemoPageResponse mongoFindMemoCursor(String categoryId, String lastId, int limit) {
        int fetchLimit = limit + 1;
        Query query = new Query();
        if (categoryId != null && !categoryId.isBlank()) {
            query.addCriteria(Criteria.where("categoryId").is(categoryId));
        }
        if (lastId != null && !lastId.isBlank()) {
            query.addCriteria(Criteria.where("_id").lt(new ObjectId(lastId)));
        }
        query.with(Sort.by(Sort.Direction.DESC, "_id"));
        query.limit(fetchLimit);
        List<Memo> list = mongoTemplate.find(query, Memo.class);
        boolean hasNext = list.size() > limit;
        List<Memo> result = hasNext ? list.subList(0, limit) : list;
        return new MemoPageResponse(result, hasNext);
    }

    /**
     * 커서 기반 검색 목록 페이지네이션 (categoryId 필터 적용).
     */
    public MemoPageResponse mongoSearchMemoCursor(SearchDB searchDB, String categoryId, String lastId, int limit) {
        if (searchDB == null || searchDB.getSearchDB() == null || searchDB.getSearchContent() == null || searchDB.getSearchContent().isBlank()) {
            return new MemoPageResponse(List.of(), false);
        }
        if ("_id".equals(searchDB.getSearchDB())) {
            try {
                ObjectId id = new ObjectId(searchDB.getSearchContent().trim());
                Memo memo = mongoTemplate.findById(id, Memo.class);
                if (memo == null) return new MemoPageResponse(List.of(), false);
                if (categoryId != null && !categoryId.isBlank() && !categoryId.equals(memo.getCategoryId())) {
                    return new MemoPageResponse(List.of(), false);
                }
                return new MemoPageResponse(List.of(memo), false);
            } catch (Exception e) {
                return new MemoPageResponse(List.of(), false);
            }
        }
        int fetchLimit = limit + 1;
        Criteria criteria;
        if ("title".equals(searchDB.getSearchDB())) {
            criteria = Criteria.where("title").regex(searchDB.getSearchContent());
        } else if ("message".equals(searchDB.getSearchDB())) {
            criteria = Criteria.where("message").regex(searchDB.getSearchContent());
        } else if ("tag".equals(searchDB.getSearchDB())) {
            String tag = searchDB.getSearchContent().trim();
            if (tag.isEmpty()) return new MemoPageResponse(List.of(), false);
            criteria = Criteria.where("tags").is(tag);
        } else {
            return new MemoPageResponse(List.of(), false);
        }
        Query query = new Query(criteria);
        if (categoryId != null && !categoryId.isBlank()) {
            query.addCriteria(Criteria.where("categoryId").is(categoryId));
        }
        if (lastId != null && !lastId.isBlank()) {
            query.addCriteria(Criteria.where("_id").lt(new ObjectId(lastId)));
        }
        query.with(Sort.by(Sort.Direction.DESC, "_id"));
        query.limit(fetchLimit);
        List<Memo> list = mongoTemplate.find(query, Memo.class);
        boolean hasNext = list.size() > limit;
        List<Memo> result = hasNext ? list.subList(0, limit) : list;
        return new MemoPageResponse(result, hasNext);
    }

    // ---- Category CRUD ----
    public List<Category> mongoFindAllCategory() {
        Query query = new Query().with(Sort.by(Sort.Direction.ASC, "sortOrder"));
        return mongoTemplate.find(query, Category.class);
    }

    public Category mongoFindOneCategory(String id) {
        return mongoTemplate.findById(id, Category.class);
    }

    public void mongoCategoryInsert(Category category) {
        mongoTemplate.insert(category);
    }

    public void mongoCategoryUpdate(Category category) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(category.getId()));
        Update update = new Update().set("name", category.getName()).set("sortOrder", category.getSortOrder());
        mongoTemplate.updateFirst(query, update, Category.class);
    }

    public void mongoCategoryDelete(String id) {
        mongoTemplate.remove(new Query(Criteria.where("_id").is(id)), Category.class);
    }

    /** categoryId가 없는 메모들에 기본 categoryId 부여 (마이그레이션) */
    public long mongoMigrateMemoCategoryId(String categoryId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("categoryId").exists(false),
                Criteria.where("categoryId").is(null)
        ));
        Update update = new Update().set("categoryId", categoryId);
        var result = mongoTemplate.updateMulti(query, update, Memo.class);
        return result.getModifiedCount();
    }

    //조건 검색
    public List<Memo> mongoSearchFindAll(SearchDB searchDB) {
//    	System.out.println("서비스 searchDB.getSearchDB(): "+searchDB.getSearchDB());
//    	System.out.println("서비스 searchDB.getSearchContent(): "+searchDB.getSearchContent());
    	List<Memo> memoList = null;
    	if(searchDB.getSearchDB().equals("_id")) {
    		Criteria criteria = new Criteria("_id");
    		criteria.is(Long.parseLong(searchDB.getSearchContent()));
    		
    		//기존 1:1 검색
    		Query query = new Query(criteria);
            memoList=mongoTemplate.find(query, Memo.class);
    	} else if( searchDB.getSearchDB().equals("title")) {
    		
    		//like 검색. 
    		Query searchQuery = new Query();
    		 
    		// LIKE '%[searchIndexInfoSearchParam.getTitleMain()]%' 와 같음
    		searchQuery.addCriteria(Criteria.where("title").regex(searchDB.getSearchContent()));
            memoList=mongoTemplate.find(searchQuery, Memo.class);
    		
    	} else if( searchDB.getSearchDB().equals("message")) {
    		//like 검색. 
    		Query searchQuery = new Query();
    		 
    		// LIKE '%[searchIndexInfoSearchParam.getTitleMain()]%' 와 같음
    		searchQuery.addCriteria(Criteria.where("message").regex(searchDB.getSearchContent()));    
    		memoList=mongoTemplate.find(searchQuery, Memo.class);
    	}
		return memoList;
        
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User2 user2 = mongoFindOneUser2Email(email);

        if (user2 == null) {
            log.warn("[로그인 실패] 사용자 없음: email={}", email);
            throw new UsernameNotFoundException(email);
        }

        // DB 저장 시 공백이 들어간 경우 대비 trim
        String storedPassword = user2.getPassword() != null ? user2.getPassword().trim() : null;
        boolean isBcrypt = storedPassword != null && storedPassword.startsWith("$2a$");
        log.info("[로그인] 사용자 조회: email={}, DB비밀번호BCrypt여부={}", email, isBcrypt);

        return User.builder()
                .username(user2.getEmail())
                .password(storedPassword)
                .roles(user2.getRole() != null ? user2.getRole().trim() : "USER")
                .build();
    }
    
  //유저 찾기
    public User2 mongoFindOneUser2Email(String email) {
    	Criteria criteria = new Criteria("email");
		criteria.is(email);
		
		//기존 1:1 검색
		Query query = new Query(criteria);
		User2 user2 = mongoTemplate.findOne(query, User2.class);
		return user2;
    }

    /** 이메일로 user2 비밀번호만 갱신 (기본 관리자 복구용) */
    public void mongoUser2UpdatePassword(String email, String encodedPassword) {
        Query query = new Query(Criteria.where("email").is(email));
        Update update = new Update().set("password", encodedPassword);
        mongoTemplate.updateFirst(query, update, User2.class);
    }

    public Memo mongoFindOneMemo(ObjectId id) {
        Memo memo = mongoTemplate.findById(id, Memo.class);
        return memo;
    }

//메모 하나 수정하기.
public void mongoMemoUpdate(Memo memo) {
	Query query = new Query();
   Update update = new Update();

   // where절 조건
   query.addCriteria(Criteria.where("_id").is(memo.getId()));
   update.set("title", memo.getTitle());
   update.set("message", memo.getMessage());
   update.set("imageFileName", memo.getImageFileName());
   update.set("categoryId", memo.getCategoryId());
   if (memo.getExpiryDate() != null) update.set("expiryDate", memo.getExpiryDate());
   if (memo.getTags() != null) update.set("tags", memo.getTags());

   mongoTemplate.updateMulti(query, update, "memo");

   }

// 삭제
public void deleteDb(String key, String value) {
	Criteria criteria = new Criteria(key);
	criteria.is(value);
	
	Query query = new Query(criteria);
	mongoTemplate.remove(query, "memo");
}



}

