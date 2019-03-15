package com.legooframework.model.regiscenter.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StoreActiveInfoEntityAction extends BaseEntityAction<StoreActiveInfoEntity> {

    private static final Logger logger = LoggerFactory.getLogger(StoreActiveInfoEntityAction.class);

    public StoreActiveInfoEntityAction() {
        super(null);
    }

    public Optional<StoreActiveInfoEntity> findByDeviceId(String deviceId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "参数 deviceId 不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("deviceId", deviceId);
        Optional<StoreActiveInfoEntity> storeActive = super.queryForEntity("findByDeviceId", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByDeviceId(%s) return %s", deviceId, storeActive.orElse(null)));
        return storeActive;
    }

    Optional<List<StoreActiveInfoEntity>> findByStore(CrmStoreEntity store) {
        Preconditions.checkNotNull(store, "参数 store 不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId());
        Optional<List<StoreActiveInfoEntity>> storeActives = super.queryForEntities("findByStore", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByStore(%s) return %s", store.getId(), storeActives.orElse(null)));
        return storeActives;
    }

    public void activeDevice(CrmStoreEntity store, String deviceId) {
        Optional<StoreActiveInfoEntity> storeActiveInfoOpt = findByDeviceId(deviceId);
        if (storeActiveInfoOpt.isPresent()) {
            Preconditions.checkState(storeActiveInfoOpt.get().sameStore(store), "该设备%s 已经绑定其他门店，无法重复激活...",
                    deviceId);
            return;
        }
        StoreActiveInfoEntity instance = new StoreActiveInfoEntity(store, LocalDate.now(), deviceId);
        super.updateAction(instance, "insert");
    }

    public void changeDevice(CrmStoreEntity store, DevicePinCodeEntity pinCode, String newDeviceId) {
        Preconditions.checkNotNull(pinCode);
        Preconditions.checkState(pinCode.getDeviceId().isPresent());
        Optional<StoreActiveInfoEntity> sheep = findByDeviceId(pinCode.getDeviceId().get());
        if (sheep.isPresent()) {
            Preconditions.checkState(sheep.get().sameStore(store), "该设备%s 已经绑定其他门店，无法重复激活...",
                    pinCode.getDeviceId().get());
            Optional<StoreActiveInfoEntity> clone = sheep.get().changeDevice(newDeviceId);
            clone.ifPresent(x -> super.updateAction(x, "changeDevice"));
        } else {
            Preconditions.checkState(pinCode.getBindingDate().isPresent());
            StoreActiveInfoEntity instance = new StoreActiveInfoEntity(store, pinCode.getBindingDate().get(), newDeviceId);
            super.updateAction(instance, "insert");
        }
    }


    @Override
    protected RowMapper<StoreActiveInfoEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<StoreActiveInfoEntity> {
        @Override
        public StoreActiveInfoEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new StoreActiveInfoEntity(res.getLong("id"), res);
        }
    }
}
