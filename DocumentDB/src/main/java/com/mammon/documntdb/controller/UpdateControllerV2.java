package com.mammon.documntdb.controller;

import com.mammon.documntdb.authentication.SHA512;
import com.mammon.documntdb.dao.DatabaseDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
public class UpdateControllerV2 {
    @Autowired
    DatabaseDAO databaseDAO;

    @PostMapping("/v2/update/{databaseName}/{schemaName}")
    public ResponseEntity<Object> update(
            @RequestBody Map<String, Object>[] fields,
            @PathVariable("schemaName") String schemaName,
            @PathVariable("databaseName") String databaseName,
            ServletRequest req) throws NoSuchAlgorithmException, IOException, InterruptedException {
        String role=(String)req.getAttribute("role");
        if(schemaName.equals("user")&&databaseName.equals("users")) {
            if (!role.equals("admin"))
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            if(fields[1].containsKey("password"))
                fields[1].put("password", SHA512.toHexString(SHA512.getSHA((String) fields[1].get("password"))));
        }
        if(databaseDAO.updateV2(databaseName,schemaName,fields))
            return ResponseEntity.ok().build();
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
