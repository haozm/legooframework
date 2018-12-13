package com.legooframework.model.organization.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.devices.entity.DeviceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EquipmentEntityAction extends BaseEntityAction<EquipmentEntity> {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentEntityAction.class);

    public EquipmentEntityAction() {
        super("OrganizationCache");
    }

    public String insertXDevice(DeviceEntity device, String remark, CompanyEntity company) {
        Preconditions.checkNotNull(device, "设备不可以为空.");
        Optional<EquipmentEntity> exits = findById(device.getImei());
        if (exits.isPresent()) {
            Preconditions.checkState(Longs.compare(exits.get().getTenantId(), company.getId()) == 0,
                    "设备%s 已经绑定其他公司%s，无法完成本次操作", device, exits.get().getTenantId());
            logger.warn(String.format("设备%s已经存在于当前公司%s,忽略本次新增操作.", device, company.getFullName()));
            return exits.get().getId();
        }
        EquipmentEntity entity = EquipmentEntity.createMainDev(device, remark, company);
        int res = super.updateAction(entity, "insert");
        Preconditions.checkState(1 == res, "新增设备%s到公司，写入数据库失败.", entity);
        if (logger.isDebugEnabled())
            logger.debug(String.format("新增设备%s 到租户%s成功.", entity, company.getFullName()));
        return device.getId();
    }

    public Optional<List<EquipmentEntity>> loadAllByCompany(CompanyEntity company) {
        Preconditions.checkNotNull(company, "入参 company 不可以为空.");
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", company.getId());
        return queryForEntities("loadAllByCompany", params, getRowMapper());
    }

    public Optional<List<EquipmentEntity>> loadAllByStore(StoreEntity store) {
        Preconditions.checkNotNull(store, "入参 Store 不可以为空.");
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        return queryForEntities("loadAllByStore", params, getRowMapper());
    }

    public Optional<List<EquipmentEntity>> loadAllByLoginUser(LoginContext loginUser) {
        Preconditions.checkNotNull(loginUser, "LoginContext loginUser 不可以为空.");
        if (!loginUser.getStoreIds().isPresent()) return Optional.empty();
        return queryForEntities("loadAllByLoginUser", loginUser.toParams(), getRowMapper());
    }

    public int enableDevice(String deviceId) {
        Optional<EquipmentEntity> exits = findById(deviceId);
        Preconditions.checkState(exits.isPresent(), "不存在ID=%s对应的设备.");
        Optional<EquipmentEntity> entity = exits.get().enabled();
        int res = entity.map(equipmentEntity -> super.updateAction(equipmentEntity, "enabled")).orElse(0);
        if (res == 1) evictEntity(exits.get());
        return res;
    }

    public int disabeldDevice(String deviceId) {
        Optional<EquipmentEntity> exits = findById(deviceId);
        Preconditions.checkState(exits.isPresent(), "不存在ID=%s对应的设备.");
        Optional<EquipmentEntity> entity = exits.get().enabled();
        int res = entity.map(equipmentEntity -> super.updateAction(equipmentEntity, "disabled")).orElse(0);
        if (res == 1) evictEntity(exits.get());
        return res;
    }

    // 激活设备
    public int activedDevice(String deviceId) {
        Optional<EquipmentEntity> exits = findById(deviceId);
        Preconditions.checkState(exits.isPresent(), "不存在ID=%s对应的设备...");
        Preconditions.checkState(exits.get().isEnabled(), "设备ID=%s处于停用状态，无法激活...");
        if (exits.get().isActivated()) return 0;
        int res = super.updateAction(exits.get(), "activate");
        if (res == 1) evictEntity(exits.get());
        return res;
    }

    @Override
    protected void cacheEntity(EquipmentEntity entity) {
        super.cacheEntity(entity);
        final String key = String.format("%s_imei_%s", getModelName(), entity.getDevice().getImei());
        getCache().ifPresent(c -> c.put(key, entity));
    }

    @Override
    protected void evictEntity(EquipmentEntity entity) {
        super.evictEntity(entity);
        final String key = String.format("%s_imei_%s", getModelName(), entity.getDevice().getImei());
        getCache().ifPresent(c -> c.put(key, entity));
    }

    @Override
    protected RowMapper<EquipmentEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<EquipmentEntity> {
        @Override
        public EquipmentEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new EquipmentEntity(ResultSetUtil.getString(res, "equipmentId"), res);
        }
    }
}
