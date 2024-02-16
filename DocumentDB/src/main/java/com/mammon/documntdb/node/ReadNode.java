package com.mammon.documntdb.node;

import com.mammon.documntdb.database.Database;
import com.mammon.documntdb.database.DocumentCollection;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

public interface ReadNode {

    void createNodes(String databaseName, String collectionName, int numberOfInstance) throws IOException;

    void createNode(String databaseName, String collectionName) throws IOException;

    void removeNodes(String databaseName, String collectionName) throws IOException;

    void updateNode(String databaseName, String collectionName, DocumentCollection collection) throws InterruptedException, NoSuchAlgorithmException;

    Optional<Integer> getPort(String databaseName, String collectionName);

    void destroyAllNodes() throws IOException;

    void startAllNodes(Map<String, Database> databases) throws IOException, InterruptedException, NoSuchAlgorithmException;

    boolean checkIfNodeCreated(String databaseName, String collectionName);

    void removeNode(String databaseName, String collectionName);
}
