package com.legooframework.model.regiscenter.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.organization.entity.CompanyEntity;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DevicePinCodeEntityAction extends BaseEntityAction<DevicePinCodeEntity> {

    private static final Logger logger = LoggerFactory.getLogger(DevicePinCodeEntityAction.class);

    public DevicePinCodeEntityAction() {
        super(null);
    }

    public boolean activeDeviceId(String pinCode, String deviceId) {
        Optional<List<DevicePinCodeEntity>> exits = findByCodeOrDeviceId(pinCode, deviceId);
        Preconditions.checkState(exits.isPresent(), "非法的 pinCode = %s", pinCode);
        if (exits.get().size() != 1)
            throw new IllegalArgumentException(String.format("pinCode=%s 或者 deviceId=%s 已经被注册 ...",
                    pinCode, deviceId));
        DevicePinCodeEntity em = exits.get().get(0);
        Preconditions.checkState(StringUtils.equals(em.getPinCode(), pinCode), "非法的 pinCode = %s", pinCode);
        Optional<DevicePinCodeEntity> clone = em.activeDeviceId(deviceId);
        clone.ifPresent(devicePinCodeEntity -> super.updateAction(devicePinCodeEntity, "activeDeviceId"));
        return clone.isPresent();
    }

    public Collection<String> batchCreatePinCodes(CompanyEntity company, Date deadline, int size) {
        Preconditions.checkNotNull(company);
        Preconditions.checkArgument(size > 0);
        Set<String> pin_codes = Sets.newHashSet();
        for (int i = 0; i < size * 2; i++) pin_codes.add(String.valueOf(RandomUtils.nextInt(100000, 999999)));
        Map<String, Object> params = Maps.newHashMap();
        params.put("pinCodes", pin_codes);
        Optional<List<DevicePinCodeEntity>> exits = queryForEntities("findByPinCodes", params, getRowMapper());
        if (exits.isPresent()) {
            Set<String> _temp = exits.get().stream().map(DevicePinCodeEntity::getPinCode).collect(Collectors.toSet());
            pin_codes.removeAll(_temp);
        }
        Preconditions.checkElementIndex(size, pin_codes.size(), "数据异常，有效序列号有重复，请重新生成...");
        int index = 1;
        List<String> res = Lists.newArrayListWithCapacity(size);
        List<DevicePinCodeEntity> entities = Lists.newArrayListWithCapacity(size);
        for (String $it : pin_codes) {
            entities.add(new DevicePinCodeEntity(company, $it, deadline, LoginContextHolder.get()));
            index++;
            res.add($it);
            if (index == size) break;
        }
        super.batchInsert("batchInsert", entities);
        return res;
    }

    public Optional<DevicePinCodeEntity> findByCode(String pinCode) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(pinCode), "pinCode 入参非法...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("pinCode", pinCode);
        return queryForEntity("findByCode", params, getRowMapper());
    }

    public Optional<List<DevicePinCodeEntity>> findByCodeOrDeviceId(String pinCode, String deviceId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(pinCode), "pinCode 入参非法...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "deviceId 入参非法...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("pinCode", pinCode);
        params.put("deviceId", deviceId);
        return queryForEntities("findByCodeAndDevice", params, getRowMapper());
    }

    public Optional<DevicePinCodeEntity> findByDeviceId(String deviceId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "deviceId 入参非法...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("deviceId", deviceId);
        return queryForEntity("findByDeviceId", params, getRowMapper());
    }

    @Override
    protected RowMapper<DevicePinCodeEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<DevicePinCodeEntity> {
        @Override
        public DevicePinCodeEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new DevicePinCodeEntity(res.getLong("id"), res);
        }
    }

}
