package com.mammon.documentdbnodev2.database;

import java.io.Serializable;
import java.util.*;

public class SchemaObject implements Serializable {
    private String name;
    private Map<String,Object> schemaJson;
    public SchemaObject(){
        super();
        schemaJson=new HashMap<>();
    }

    public static SchemaObject createSchema(String name,Map<String,Object> schemaJson){
        return new SchemaObject(name, schemaJson);
    }

    private SchemaObject(String name,Map<String,Object> schemaJson){
        this.name=name;
        this.schemaJson=schemaJson;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String,Object> getSchemaJson() {
        return schemaJson;
    }

    public void setSchemaJson(Map<String,Object> schemaJson) {
        this.schemaJson = schemaJson;
    }

    @Override
    public String toString() {
        return "SchemaObject{" +
                "name='" + name + '\'' +
                ", schemaJson=" + schemaJson +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaObject that = (SchemaObject) o;
        return Objects.equals(name, that.name) && Objects.equals(schemaJson, that.schemaJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, schemaJson);
    }
}
