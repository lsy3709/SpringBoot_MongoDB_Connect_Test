package com.myMongoTest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.myMongoTest.DTO.SearchDB;
import com.myMongoTest.document.User;
import com.myMongoTest.document.User2;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final MongoTemplate mongoTemplate;
    
    private final GridFsTemplate gridFsTemplate;

// 샘플 하나 추가. 
    public void mongoInsert() {
        User user1 = new User(3L, "제목2", "메세지2");
        mongoTemplate.insert(user1);
    }
    
//하나 추가. 
    public void mongoUserInsert(User user) {
        mongoTemplate.insert(user);
    }
    
  //하나 추가. 
    public void mongoUser2Insert(User2 user) {
        mongoTemplate.insert(user);
    }
    
    
//전체 검색
    public List<User> mongoFindAll() {
		List<User> userList=mongoTemplate.findAll(User.class,"user");
		return userList;
        
    }
    
 //조건 검색
    public List<User> mongoSearchFindAll(SearchDB searchDB) {
    	System.out.println("서비스 searchDB.getSearchDB(): "+searchDB.getSearchDB());
    	System.out.println("서비스 searchDB.getSearchContent(): "+searchDB.getSearchContent());
    	List<User> userList = null;
    	if(searchDB.getSearchDB().equals("_id")) {
    		Criteria criteria = new Criteria("_id");
    		criteria.is(Long.parseLong(searchDB.getSearchContent()));
    		
    		//기존 1:1 검색
    		Query query = new Query(criteria);
    		userList=mongoTemplate.find(query, User.class);
    	} else if( searchDB.getSearchDB().equals("title")) {
    		
    		//like 검색. 
    		Query searchQuery = new Query();
    		 
    		// LIKE '%[searchIndexInfoSearchParam.getTitleMain()]%' 와 같음
    		searchQuery.addCriteria(Criteria.where("title").regex(searchDB.getSearchContent()));    
    		userList=mongoTemplate.find(searchQuery, User.class);
    		
    	} else if( searchDB.getSearchDB().equals("message")) {
    		//like 검색. 
    		Query searchQuery = new Query();
    		 
    		// LIKE '%[searchIndexInfoSearchParam.getTitleMain()]%' 와 같음
    		searchQuery.addCriteria(Criteria.where("message").regex(searchDB.getSearchContent()));    
    		userList=mongoTemplate.find(searchQuery, User.class);
    	}
		return userList;
        
    }
    
  //유저 찾기
    public User2 mongoFindOneUser2Email(User2 user) {
    	Criteria criteria = new Criteria("email");
		criteria.is(user.getEmail());
		
		//기존 1:1 검색
		Query query = new Query(criteria);
		User2 user2 = mongoTemplate.findOne(query, User2.class);
		return user2;
    }
    
//하나 찾기
    public User mongoFindOne(Long id) {
		User user = mongoTemplate.findById(id, User.class);
		return user;
    }
 //하나 수정하기.
public void mongoUserUpdate(User user) {
	Query query = new Query();
    Update update = new Update();

    // where절 조건
    query.addCriteria(Criteria.where("_id").is(user.getId()));
    update.set("title",user.getTitle());
    update.set("message", user.getMessage());


    mongoTemplate.updateMulti(query, update, "user");

    }

// 삭제
public void deleteDb(String key, Long value) {
	Criteria criteria = new Criteria(key);
	criteria.is(value);
	
	Query query = new Query(criteria);
	mongoTemplate.remove(query, "user");
}

}

