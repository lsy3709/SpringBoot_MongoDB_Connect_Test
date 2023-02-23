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

    @PostMapping
    public ResponseEntity<ObjectId> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        ObjectId objectId = gridFsTemplate.store(inputStream, file.getOriginalFilename(), file.getContentType());
        return new ResponseEntity<>(objectId, HttpStatus.OK);
    }
    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable String id) throws IOException {
        Optional<GridFSFile> gridFSFile = Optional.ofNullable(gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id))));
        if (gridFSFile.isPresent()) {
            GridFsResource resource = gridFsTemplate.getResource(gridFSFile.get().getFilename());
            byte[] bytes = IOUtils.toByteArray(resource.getInputStream());
            return ResponseEntity.ok().contentType(MediaType.valueOf(resource.getContentType())).body(bytes);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
