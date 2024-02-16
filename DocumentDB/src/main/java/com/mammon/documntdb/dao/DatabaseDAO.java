package com.mammon.documntdb.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mammon.documntdb.database.*;
import com.mammon.documntdb.fileio.*;
import com.mammon.documntdb.node.*;
import org.everit.json.schema.*;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Service
@Scope("singleton")
public class DatabaseDAO implements DAO {

    private static Map<String, Database> databases;
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final FileOperation fileIO = DatabaseIO.createIOOperation();
    ReadNode nodeManager = NodeManagerV2.createReadNodeManager();

    private List<String> getID(JSONArray data) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            String id = data.getJSONObject(i).getString("_id");
            ids.add(id);
        }
        return ids;
    }

    @Override
    public void readStoredData() throws IOException {
        lock.writeLock().lock();
        try {
            Optional<HashMap<String, Database>> result = fileIO.readAllDatabases();
            if (result.isPresent() && !result.get().isEmpty()) {
                databases = result.get();
                nodeManager.startAllNodes(databases);
            } else {
                databases = new HashMap<>();
                addSchema("users", "user", DefaultDatabaseData.getDefaultUserAdminMap());
                addObject("users", "user", DefaultDatabaseData.getDefaultUserAdminMap());
            }
        } catch (NoSuchAlgorithmException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<String> readData(String databaseName, String schemaName) throws IOException {
        lock.readLock().lock();
        JSONArray result;
        try {
            result = new JSONArray();
            if (!checkDatabaseAndSchemaExist(databaseName, schemaName))
                return Optional.empty();
            DocumentCollection collection = databases.get(databaseName).getCollection(schemaName).get();
            for (Map<String, Object> jsonObject : collection.getDataAsJson())
                result.put(new JSONObject(jsonObject));
        } finally {
            lock.readLock().unlock();
        }
        return Optional.of(result.toString());
    }


    @Override
    public boolean addObject(String databaseName, String schemaName, Map<String, Object> jsonObject) throws NoSuchAlgorithmException, IOException, InterruptedException {
        lock.writeLock().lock();
        try {
            if (!checkDatabaseAndSchemaExist(databaseName, schemaName))
                return false;
            DocumentCollection collection = databases.get(databaseName).getCollection(schemaName).get();
            Map<String, Object> schemaMap = collection.getSchema().getSchemaJson();
            if (!isValidJson(new JSONObject(schemaMap), new JSONObject(jsonObject))) return false;
            String id = JsonIDGenerator.getUniqueID();
            Document document = Document.createDataObject(id, jsonObject);
            collection.addDataObject(document);
            fileIO.writeCollection(databaseName, collection);

        } finally {
            lock.writeLock().unlock();
        }
        if (!databaseName.equals("users"))
            nodeManager.updateNode(databaseName, schemaName, databases.get(databaseName).getCollection(schemaName).get());
        return true;
    }

    @Override
    public void addSchema(String databaseName, String schemaName, Map<String, Object> schemaBody) throws IOException {
        lock.writeLock().lock();
        try {
            if (!isDatabaseCreated(databaseName))
                createDatabase(databaseName);
            String body = new JSONObject(schemaBody).toString();
            String resultSchema = JsonSchemaGenerator.outputAsString(schemaName, body);
            JSONObject schema = new JSONObject(resultSchema);
            Map<String, Object> response = new Gson().fromJson(schema.toString(), HashMap.class);
            SchemaObject schemaObject = SchemaObject.createSchema(schemaName, response);
            DocumentCollection documentCollection = DocumentCollection.createCollection(schemaObject);
            databases.get(databaseName).addCollection(schemaName, documentCollection);
            fileIO.writeCollection(databaseName, documentCollection);
            if (!nodeManager.checkIfNodeCreated(databaseName, schemaName) && !databaseName.equals("users")) {
                nodeManager.createNodes(databaseName, schemaName, 2);
                nodeManager.updateNode(databaseName, schemaName, databases.get(databaseName).getCollection(schemaName).get());
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean updateV2(String databaseName, String schemaName, Map<String, Object>[] fields) throws IOException, InterruptedException, NoSuchAlgorithmException {
        lock.writeLock().lock();
        try {
            if (!checkDatabaseAndSchemaExist(databaseName, schemaName))
                return false;
            Optional<JSONArray> searchResult = search(databaseName, schemaName, fields[0], false);
            if (!searchResult.isPresent())
                return false;
            DocumentCollection collection = databases.get(databaseName).getCollection(schemaName).get();
            List<String> ids = updateV2(fields[1], searchResult.get(), collection);
            databases.get(databaseName).addCollection(schemaName, collection);
            for (String id : ids) {
                fileIO.updateFile(databaseName, schemaName, new JSONObject(collection.getData().get(id).getData()));
            }
            nodeManager.updateNode(databaseName, schemaName, databases.get(databaseName).getCollection(schemaName).get());
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    @Override
    public boolean deleteByProperties(String databaseName, String schemaName, Map<String, Object> fields) throws IOException, InterruptedException, NoSuchAlgorithmException {
        lock.writeLock().lock();
        try {
            if (!checkDatabaseAndSchemaExist(databaseName, schemaName))
                return false;
            Optional<JSONArray> searchResult = search(databaseName, schemaName, fields, false);
            if (!searchResult.isPresent())
                return false;
            for (String id : getID(searchResult.get())) {
                fileIO.delete(databaseName, schemaName, id);
                deleteByID(databaseName, schemaName, id);
            }
            nodeManager.updateNode(databaseName, schemaName, databases.get(databaseName).getCollection(schemaName).get());
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    @Override
    public Optional<JSONArray> searchRead(String databaseName, String schemaName, Map<String, Object> fields) throws JsonProcessingException {
        lock.readLock().lock();
        try {
            return search(databaseName, schemaName, fields, true);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean deleteByID(String databaseName, String schemaName, String id) throws IOException, InterruptedException, NoSuchAlgorithmException {
        lock.writeLock().lock();
        try {
            if (!checkDatabaseAndSchemaExist(databaseName, schemaName))
                return false;
            fileIO.delete(databaseName, schemaName, id);
            databases.get(databaseName).getCollection(schemaName).get().remove(id);
            nodeManager.updateNode(databaseName, schemaName, databases.get(databaseName).getCollection(schemaName).get());
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }


    @Override
    public Optional<String> searchDocumentByID(String databaseName, String schemaName, String id) {
        lock.readLock().lock();
        String result;
        try {
            if (!checkDatabaseAndSchemaExist(databaseName, schemaName))
                return Optional.empty();
            if (!databases.get(databaseName).getCollection(schemaName).get().getData().containsKey(id))
                return Optional.empty();
            result = databases.get(databaseName).getCollection(schemaName).get()
                    .getData().get(id).getData().toString();
        } finally {
            lock.readLock().unlock();
        }
        return Optional.of(result);
    }


    @Override
    public Optional<File> exportDatabase(String databaseName) throws IOException {
        lock.readLock().lock();
        try {
            if (!isDatabaseCreated(databaseName))
                return Optional.empty();
            return Optional.of(fileIO.exportDatabase(databaseName));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void importDatabase(MultipartFile zipFile) throws IOException, InterruptedException, NoSuchAlgorithmException {
        String fileName = zipFile.getOriginalFilename();
        lock.writeLock().lock();
        try {
            fileIO.importDatabase(zipFile);
            Optional<Database> database = fileIO.readDatabase(fileName);
            if (!database.isPresent())
                return;
            databases.put(zipFile.getOriginalFilename(), database.get());
        } finally {
            lock.writeLock().unlock();
        }
        if (fileName.equals("users"))
            return;
        UpdateNodesAfterImport(fileName);
    }

    private void UpdateNodesAfterImport(String fileName) throws InterruptedException, IOException, NoSuchAlgorithmException {
        for (String collectionName : databases.get(fileName).getCollections().keySet()) {
            if (nodeManager.checkIfNodeCreated(fileName, collectionName)) {
                nodeManager.updateNode(fileName, collectionName,
                        databases.get(fileName).getCollection(collectionName).get());
                continue;
            }
            nodeManager.removeNodes(fileName, collectionName);
            nodeManager.createNodes(fileName, collectionName, 2);
            nodeManager.updateNode(fileName, collectionName, databases.get(fileName).getCollection(collectionName).get());
        }
    }


    @Override
    public boolean deleteCollection(String databaseName, String collectionName) throws IOException {
        if (!checkDatabaseAndSchemaExist(databaseName, collectionName))
            return false;
        lock.writeLock().lock();
        try {
            databases.get(databaseName).getCollections().remove(collectionName);
            fileIO.deleteCollection(databaseName, collectionName);
            nodeManager.removeNodes(databaseName, collectionName);
            if (databases.get(databaseName).getCollections().isEmpty()) {
                databases.remove(databaseName);
                fileIO.deleteCollection(databaseName, "");
            }
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    @Override
    public Optional<DocumentCollection> getCollection(String databaseName, String schemaName) {
        if (!checkDatabaseAndSchemaExist(databaseName, schemaName))
            return Optional.empty();
        try {
            lock.readLock().lock();
            return Optional.of(databases.get(databaseName).getCollection(schemaName).get());
        } finally {
            lock.readLock().unlock();
        }
    }

    private Optional<JSONArray> search(String databaseName, String schemaName, Map<String, Object> fields, boolean isRead) throws JsonProcessingException {
        if (!checkDatabaseAndSchemaExist(databaseName, schemaName))
            return Optional.empty();
        List<String> keys = new ArrayList<>();
        DocumentCollection documentCollection = databases.get(databaseName).getCollection(schemaName).get();
        for (String key : fields.keySet()) {
            keys.add(key);
        }
        documentCollection.getIndexByJsonProperty();
        if (!documentCollection.getIndexByJsonProperty().isBuilt()) {
            Optional.empty();
        }
        if (!documentCollection.getIndexedProperties().contains(keys)) {
            Optional.empty();
        }
        List<Map<String, Object>> resultList = documentCollection.findInCollection(getSearchValues(fields));
        JSONArray result = new JSONArray();
        for (Map<String, Object> map : resultList)
            result.put(map);
        if (result.length() == 0)
            return Optional.empty();
        return Optional.of(result);
    }

    private String getSearchValues(Map<String, Object> fields) {
        StringBuilder searchValues = new StringBuilder();
        for (String key : fields.keySet()) {
            searchValues.append(key).append("=").append(fields.get(key)).append("\n");
        }
        return searchValues.toString();
    }

    private List<String> updateV2(Map<String, Object> fields, JSONArray oldDocuments, DocumentCollection collection) throws JsonProcessingException {
        List<String> ids = new ArrayList<>();
        for (int count = 0; count < oldDocuments.length(); count++) {
            JSONObject updateJsonData = oldDocuments.getJSONObject(count);
            for (String key : fields.keySet())
                updateJsonData.put(key, fields.get(key));
            String id = updateJsonData.getString("_id");
            collection.remove(id);
            Map<String, Object> data = new ObjectMapper().readValue(updateJsonData.toString(), HashMap.class);
            Document document = Document.createDataObject(id, data);
            collection.addDataObject(document);
            ids.add(id);
        }
        return ids;
    }

    private boolean isDatabaseCreated(String databaseName) {
        if (databases.containsKey(databaseName))
            return true;
        return false;
    }

    private void createDatabase(String databaseName) {
        Database database = Database.createDatabase(databaseName);
        databases.put(databaseName, database);
    }

    private boolean checkDatabaseAndSchemaExist(String databaseName, String schemaName) {
        return isDatabaseCreated(databaseName) && databases.get(databaseName).getCollection(schemaName).isPresent();
    }

    private boolean isValidJson(JSONObject schema, JSONObject newJson) {
        Schema schemaValidator = SchemaLoader.load(schema);
        try {
            schemaValidator.validate(newJson);
            System.out.println("\n\n valid");
        } catch (ValidationException e) {
            System.out.println("\n\n not valid");
            return false;
        }
        return true;
    }
}
