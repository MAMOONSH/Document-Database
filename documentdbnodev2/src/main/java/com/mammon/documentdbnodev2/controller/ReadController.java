package com.mammon.documentdbnodev2.controller;

import com.mammon.documentdbnodev2.dao.DatabaseDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
public class ReadController {
    @Autowired
    DatabaseDAO databaseDAO;

    @GetMapping("/read/collection/{collectionName}")
    public ResponseEntity<Object> readCollectionData(
            @PathVariable("collectionName") String collectionName) {
        System.out.println("hello");
        Optional<String> result = databaseDAO.readData(collectionName);
        return result.<ResponseEntity<Object>>map(s -> new ResponseEntity<>(
                s,
                HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping("/read/collection/{collectionName}/{id}")
    public ResponseEntity<Object> readByID(
            @PathVariable("collectionName") String collectionName,
            @PathVariable("id") String id) {
        System.out.println("hello from read by id");
        Optional<String> result = databaseDAO.searchDocumentByID(collectionName, id);
        return result.<ResponseEntity<Object>>map(s -> new ResponseEntity<>(
                s,
                HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PostMapping("/search/collection/{collectionName}")
    public ResponseEntity<Object> searchInCollection(
            @PathVariable("collectionName") String collectionName,
            @RequestBody Map<String, Object> fields) {
        System.out.println("hello from search");
        Optional<String> result = databaseDAO.search(collectionName, fields);
        return result.<ResponseEntity<Object>>map(s -> new ResponseEntity<>(
                s,
                HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

}
