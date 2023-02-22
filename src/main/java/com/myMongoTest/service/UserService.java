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

    public void mongoInsert() {
        User user1 = new User(3L, "제목2", "메세지2");
        mongoTemplate.insert(user1);
    }
    
    public void mongoUserInsert(User user) {
        mongoTemplate.insert(user);
    }
    
    public List<User> mongoFindAll() {
		List<User> userList=mongoTemplate.findAll(User.class,"user");
		//log.info("rList[0]"+rList.get(0));
		return userList;
        
    }
    
public void mongoUpdate() {
        
    }

public void mongoDelete(Long id) {
	mongoTemplate.remove(id);
    
}

public void deleteDb(String key, Long value) {
	Criteria criteria = new Criteria(key);
	criteria.is(value);
	
	Query query = new Query(criteria);
	mongoTemplate.remove(query, "user");
}

}

