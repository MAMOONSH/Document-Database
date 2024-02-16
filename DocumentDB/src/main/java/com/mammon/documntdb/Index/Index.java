package com.mammon.documntdb.Index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mammon.documntdb.database.Document;
import com.mammon.documntdb.database.DocumentCollection;

import java.util.HashMap;
import java.util.List;

public interface Index {
    void build(HashMap<String, Document> data, List<String> properties) throws JsonProcessingException;
    List<String> find(String phrase);
    void update(Document data) throws JsonProcessingException;


    void delete(List<String> properties, Document document) throws JsonProcessingException;

    boolean isBuilt();
}
