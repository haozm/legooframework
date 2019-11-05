package com.legooframework.model.redis.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.utils.DateTimeUtils;
import org.joda.time.LocalDate;

import java.lang.reflect.Type;
import java.util.Map;

public class CacheEntity extends BaseEntity<Integer> {

    private String name, address;
    private int sex;
    private LocalDate biredy;

    CacheEntity(Integer id, String name, int sex, LocalDate biredy, String address) {
        super(id);
        this.name = name;
        this.address = address;
        this.sex = sex;
        this.biredy = biredy;
    }

    CacheEntity(String ser) {
        super(0);
    }

    public String toJsonString() {
        Map<String, String> params = Maps.newHashMap();
        params.put("name", name);
        params.put("sex", String.valueOf(sex));
        params.put("biredy", biredy.toString("yyyyMMdd"));
        return Joiner.on(',').withKeyValueSeparator("=").join(params);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass().getName())
                .add("name", name)
                .add("address", address)
                .add("sex", sex)
                .add("biredy", biredy)
                .toString();
    }

    static class LocalDateSerializer implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString("yyyyMMdd"));
        }

        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return DateTimeUtils.parseShortYYYYMMDD(json.getAsJsonPrimitive().getAsString());
        }
    }


    public static void main(String[] args) throws Exception {
        CacheEntity entity = new CacheEntity(1, "hjaopx", 2, LocalDate.now(), "guagnz");
        GsonBuilder builder = new GsonBuilder();
        builder = builder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        //builder = builder.registerTypeAdapter(Asd.class, new AsdSerializer());
        Gson gson = builder.create();
        System.out.println(gson.toJson(entity));
    }
}
