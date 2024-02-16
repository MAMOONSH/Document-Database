package com.mammon.documntdb.controller;

import com.mammon.documntdb.dao.DatabaseDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@RestController
public class ImportExportController {
    @Autowired
    DatabaseDAO databaseDAO;

    @GetMapping(path = "/v2/export/{databaseName}")
    public ResponseEntity<Resource> exportDatabase(
            @PathVariable("databaseName") String databaseName) throws IOException {

        Optional<File> zipFile = databaseDAO.exportDatabase(databaseName);
        System.out.println(zipFile.get().getName());
        InputStreamResource resource = new InputStreamResource(new FileInputStream(zipFile.get()));
        return ResponseEntity.ok()
                .contentLength(zipFile.get().length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping(path = "/v2/import")
    public Object importDatabase(
            @RequestParam("database") MultipartFile zipFile) throws IOException, InterruptedException, NoSuchAlgorithmException {
        System.out.println(zipFile.getOriginalFilename());
        databaseDAO.importDatabase(zipFile);
        return "Downloading";
    }

}
