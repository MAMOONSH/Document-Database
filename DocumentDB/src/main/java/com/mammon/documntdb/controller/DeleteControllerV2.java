package com.mammon.documntdb.controller;

import com.mammon.documntdb.dao.DatabaseDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
public class DeleteControllerV2 {
    @Autowired
    DatabaseDAO databaseDAO;

    @PostMapping("/v2/delete/{databaseName}/{schemaName}")
    public ResponseEntity<Object> delete(
            @RequestBody Map<String, Object> fields,
            @PathVariable("schemaName") String schemaName,
            @PathVariable("databaseName") String databaseName,
            ServletRequest req) throws IOException, InterruptedException, NoSuchAlgorithmException {
        String role = (String) req.getAttribute("role");
        if (schemaName.equals("user") && databaseName.equals("users")) {
            if (!role.equals("admin"))
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (databaseDAO.deleteByProperties(databaseName, schemaName, fields))
            return ResponseEntity.ok().build();
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/v2/delete/{databaseName}/{schemaName}/{id}")
    public ResponseEntity<Object> deleteByID(
            @PathVariable("databaseName") String databaseName,
            @PathVariable("schemaName") String schemaName,
            @PathVariable("id") String id,
            ServletRequest req) throws IOException, InterruptedException, NoSuchAlgorithmException {
        String role = (String) req.getAttribute("role");
        if (schemaName.equals("user") && databaseName.equals("users") && !role.equals("admin"))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        if (databaseDAO.deleteByID(databaseName, schemaName, id))
            return ResponseEntity.ok().build();
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/v2/delete/collection/{databaseName}/{collectionName}")
    public ResponseEntity<Object> deleteCollection(
            @PathVariable("databaseName") String databaseName,
            @PathVariable("collectionName") String collectionName,
            ServletRequest req) throws IOException {
        String role = (String) req.getAttribute("role");
        if (databaseName.equals("users") || !role.equals("admin"))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        boolean result = databaseDAO.deleteCollection(databaseName, collectionName);
        if (result)
            return ResponseEntity.ok().build();
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
