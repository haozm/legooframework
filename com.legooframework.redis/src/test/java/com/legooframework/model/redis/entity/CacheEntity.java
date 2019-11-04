package com.legooframework.model.redis.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.joda.time.LocalDate;

public class CacheEntity extends BaseEntity<Integer> implements JsonSerializer {

    private String name, address;
    private int sex;
    private LocalDate biredy;

    protected CacheEntity(Integer id, String name, int sex, LocalDate biredy, String address) {
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
}
