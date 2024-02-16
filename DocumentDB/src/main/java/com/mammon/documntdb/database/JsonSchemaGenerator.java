package com.mammon.documntdb.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.IOException;
import java.util.Iterator;

public final class JsonSchemaGenerator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String outputAsString(String title, String json) throws IOException {
        return outputAsString(title, json, null);
    }

    private static String outputAsString(String title,
                                         String json, JsonNodeType type) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(json);
        StringBuilder output = new StringBuilder();
        output.append("{");

        output.append("\"title\": \"").append(title).append("\", \"type\": \"object\", \"properties\": {");

        for (Iterator<String> iterator = jsonNode.fieldNames(); iterator.hasNext(); ) {
            String fieldName = iterator.next();
            JsonNodeType nodeType = jsonNode.get(fieldName).getNodeType();

            output.append(convertNodeToStringSchemaNode(jsonNode, nodeType, fieldName));
        }

        if (type == null) output.append("}");
        output.append(",\n\"additionalProperties\": false\n");
        output.append("}");

        return output.toString();
    }

    private static String convertNodeToStringSchemaNode(
            JsonNode jsonNode, JsonNodeType nodeType, String key) throws IOException {
        StringBuilder result = new StringBuilder("\"" + key + "\": { \"type\": \"");

        JsonNode node;
        switch (nodeType) {
            case ARRAY:
                node = jsonNode.get(key).get(0);
                result.append("array\", \"items\": { \"properties\":");
                result.append(outputAsString(null, node.toString(), JsonNodeType.ARRAY));
                result.append("}},");
                break;
            case BOOLEAN:
                result.append("boolean\" },");
                break;
            case NUMBER:
                result.append("number\" },");
                break;
            case OBJECT:
                node = jsonNode.get(key);
                result.append("object\", \"properties\": ");
                result.append(outputAsString(null, node.toString(), JsonNodeType.OBJECT));
                result.append("},");
                break;
            case STRING:
                result.append("string\" },");
                break;
        }

        return result.toString();
    }
}