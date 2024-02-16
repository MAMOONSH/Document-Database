package com.mammon.documntdb.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mammon.documntdb.database.DocumentCollection;
import org.json.JSONArray;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public interface DAO {

    void readStoredData() throws IOException;

    Optional<String> readData(String databaseName, String schemaName) throws IOException;

    boolean addObject(String databaseName, String schemaName, Map<String, Object> jsonObject) throws NoSuchAlgorithmException, IOException, InterruptedException;

    void addSchema(String databaseName, String schemaName, Map<String, Object> schemaBody) throws IOException;

    boolean updateV2(String databaseName, String schemaName, Map<String, Object>[] fields) throws IOException, InterruptedException, NoSuchAlgorithmException;

    boolean deleteByProperties(String databaseName, String schemaName, Map<String, Object> fields) throws IOException, InterruptedException, NoSuchAlgorithmException;

    Optional<JSONArray> searchRead(String databaseName, String schemaName, Map<String, Object> fields) throws JsonProcessingException;

    boolean deleteByID(String databaseName, String schemaName, String id) throws IOException, InterruptedException, NoSuchAlgorithmException;

    Optional<String> searchDocumentByID(String databaseName, String schemaName, String id);

    Optional<File> exportDatabase(String databaseName) throws IOException;

    void importDatabase(MultipartFile zipFile) throws IOException, InterruptedException, NoSuchAlgorithmException;

    boolean deleteCollection(String databaseName, String collectionName) throws IOException;

    Optional<DocumentCollection> getCollection(String databaseName, String schemaName);
}
