package com.myMongoTest.document;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Document("category")
public class Category {

    @Id
    private String id;

    private String name;

    /** 탭 순서 (작을수록 앞) */
    private int sortOrder;
}
