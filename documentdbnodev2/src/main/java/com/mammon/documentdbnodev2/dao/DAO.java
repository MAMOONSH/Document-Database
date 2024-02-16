package com.mammon.documentdbnodev2.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mammon.documentdbnodev2.database.DocumentCollection;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public interface DAO {
    void setCollection(String schemaName, DocumentCollection collection);
    Optional<String> readData(String schemaName) throws IOException;
    Optional<String> searchDocumentByID(String schemaName, String id);
    Optional<String> search(String schemaName, Map<String, Object> fields) throws JsonProcessingException;
}
