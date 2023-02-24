package com.myMongoTest.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ImageService {
	
    private final MongoTemplate mongoTemplate;
    
    private final GridFsTemplate gridFsTemplate;
    
    //파일이름 모두 검색
    public List<String> findAllFilenames() {
        GridFSFindIterable files = gridFsTemplate.find(new Query());
        List<String> filenames = new ArrayList<>();
        for (GridFSFile file : files) {
            filenames.add(file.getFilename());
        }
        System.out.println(filenames);
        return filenames;
    }
    

 // 삭제
    public void deleteImage(String filename) {
    	   Query query = Query.query(Criteria.where("filename").is(filename));
    	    gridFsTemplate.delete(query);
    }

}