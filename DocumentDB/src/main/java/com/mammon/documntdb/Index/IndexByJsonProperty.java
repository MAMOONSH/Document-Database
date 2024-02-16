package com.mammon.documntdb.Index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mammon.documntdb.database.Document;

import java.io.Serializable;
import java.util.*;

public class IndexByJsonProperty implements Index, Serializable {

    private int sourcesCount;
    private boolean built = false;
    private Map<Integer, String> sources;
    private HashMap<String, HashSet<Integer>> index;
    private List<String> indexedProperties;
    private HashMap<String, Integer> documentCount;


    public IndexByJsonProperty() {
        super();
        sources = new HashMap<>();
        index = new HashMap<>();
        indexedProperties = new ArrayList<>();
        documentCount = new HashMap<>();
    }

    private IndexByJsonProperty(HashMap<String, Document> data, List<String> properties) throws JsonProcessingException {
        build(data, properties);
    }

    @Override
    public void build(HashMap<String, Document> data, List<String> properties) throws JsonProcessingException {
        documentCount = new HashMap<>();
        indexedProperties = properties;
        sources = new HashMap<>();
        index = new HashMap<>();
        int i = 0;
        for (String dataKey : data.keySet()) {
            Document document = data.get(dataKey);
            System.out.println(document);
            doIndexing(properties, i, document);
            i++;
        }
        built = true;
        sourcesCount = i;
    }

    private void doIndexing(List<String> properties, int fileCount, Document document) throws JsonProcessingException {
        documentCount.put(document.getId(), fileCount);
        sources.put(fileCount, document.getId());
        for (String key : properties) {
            if (!document.getFields().containsKey(key))
                continue;
            String value = key + "=" + String.valueOf(document.getFields().get(key));
            System.out.println(value);
            value = value.toLowerCase();
            if (!index.containsKey(value))
                index.put(value, new HashSet<>());
            index.get(value).add(fileCount);
        }
    }

    @Override
    public List<String> find(String phrase) {
        String[] words = phrase.split("\\n");
        List<String> filesName = new ArrayList<>();
        if (!index.containsKey(words[0].toLowerCase())) {
            return filesName;
        }
        HashSet<Integer> res = new HashSet<>(index.get(words[0].toLowerCase()));
        for (String word : words) {
            if (!index.containsKey(word.toLowerCase())) {
                return filesName;
            }
            res.retainAll(index.get(word.toLowerCase()));
        }
        if (res.size() == 0) {
            System.out.println("Not found");
            return filesName;
        }
        System.out.println("Found in: ");
        for (int num : res) {
            filesName.add(sources.get(num));
            System.out.println("\t" + sources.get(num));
        }
        return filesName;
    }

    @Override
    public void update(Document document) throws JsonProcessingException {
        sourcesCount++;
        doIndexing(indexedProperties, sourcesCount, document);
    }

    @Override
    public void delete(List<String> properties, Document document) throws JsonProcessingException {
        for (String key : properties) {
            if (!document.getFields().containsKey(key))
                continue;
            String value = key + "=" + String.valueOf(document.getFields().get(key));
            value = value.toLowerCase();
            String id = document.getId();
            int idCount = documentCount.get(id);
            index.get(value).remove(idCount);
            if (index.get(value).isEmpty())
                index.remove(value);
            sources.remove(idCount);
        }
    }

    @Override
    public boolean isBuilt() {
        return built;
    }

    public void setBuilt(boolean built) {
        this.built = built;
    }

    public static IndexByJsonProperty createMultiIndex(HashMap<String, Document> data
            , List<String> properties) throws JsonProcessingException {
        return new IndexByJsonProperty(data, properties);
    }

    public static IndexByJsonProperty createEmptyIndex() {
        return new IndexByJsonProperty();
    }

    public int getSourcesCount() {
        return sourcesCount;
    }

    public void setSourcesCount(int sourcesCount) {
        this.sourcesCount = sourcesCount;
    }

    public Map<Integer, String> getSources() {
        return sources;
    }

    public void setSources(Map<Integer, String> sources) {
        this.sources = sources;
    }

    public HashMap<String, HashSet<Integer>> getIndex() {
        return index;
    }

    public void setIndex(HashMap<String, HashSet<Integer>> index) {
        this.index = index;
    }

    public List<String> getIndexedProperties() {
        return indexedProperties;
    }

    public void setIndexedProperties(List<String> indexedProperties) {
        this.indexedProperties = indexedProperties;
    }

    public HashMap<String, Integer> getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(HashMap<String, Integer> documentCount) {
        this.documentCount = documentCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourcesCount, built, sources, index, indexedProperties, documentCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexByJsonProperty that = (IndexByJsonProperty) o;
        return sourcesCount == that.sourcesCount && built == that.built && Objects.equals(sources, that.sources) && Objects.equals(index, that.index) && Objects.equals(indexedProperties, that.indexedProperties) && Objects.equals(documentCount, that.documentCount);
    }

    @Override
    public String toString() {
        return "IndexByJsonProperty{" +
                "sourcesCount=" + sourcesCount +
                ", built=" + built +
                ", sources=" + sources +
                ", index=" + index +
                ", indexedProperties=" + indexedProperties +
                ", documentCount=" + documentCount +
                '}';
    }
}