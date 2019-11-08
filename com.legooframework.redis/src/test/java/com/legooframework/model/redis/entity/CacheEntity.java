package com.legooframework.model.redis.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.GsonSerializer;
import com.legooframework.model.core.utils.GsonUtil;
import org.joda.time.LocalDate;

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
        CacheEntity cacheEntity = new CacheEntity(1, String.format("HXJ-%d", 1), 2 % 2, LocalDate.now(), String.format("GZ-%d", 2));
        System.out.println(GsonUtil.serialize(cacheEntity));
        String json = GsonUtil.serialize(cacheEntity);
        cacheEntity = GsonUtil.deserialize(json, CacheEntity.class);
        System.out.println(cacheEntity);
    }
}
