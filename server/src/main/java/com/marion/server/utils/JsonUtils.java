package com.marion.server.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 单列模式
 * @author Marion
 * @date 2021/10/26 18:23
 */
public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    private static volatile ObjectMapper objectMapper;

    public static final ObjectMapper INSTANCE = getMapper();

    private static ObjectMapper getMapper() {
        if (null == objectMapper) {
            synchronized (JsonUtils.class) {
                if (null == objectMapper) {
                    objectMapper = new ObjectMapper();
                    objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                    objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                    // 反序列化忽略JSON存在但JAVA对象不存在的属性
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    // 忽略值为null的字段
                    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                }
            }
        }
        return objectMapper;
    }

    public static String toString(Object value) {
        try {
            return getMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("ERROR toString parse value failed.");
        }
        return null;
    }

    public static <T> T fromString(String value, Class<T> clazz) {
        try {
            return getMapper().readValue(value, clazz);
        } catch (IOException e) {
            logger.error("ERROR fromString parse value failed.");
        }
        return null;
    }

    public static <T> T fromString(String value, TypeReference<T> typeReference) {
        try {
            return getMapper().readValue(value, typeReference);
        } catch (IOException e) {
            logger.error("ERROR fromString parse value failed.");
        }
        return null;
    }
}
