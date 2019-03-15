package com.legooframework.model.regiscenter.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.LocalDateTime;
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

    public void activeDevice(String pinCode, String deviceId, CrmStoreEntity store) {
        Optional<DevicePinCodeEntity> exits = findByCode(pinCode);
        Preconditions.checkState(exits.isPresent(), "pinCode=%s 非法...", pinCode);
        Optional<DevicePinCodeEntity> clone = exits.get().activeDeviceId(deviceId, store);
        clone.ifPresent(p -> super.updateAction(p, "activeDeviceId"));
    }

    /**
     * 批量增加 pincode 指定公司
     *
     * @param company
     * @param size
     * @return
     */
    public Collection<Integer> batchCreatePinCodes(CrmOrganizationEntity company, int size) {
        Preconditions.checkNotNull(company);
        Preconditions.checkArgument(size > 0);
        String batchNo = String.format("%s-%s", LocalDateTime.now().toString("yyMMddHHmmss"), company.getId());
        Set<Integer> pin_codes = Sets.newHashSet();
        for (int i = 0; i < size * 3; i++) pin_codes.add(RandomUtils.nextInt(100000, 999999));
        Map<String, Object> params = Maps.newHashMap();
        params.put("pinCodes", pin_codes);
        Optional<List<Map<String, Object>>> exits = queryForMapList("findByPinCodes", params);
        if (exits.isPresent()) {
            Set<Integer> _temp = exits.get().stream().map(x -> MapUtils.getInteger(x, "pinCode")).collect(Collectors.toSet());
            pin_codes.removeAll(_temp);
        }
        int index = 1;
        List<Integer> res = Lists.newArrayListWithCapacity(size);
        List<DevicePinCodeEntity> entities = Lists.newArrayListWithCapacity(size);
        for (Integer $it : pin_codes) {
            entities.add(new DevicePinCodeEntity(company, String.valueOf($it), batchNo, LoginContextHolder.get()));
            res.add($it);
            index++;
            if (index == size + 1) break;
        }
        super.batchInsert("batchInsert", entities);
        return res;
    }

    /**
     * 获取指定批次号的 pincode
     *
     * @param batchNo
     * @param company
     * @return
     */
    public Optional<List<DevicePinCodeEntity>> loadByBatchNo(String batchNo, CrmOrganizationEntity company) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(batchNo), "批次号 batchNo 不可以为空值...");
        Preconditions.checkNotNull(company, "所属公司 company 不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("batchNo", batchNo);
        params.put("companyId", company.getId());
        Optional<List<DevicePinCodeEntity>> res = super.queryForEntities("findByBatchNo", params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByBatchNo(%s,%s) size is %s", batchNo, company.getId(),
                    res.isPresent() ? res.get().size() : 0));
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

    public void changeDevice(DevicePinCodeEntity pinCode, String newDeviceId, CrmStoreEntity store) {
        Preconditions.checkNotNull(pinCode, "参数 DevicePinCodeEntity 不可以为空值...");
        Optional<DevicePinCodeEntity> disabled = pinCode.disabled();
        disabled.ifPresent(c -> super.updateAction(c, "disabledByDevice"));
        Optional<DevicePinCodeEntity> newDevices = pinCode.changeDivece(newDeviceId, store);
        newDevices.ifPresent(o -> super.updateAction(o, "changeDevice"));
        if (logger.isDebugEnabled())
            logger.debug(String.format("新设备 %s 注册，注销设备使用 %s", newDeviceId, pinCode.getDeviceId()));
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
