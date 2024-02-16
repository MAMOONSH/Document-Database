package com.mammon.documentdbnodev2.dao;

import com.mammon.documentdbnodev2.cache.*;
import com.mammon.documentdbnodev2.database.DocumentCollection;
import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Scope("singleton")
public class DatabaseDAO implements DAO {

    private static String collectionName = "empty";
    private static DocumentCollection collection = new DocumentCollection();
    private static DocumentCollection collectionTmp = new DocumentCollection();
    private static LRUCache<String, String> readCache = new LRUCache<>();
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private static ReentrantReadWriteLock lockTmp = new ReentrantReadWriteLock(true);

    @Override
    public void setCollection(String schemaName, DocumentCollection collection) {
        try {
            lockTmp.writeLock().lock();
            if (!collectionName.equals("empty")) {
                collectionTmp = collection;
            }
        } finally {
            lockTmp.writeLock().unlock();
        }
        lock.writeLock().lock();
        try {
            collectionName = schemaName;
            DatabaseDAO.collection = collection;
            if (!readCache.isEmpty()) readCache.clear();
            System.out.println("collection name is" + collectionName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<String> readData(String schemaName) {
        if (lock.isWriteLocked() && !collectionName.equals("empty")) {
            try {
                lockTmp.readLock().lock();
                return readData(schemaName, collectionTmp);
            } finally {
                lockTmp.readLock().unlock();
            }
        } else if (collectionName.equals("empty")) {
            return Optional.empty();
        } else {
            try {
                lock.readLock().lock();
                return readData(schemaName, collection);
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    private Optional<String> readData(String schemaName, DocumentCollection collection) {
        if (isInCache(schemaName) && !lock.isWriteLocked())
            return Optional.of(readCache.get(schemaName).get());
        JSONArray result;
        result = new JSONArray();
        if (!checkIfSchemaExist(schemaName))
            return Optional.empty();
        for (Map<String, Object> jsonObject : collection.getDataAsJson())
            result.put(new JSONObject(jsonObject));
        if (!lock.isWriteLocked()) readCache.put(schemaName, result.toString());
        return Optional.of(result.toString());
    }

    private boolean isInCache(String key) {
        return readCache.get(key).isPresent();
    }

    private boolean checkIfSchemaExist(String schemaName) {
        return Objects.equals(schemaName, collectionName);
    }

    @Override
    public Optional<String> searchDocumentByID(String schemaName, String id) {
        if (lock.isWriteLocked() && !collectionName.equals("empty")) {
            try {
                lockTmp.readLock().lock();
                return searchDocumentByID(schemaName, id, collectionTmp);
            } finally {
                lockTmp.readLock().unlock();
            }
        } else if (collectionName.equals("empty")) {
            return Optional.empty();
        } else {
            try {
                lock.readLock().lock();
                return searchDocumentByID(schemaName, id, collection);
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    private Optional<String> searchDocumentByID(String schemaName, String id, DocumentCollection collection) {
        String result;
        if (!checkIfSchemaExist(schemaName))
            return Optional.empty();
        if (!collection.getData().containsKey(id))
            return Optional.empty();
        result = collection.getData().get(id).getData().toString();
        return Optional.of(result);
    }

    @Override
    public Optional<String> search(String schemaName, Map<String, Object> fields) {
        if (lock.isWriteLocked() && !collectionName.equals("empty")) {
            try {
                lockTmp.readLock().lock();
                return search(schemaName, fields, collectionTmp);
            } finally {
                lockTmp.readLock().unlock();
            }
        } else if (collectionName.equals("empty")) {
            return Optional.empty();
        } else {
            try {
                lock.readLock().lock();
                return search(schemaName, fields, collection);
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    private Optional<String> search(String schemaName, Map<String, Object> fields, DocumentCollection collection) {
        String cacheKey = CacheKeyCreator.createKey(fields);
        if (isInCache(cacheKey) && !lock.isWriteLocked())
            return Optional.of(readCache.get(cacheKey).get());
        if (!checkIfSchemaExist(schemaName))
            return Optional.empty();
        List<String> keys = new ArrayList<>();
        for (String key : fields.keySet()) {
            keys.add(key);
        }
        collection.getIndexByJsonProperty();
        if (!collection.getIndexedProperties().contains(keys)) {
            Optional.empty();
        }
        List<Map<String, Object>> resultList = collection.findInCollection(getSearchValues(fields));
        JSONArray result = new JSONArray();
        for (Map<String, Object> map : resultList)
            result.put(map);
        if (result.length() == 0)
            return Optional.empty();
        if (!lock.isWriteLocked()) readCache.put(cacheKey, result.toString());
        return Optional.of(result.toString());
    }

    private String getSearchValues(Map<String, Object> fields) {
        StringBuilder searchValues = new StringBuilder();
        for (String key : fields.keySet()) {
            searchValues.append(key).append("=").append(fields.get(key)).append("\n");
        }
        System.out.println(searchValues);
        return searchValues.toString();
    }
}
