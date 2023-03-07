package com.myMongoTest.DTO;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class User2DB {
	
	private ObjectId id;
    private String email;
    private String password;
	

}
