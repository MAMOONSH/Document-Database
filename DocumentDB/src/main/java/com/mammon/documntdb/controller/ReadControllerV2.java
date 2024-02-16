package com.mammon.documntdb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mammon.documntdb.dao.DatabaseDAO;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController

public class ReadControllerV2 {
    @Autowired
    DatabaseDAO databaseDAO;

    @GetMapping(path="/v2/read/document/{databaseName}/{schemaName}",produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> readDocumentOfSchema(
            @PathVariable("schemaName") String schemaName,
            @PathVariable("databaseName") String databaseName) throws IOException {
        Optional<String> result=databaseDAO.readData(databaseName,schemaName);
        if(result.isPresent())
        {
            return new ResponseEntity<>(
                    result.get(),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    @GetMapping("/v2/search/document/{databaseName}/{schemaName}/{id}")
    public ResponseEntity<Object> searchForDocumentById(
            @PathVariable("schemaName") String schemaName,
            @PathVariable("databaseName") String databaseName,
            @PathVariable("id") String id){
        Optional<String> result=databaseDAO.searchDocumentByID(databaseName,schemaName,id);
        if(schemaName.equals("user")&&databaseName.equals("users")){

        }
        if(result.isPresent())
        {
            return new ResponseEntity<>(
                    result.get(),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    @PostMapping("/v2/search/document/{databaseName}/{schemaName}")
    public ResponseEntity<Object> searchByProperties(
            @PathVariable("schemaName") String schemaName,
            @PathVariable("databaseName") String databaseName,
            @RequestBody Map<String, Object> fields) throws JsonProcessingException {
        Optional<JSONArray> result=databaseDAO.searchRead(databaseName,schemaName,fields);
        if(result.isPresent())
        {
            return new ResponseEntity<>(
                    result.get().toString(),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
