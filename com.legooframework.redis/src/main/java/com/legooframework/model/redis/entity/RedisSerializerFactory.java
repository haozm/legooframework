package com.legooframework.model.redis.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.IOException;

public class RedisSerializerFactory extends AbstractFactoryBean<RedisSerializer<Object>> {


    @Override
    public Class<RedisSerializer> getObjectType() {
        return RedisSerializer.class;
    }

    @Override
    protected RedisSerializer<Object> createInstance() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeJsonDeserializer());
        module.addSerializer(LocalDate.class, new LocalDateJsonSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateJsonDeserializer());
        objectMapper.registerModule(module);
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    private static final DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormat.forPattern("yyyyMMdd");

    private static class LocalDateTimeJsonSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime dateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(dateTime.toString(YYYYMMDDHHMMSS));
        }
    }

    private static class LocalDateJsonSerializer extends JsonSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(date.toString(YYYYMMDD));
        }
    }

    private static class LocalDateTimeJsonDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

            try {
                if (jsonParser != null && StringUtils.isNotEmpty(jsonParser.getText())) {
                    return LocalDateTime.parse(jsonParser.getText(), YYYYMMDDHHMMSS);
                } else {
                    return null;
                }

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class LocalDateJsonDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            try {
                if (jsonParser != null && StringUtils.isNotEmpty(jsonParser.getText())) {
                    return LocalDate.parse(jsonParser.getText(), YYYYMMDD);
                } else {
                    return null;
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
