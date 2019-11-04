package com.legooframework.model.redis.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.joda.time.LocalDate;

import java.lang.reflect.Constructor;
import java.util.Map;

public class CacheEntity extends BaseEntity<Integer> implements JsonSerializer {

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
        params.put("biredy", biredy.toString("yyyy-MM-dd"));
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


    public static void main(String[] args) throws Exception {
        CacheEntity asd = new CacheEntity(1, "hjaopx", 2, LocalDate.now(), "guagnz");
        String str = asd.toString();
        System.out.println(str);
        Class<?> clazz = Class.forName("com.legooframework.model.redis.entity.CacheEntity");
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor a : constructors) {
            System.out.println(a);
        }
        System.out.println("end");
    }
}
