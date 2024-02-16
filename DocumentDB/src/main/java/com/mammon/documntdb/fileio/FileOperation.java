package com.mammon.documntdb.fileio;

import com.mammon.documntdb.database.Database;
import com.mammon.documntdb.database.DocumentCollection;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

public interface FileOperation {
    void writeCollection(String databaseName, DocumentCollection collection) throws IOException;
    void updateFile(String databaseName, String schemaName, JSONObject document) throws IOException;

    void deleteCollection(String databaseName, String collectionName) throws IOException;

    void delete(String databaseName, String schemaName, String id) throws IOException;
    Optional<DocumentCollection> readCollection(String databaseName, String schemaName) throws IOException;
    Optional<Database> readDatabase(String databaseName) throws IOException;
    Optional<HashMap<String,Database>> readAllDatabases() throws IOException;

    File exportDatabase(String databaseName) throws IOException;

    void importDatabase(MultipartFile zipFile) throws IOException;
}
