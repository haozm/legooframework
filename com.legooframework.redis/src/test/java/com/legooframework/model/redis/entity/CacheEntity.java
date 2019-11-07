package com.legooframework.model.redis.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.GsonSerializer;
import org.joda.time.LocalDate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class CacheEntity extends BaseEntity<Integer> implements GsonSerializer {

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("address", address)
                .add("sex", sex)
                .add("biredy", biredy)
                .toString();
    }


    public static void main(String[] args) {
        List<String> list = Lists.newArrayList();
        list.add("ASDads");

        Type genericType = list.getClass().getGenericSuperclass();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type[] types = pt.getActualTypeArguments();
            System.out.println(Arrays.toString(types));
        }
        System.out.println(genericType);
    }
}
