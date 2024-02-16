package com.mammon.documntdb.controller;

import com.mammon.documntdb.dao.DatabaseDAO;
import com.mammon.documntdb.database.DocumentCollection;
import com.mammon.documntdb.node.NodeManagerV2;
import com.mammon.documntdb.node.ReadNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@RestController
public class ManageNodeController {

    @Autowired
    DatabaseDAO databaseDAO;
    @Autowired
    RestTemplate restTemplate;
    ReadNode nodeManager = NodeManagerV2.createReadNodeManager();

    int INITIALIZE_TIME = 3000;

    @GetMapping("/node/add/{databaseName}/{schemaName}/{number}")
    public ResponseEntity<Object> addMoreNodes(@PathVariable("databaseName") String databaseName,
                                               @PathVariable("schemaName") String schemaName,
                                               @PathVariable("number") int number,
                                               ServletRequest req) throws IOException, NoSuchAlgorithmException, InterruptedException {
        String role = (String) req.getAttribute("role");
        if (schemaName.equals("user") && databaseName.equals("users") && !role.equals("admin"))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        Optional<DocumentCollection> collection = databaseDAO.getCollection(databaseName, schemaName);
        if (!collection.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        createAndUpdateNode(databaseName, schemaName, number, collection);
        return ResponseEntity.ok().build();
    }

    private void createAndUpdateNode(String databaseName, String schemaName, int number, Optional<DocumentCollection> collection) throws IOException, InterruptedException, NoSuchAlgorithmException {
        nodeManager.createNodes(databaseName, schemaName, number);
        Thread.sleep(INITIALIZE_TIME);
        nodeManager.updateNode(databaseName, schemaName, collection.get());
    }

    @GetMapping("/node/delete/{databaseName}/{schemaName}/{number}")
    public ResponseEntity<Object> deleteNodes(@PathVariable("databaseName") String databaseName,
                                              @PathVariable("schemaName") String schemaName,
                                              @PathVariable("number") int number,
                                              ServletRequest req) throws IOException, NoSuchAlgorithmException, InterruptedException {
        String role = (String) req.getAttribute("role");
        if (schemaName.equals("user") && databaseName.equals("users") && !role.equals("admin"))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        for (int i = 0; i < number; i++)
            nodeManager.removeNode(databaseName, schemaName);
        return ResponseEntity.ok().build();
    }
}
