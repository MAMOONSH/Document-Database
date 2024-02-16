package com.mammon.documentdbnodev2.database;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Document implements Serializable {


    private String id;
    private Map<String, Object> data;

    public Document() {
        super();
        data = new HashMap<>();
    }

    private Document(String id, Map<String, Object> data) {
        this.id = id;
        this.data = data;
        this.data.put("_id", id);
    }

    public static Document createDataObject(String id, Map<String, Object> data) {
        return new Document(id, data);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getFields() throws JsonProcessingException {
        return data;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> jsonObject) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id) && Objects.equals(data, document.data);
    }

    @Override
    public String toString() {
        return "Data{" +
                "id='" + id + '\'' +
                ", data=" + data +
                '}';
    }
}
