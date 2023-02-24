package com.myMongoTest.controller;

import com.mongodb.client.gridfs.model.GridFSFile;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private GridFsTemplate gridFsTemplate;
    
    @Autowired
    private GridFsOperations gridFsOperations;

    @PostMapping
    public ResponseEntity<ObjectId> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        ObjectId objectId = gridFsTemplate.store(inputStream, file.getOriginalFilename(), file.getContentType());
        return new ResponseEntity<>(objectId, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable String id) throws IOException {
    	System.out.println("===========================================");
    	System.out.println("id 확인: "+ id);
    	
    	GridFSFile gridFSFile2 = 	gridFsOperations.findOne(new Query(Criteria.where("filename").is(id)));
    	
        if (gridFSFile2 != null) {
            GridFsResource resource = gridFsTemplate.getResource(gridFSFile2);
            byte[] bytes = IOUtils.toByteArray(resource.getInputStream());
            return ResponseEntity.ok().body(bytes);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    
}
