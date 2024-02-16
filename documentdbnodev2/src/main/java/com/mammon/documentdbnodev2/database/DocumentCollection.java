package com.mammon.documentdbnodev2.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mammon.documentdbnodev2.Index.Index;
import com.mammon.documentdbnodev2.Index.IndexByJsonProperty;


import java.io.Serializable;
import java.util.*;


public class DocumentCollection implements Serializable {

    private SchemaObject schema;
    private HashMap<String, Document> data;
    private IndexByJsonProperty indexByJsonProperty;
    private List<String> indexedProperties;
    public DocumentCollection(){
        super();
        data=new HashMap<>();
        indexByJsonProperty=new IndexByJsonProperty();
        indexedProperties=new ArrayList<>();
        schema=new SchemaObject();
    }



    public static DocumentCollection createCollection(SchemaObject schema){
        return new DocumentCollection(schema);
    }
    private DocumentCollection(SchemaObject schema){
        this.schema=schema;
        data=new HashMap<>();
        indexByJsonProperty=IndexByJsonProperty.createEmptyIndex();
    }

    public SchemaObject getSchema() {
        return schema;
    }

    public void setSchema(SchemaObject schema) {
        this.schema = schema;
    }

    public HashMap<String, Document> getData() {
        return data;
    }

    public void setData(HashMap<String, Document> data) {
        this.data = data;
    }

    public void addDataObject(Document document) throws JsonProcessingException {
        if(indexByJsonProperty.isBuilt())
            indexByJsonProperty.update(document);
        this.data.put(document.getId(), document);
        if(!indexByJsonProperty.isBuilt()){
            buildIndexMulti(new ArrayList<>(document.getFields().keySet()));
        }
    }
    public List<Map<String,Object>> getDataAsJson(){
        List<Map<String,Object>> dataJson=new ArrayList<>();
        for(String id: data.keySet()){
            dataJson.add(data.get(id).getData());
        }
        return dataJson;
    }

    public Index getIndexByJsonProperty() {
        return indexByJsonProperty;
    }

    public void setIndexByJsonProperty(IndexByJsonProperty indexByJsonProperty) {
        this.indexByJsonProperty = indexByJsonProperty;
    }
    public void buildIndexMulti(List<String> properties) throws JsonProcessingException {
        indexedProperties=properties;
        indexByJsonProperty=IndexByJsonProperty.createMultiIndex(data,properties);
    }
    public boolean isDataEmpty(){
        return data.isEmpty();
    }

    public List<String> getIndexedProperties() {
        return indexedProperties;
    }

    public void setIndexedProperties(List<String> indexedProperties) {
        this.indexedProperties = indexedProperties;
    }
    public List<Map<String,Object>> findInCollection(String searchValues){
        List<String> result=indexByJsonProperty.find(searchValues);
        List<Map<String,Object>> jsonArray=new ArrayList<Map<String,Object>>();
        for(String id:result){
            jsonArray.add(data.get(id).getData());
        }
        return jsonArray;
    }
    public void remove(String id) throws JsonProcessingException {
        Document document=data.get(id);
        data.remove(id);
        if(indexByJsonProperty.isBuilt())
            indexByJsonProperty.delete(indexedProperties,document);
    }

    @Override
    public String toString() {
        return "DocumentCollection{" +
                "schema=" + schema +
                ", data=" + data +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentCollection that = (DocumentCollection) o;
        return Objects.equals(schema, that.schema) && Objects.equals(data, that.data) && Objects.equals(indexByJsonProperty, that.indexByJsonProperty) && Objects.equals(indexedProperties, that.indexedProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, data, indexByJsonProperty, indexedProperties);
    }
}
