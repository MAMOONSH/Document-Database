package com.mammon.documntdb.database;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.*;

@JsonIgnoreProperties(value = {"handler", "hibernateLazyInitializer", "FieldHandler"})
public class Database implements Serializable {

    private String name;
    private Map<String, DocumentCollection> collections;

    public Database() {
        super();
        collections = new HashMap<>();
    }

    private Database(String name) {
        this.name = name;
        collections = new HashMap<>();
    }

    public static Database createDatabase(String name) {
        return new Database(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, DocumentCollection> getCollections() {
        return collections;
    }

    public void setCollections(Map<String, DocumentCollection> schemas) {
        this.collections = schemas;
    }

    public void addCollection(String name, DocumentCollection schema) {
        collections.put(name, schema);
    }

    public Optional<DocumentCollection> getCollection(String schemaName) {
        if (collections.containsKey(schemaName))
            return Optional.of(collections.get(schemaName));
        return Optional.empty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, collections);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Database database = (Database) o;
        return Objects.equals(name, database.name) && Objects.equals(collections, database.collections);
    }

    @Override
    public String toString() {
        return "Database{" +
                "name='" + name + '\'' +
                ", collections=" + collections +
                '}';
    }
}
