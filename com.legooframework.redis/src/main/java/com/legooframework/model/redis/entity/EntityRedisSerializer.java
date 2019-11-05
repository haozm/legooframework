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
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;

public class EntityRedisSerializer implements RedisSerializer<Object> {

    private static final DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormat.forPattern("yyyyMMdd");

    class LocalDateTimeJsonSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime dateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(dateTime.toString(YYYYMMDDHHMMSS));
        }
    }

    class LocalDateJsonSerializer extends JsonSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(date.toString(YYYYMMDD));
        }
    }

    class LocalDateTimeJsonDeserializer extends JsonDeserializer<LocalDateTime> {
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

    class LocalDateJsonDeserializer extends JsonDeserializer<LocalDate> {
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

    @Override
    public byte[] serialize(Object o) throws SerializationException {

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeJsonDeserializer());
        module.addSerializer(LocalDate.class, new LocalDateJsonSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateJsonDeserializer());

        mapper.registerModule(module);
        return new byte[0];
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        return null;
    }

}
