package com.mammon.documentdbnodev2.controller;


import com.mammon.documentdbnodev2.dao.DatabaseDAO;
import com.mammon.documentdbnodev2.database.DocumentCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UpdateController {
    @Autowired
    DatabaseDAO databaseDAO;

    @PostMapping("/collection/{schemaName}")
    public ResponseEntity<Object> updateCollection(
            @PathVariable("schemaName") String schemaName,
            @RequestBody DocumentCollection collection) {
        System.out.println(collection);
        databaseDAO.setCollection(schemaName, collection);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ping")
    public boolean ping() {
        return true;
    }

}
