package com.ibm.pa.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * todo: Try to make deserialize work on the class without explicitly providing
 * second argument Class<T> targetClass . Can we figure the class type
 * statically?
 */
public interface JsonSerializable {
    default String serialize() {
        ObjectMapper objectMapper = new ObjectMapper();
        String result = "{\"error\": \"serialization to Json failed for: " + this.getClass() + "\"}";
        try {
            result = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }

    default <T> T deserialize(String json, Class<T> targetClass)
            throws JsonParseException, JsonMappingException, IOException {
        T instance = null;

        ObjectMapper objectMapper = new ObjectMapper();
        instance = objectMapper.readValue(json, targetClass);

        return instance;
    }

    static <T> T deserializeStatic(String json, Class<T> targetClass)
            throws JsonMappingException, JsonProcessingException {
        T instance = null;

        ObjectMapper objectMapper = new ObjectMapper();
        instance = objectMapper.readValue(json, targetClass);

        return instance;
    }
}