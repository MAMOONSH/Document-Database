package com.mammon.documntdb.controller;

import com.mammon.documntdb.authentication.SHA512;
import com.mammon.documntdb.dao.DatabaseDAO;
import com.mammon.documntdb.database.DocumentCollection;
import com.mammon.documntdb.node.NodeManagerV2;
import com.mammon.documntdb.node.ReadNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;


@RestController
public class TestNodeController {

    private final String HOST = "http://localhost:";
    @Autowired
    DatabaseDAO databaseDAO;
    @Autowired
    RestTemplate restTemplate;
    ReadNode nodeManager = NodeManagerV2.createReadNodeManager();

    @GetMapping("/node/read/{databaseName}/{schemaName}")
    public void readCollection(
            @PathVariable("schemaName") String schemaName,
            @PathVariable("databaseName") String databaseName,
            HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        Optional<Integer> port = nodeManager.getPort(databaseName, schemaName);
        if (!port.isPresent())
            return;
        String nodeUri = HOST + port.get() + "/read/collection/" + SHA512.toHexString(SHA512.getSHA(schemaName));
        response.sendRedirect(nodeUri);
    }

    @GetMapping("/node/read/{databaseName}/{schemaName}/{id}")
    public void readByID(
            @PathVariable("databaseName") String databaseName,
            @PathVariable("schemaName") String schemaName,
            @PathVariable("id") String id,
            HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        Optional<Integer> port = nodeManager.getPort(databaseName, schemaName);
        if (!port.isPresent())
            return;
        String nodeUri = HOST + port.get() + "/read/collection/" + SHA512.toHexString(SHA512.getSHA(schemaName)) + "/" + id;
        response.sendRedirect(nodeUri);
    }


    /**
     * According to RFC2616 with HTTP/1.1 you can send 307 response code,
     * which will make user-agent to repeat it's POST request to provided host.
     **/
    @PostMapping("/node/search/{databaseName}/{schemaName}")
    public void search(
            @PathVariable("databaseName") String databaseName,
            @PathVariable("schemaName") String schemaName,
            @RequestBody Map<String, Object> fields,
            HttpServletResponse response) throws NoSuchAlgorithmException {
        Optional<Integer> port = nodeManager.getPort(databaseName, schemaName);
        if (!port.isPresent())
            return;
        String nodeUri = HOST + port.get() + "/search/collection/" + SHA512.toHexString(SHA512.getSHA(schemaName));
        response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        response.setHeader("Location", nodeUri);
    }
}
