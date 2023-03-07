package com.myMongoTest.document;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Document("user2")
public class User2 {
    
	@Id
	private ObjectId id;
	
	@Indexed(unique = true)
    private String email;
	
    private String password;
    // 수강한 과목 리스트 넣을 예정.
}