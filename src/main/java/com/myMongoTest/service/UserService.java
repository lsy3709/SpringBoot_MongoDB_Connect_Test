package com.myMongoTest.service;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.myMongoTest.document.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final MongoTemplate mongoTemplate;

// 샘플 하나 추가. 
    public void mongoInsert() {
        User user1 = new User(3L, "제목2", "메세지2");
        mongoTemplate.insert(user1);
    }
    
//하나 추가. 
    public void mongoUserInsert(User user) {
        mongoTemplate.insert(user);
    }
    
//전체 검색
    public List<User> mongoFindAll() {
		List<User> userList=mongoTemplate.findAll(User.class,"user");
		//log.info("rList[0]"+rList.get(0));
		return userList;
        
    }
    
//하나 찾기
    public User mongoFindOne(Long id) {
		User user = mongoTemplate.findById(id, User.class);
		return user;
    }
    
public void mongoUpdate() {
        
    }

// 삭제
public void deleteDb(String key, Long value) {
	Criteria criteria = new Criteria(key);
	criteria.is(value);
	
	Query query = new Query(criteria);
	mongoTemplate.remove(query, "user");
}

}

