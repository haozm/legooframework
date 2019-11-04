package com.legooframework.model.redis.entity;

import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.StringSerializerHelper;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import java.util.StringJoiner;

public class CacheEntity extends BaseEntity<Integer> {

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

    static CacheEntity deserializer(String serializer) {
        String[] args = StringUtils.split(serializer, '|');
        return new CacheEntity(StringSerializerHelper.decodeInt(args[0], 0), StringSerializerHelper.decodeHex(args[1]),
                StringSerializerHelper.decodeInt(args[2], 0), StringSerializerHelper.decodeLocalDate(args[3], null),
                StringSerializerHelper.decodeHex(args[4]));
    }


    @Override
    public String serializer() {
        StringJoiner sj = new StringJoiner("|");
        sj.setEmptyValue(DEF_EMPTY).add(serializer(getId())).add(serializerHex(name))
                .add(serializer(sex)).add(serializer(biredy)).add(serializerHex(address));
        return sj.toString();
    }

}
