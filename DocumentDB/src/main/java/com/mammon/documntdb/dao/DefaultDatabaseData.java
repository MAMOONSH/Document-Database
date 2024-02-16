package com.mammon.documntdb.dao;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class DefaultDatabaseData {
    private static String defaultUserAdmin="{\"username\":\"super\",\"password\":\"1234\",\"role\":\"admin\"}";
    private static ObjectMapper mapper = new ObjectMapper();

    public static String getDefaultUserAdmin() {
        return defaultUserAdmin;
    }
    public static Map<String,Object> getDefaultUserAdminMap() {
        Map<String, Object> map = null;
        try {
            map = mapper.readValue(defaultUserAdmin, Map.class);

            System.out.println(map);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
