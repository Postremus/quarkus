package io.quarkus.bootstrap.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationTargetException;

public class Mapper {
    public static ObjectMapper getMapper() {

        try {
            Class<?> objectMapperClass = Thread.currentThread().getContextClassLoader()
                    .loadClass("com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper");
            ObjectMapper MAPPER = (ObjectMapper) objectMapperClass.getDeclaredConstructor().newInstance();
            MAPPER.setVisibility(MAPPER.getSerializationConfig().getDefaultVisibilityChecker()
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
            MAPPER.activateDefaultTyping(MAPPER.getPolymorphicTypeValidator(),
                    ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
            MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return MAPPER;
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException
                | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
