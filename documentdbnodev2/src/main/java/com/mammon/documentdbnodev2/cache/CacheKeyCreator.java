package com.mammon.documentdbnodev2.cache;

import java.util.*;

public class CacheKeyCreator {
    public static String createKey(Map<String, Object> fields) {
        StringBuilder result = new StringBuilder();
        List<String> keys = new ArrayList<>(fields.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            result.append(key.toLowerCase()).append("=").append(fields.get(key).toString().toLowerCase());
        }
        return result.toString();
    }
}
