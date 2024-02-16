package com.mammon.documntdb.controller;

import com.mammon.documntdb.authentication.SHA512;
import com.mammon.documntdb.dao.DatabaseDAO;
import com.mammon.documntdb.node.DockerUtil;
import com.mammon.documntdb.node.NodeManagerV2;
import com.mammon.documntdb.node.ReadNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;


//todo delete collection
//todo delete database schema
@RestController
public class WriteControllerV2 {
    @Autowired
    DatabaseDAO databaseDAO;
    ReadNode readNodeManager = NodeManagerV2.createReadNodeManager();

    @PostConstruct
    public void init() throws IOException {
        databaseDAO.readStoredData();
    }

    @PreDestroy
    public void preDestroy() throws IOException {
        readNodeManager.destroyAllNodes();
    }


    @PostMapping("/v2/add/schema/{databaseName}/{schemaName}")
    public ResponseEntity<Object> addSchema(
            @RequestBody Map<String, Object> body,
            @PathVariable("schemaName") String schemaName,
            @PathVariable("databaseName") String databaseName,
            ServletRequest req) throws IOException {
        String role = (String) req.getAttribute("role");
        if (schemaName.equals("user") && databaseName.equals("users") && !role.equals("admin"))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        databaseDAO.addSchema(databaseName, schemaName, body);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v2/add/document/{databaseName}/{schemaName}")
    public ResponseEntity<Object> addDocument(
            @RequestBody Map<String, Object> body,
            @PathVariable("schemaName") String schemaName,
            @PathVariable("databaseName") String databaseName,
            ServletRequest req) throws NoSuchAlgorithmException, IOException, InterruptedException {
        String role = (String) req.getAttribute("role");
        if (schemaName.equals("user") && databaseName.equals("users")) {
            if (role.equals("admin")) {
                System.out.println("entered");
                body.put("password", SHA512.toHexString(SHA512.getSHA((String) body.get("password"))));
            } else
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (databaseDAO.addObject(databaseName, schemaName, body))
            return ResponseEntity.ok().build();
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


}
