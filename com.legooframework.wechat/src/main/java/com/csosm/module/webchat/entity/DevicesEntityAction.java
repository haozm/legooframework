package com.csosm.module.webchat.entity;

import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DevicesEntityAction extends BaseEntityAction<DevicesEntity> {

    private static final Logger logger = LoggerFactory.getLogger(DevicesEntityAction.class);

    public DevicesEntityAction() {
        super("devices", null);
    }

    public int bildDeviceToStore(StoreEntity store, String deviceId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "待绑定的设备ID不可以为空....");
        Preconditions.checkNotNull(store, "待绑定设备的门店不可以为空....");
        Preconditions.checkArgument(store.getCompanyId().isPresent(), "当前门店无公司信息，无法执行绑定操作....");
        Optional<DevicesEntity> device = findByDeviceId(deviceId);
        if (device.isPresent()) {
            Preconditions.checkState(device.get().getStoreId().equals(store.getId()), "该设备绑定其他门店，无法再次绑定...");
            return 0;
        }
        DevicesEntity instance = DevicesEntity.godDevice(deviceId, store);
        getNamedParameterJdbcTemplate().update(getExecSql("insert", null), instance.toMap());
        return 1;
    }

    public Optional<DevicesEntity> findByWeixinId(String weixinId) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByWeixinId(String weixinId=%s )", weixinId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), "微信ID不可以为空....");
        Map<String, Object> params = Maps.newHashMap();
        params.put("weixinId", weixinId);
        List<DevicesEntity> devices = getNamedParameterJdbcTemplate()
                .query(getExecSql("findByWeixinId", null), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByWeixinId( weixinId:%s) size is %s", weixinId,
                    CollectionUtils.isEmpty(devices) ? 0 : devices.size()));
        if (CollectionUtils.isEmpty(devices)) return Optional.absent();
        Preconditions.checkState(devices.size() == 1, String.format("微信[%s] 绑定多个设备,数据异常...", weixinId));
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByWeixinId(%s) return -> devices is %s", weixinId, devices.get(0)));
        return Optional.of(devices.get(0));
    }

    // 通过门店获取全部设备
    public Optional<List<DevicesEntity>> findAllByStore(StoreEntity store) {
        Preconditions.checkNotNull(store, "请指定所要设备输在的门店.");
        Preconditions.checkArgument(store.getCompanyId().isPresent(), "当前门店 %s 无公司信息.",
                store.getCompanyId().isPresent());
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().get());
        List<DevicesEntity> devices = getNamedParameterJdbcTemplate()
                .query(getExecSql("findAllByStore", null), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findAllByStore(%s) return -> devices size is %s", store.getName(),
                    CollectionUtils.isEmpty(devices) ? 0 : devices.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(devices) ? null : devices);
    }

    // 通过门店获取全部设备
    public Optional<List<DevicesEntity>> findAllGodByStores(Collection<StoreEntity> stores) {
        Preconditions.checkArgument(!CollectionUtils.isEmpty(stores), "请指定所要设备输在的门店.");
        for (StoreEntity store : stores)
            Preconditions.checkArgument(store.getCompanyId().isPresent(), "当前门店 %s 无公司信息.",
                    store.getCompanyId().isPresent());
        Map<String, Object> params = Maps.newHashMap();
        List<Integer> storeIds = Lists.newArrayList();
        for (StoreEntity store : stores) storeIds.add(store.getId());
        params.put("storeIds", storeIds);
        params.put("companyId", stores.iterator().next().getCompanyId().get());
        List<DevicesEntity> devices = getNamedParameterJdbcTemplate()
                .query(getExecSql("findAllGodByStores", null), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findAllGodByStores(%s) return -> devices size is %s", storeIds,
                    CollectionUtils.isEmpty(devices) ? 0 : devices.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(devices) ? null : devices);
    }

    // 查询获取 指定门店的 上帝手机  不存在无错误
    // 目前仅支持门店唯一的情况
    public Optional<List<DevicesEntity>> findGodDeviceByStore(StoreEntity store) {
        //TODO
        Preconditions.checkNotNull(store, "请指定所要设备输在的门店.");
        Preconditions.checkArgument(store.getCompanyId().isPresent(), "当前门店无公司信息.");
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().get());
        List<DevicesEntity> devices = getNamedParameterJdbcTemplate()
                .query(getExecSql("findGodDeviceByStore", null), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findGodDeviceByStore(%s) return -> devices is %s", store.getName(), devices));
        if (CollectionUtils.isEmpty(devices)) return Optional.absent();
        return Optional.fromNullable(devices);
    }

    public Optional<List<DevicesEntity>> findGodDeviceByIds(OrganizationEntity company, String... deviceIds) {
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(deviceIds), "待查询的设备Id不可以为空....");
        Map<String, Object> params = Maps.newHashMap();
        params.put("deviceIds", deviceIds);
        params.put("companyId", company.getId());
        List<DevicesEntity> devices = getNamedParameterJdbcTemplate()
                .query(getExecSql("findGodDeviceByIds", params), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findGodDeviceByIds(%s) return -> devices is %s", company.getName(), devices.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(devices) ? null : devices);
    }

    public Optional<DevicesEntity> findByDeviceId(String deviceId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "设备ID不可以为空....");
        Map<String, Object> params = Maps.newHashMap();
        params.put("deviceId", deviceId);
        DevicesEntity device = getNamedParameterJdbcTemplate()
                .query(getExecSql("findByDeviceId", null), params, getResultSetExtractor());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByDeviceId(%s) return -> devices is %s", deviceId, device));
        return Optional.fromNullable(device);
    }

    public Optional<DevicesEntity> findByDeviceIdWithAll(String deviceId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "设备ID不可以为空....");
        Map<String, Object> params = Maps.newHashMap();
        params.put("deviceId", deviceId);
        DevicesEntity device = getNamedParameterJdbcTemplate()
                .query(getExecSql("findByDeviceIdWithAll", null), params, getResultSetExtractor());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByDeviceIdWithAll(%s) return -> devices is %s", deviceId, device));
        return Optional.fromNullable(device);
    }

    public void changeDeivce(String oldDeviceId, String newDeivceId) {
        Optional<DevicesEntity> device = findByDeviceIdWithAll(oldDeviceId);
        Preconditions.checkState(device.isPresent(), "Id=%s 对应的设备不存在...", oldDeviceId);
        if (StringUtils.equals(oldDeviceId, newDeivceId)) return;
        DevicesEntity _clone = device.get().disabled();
        getJdbc().update(getExecSql("disabledDevice", null), _clone.toMap());
        Optional<DevicesEntity> device_new = findByDeviceIdWithAll(newDeivceId);
        if (device_new.isPresent()) {
            _clone = device.get().changeDevice(newDeivceId);
            getJdbc().update(getExecSql("changeDevice", null), _clone.toMap());
        } else {
            _clone = device.get().changeDevice(newDeivceId);
            getJdbc().update(getExecSql("addNewVDevice", null), _clone.toMap());
        }
    }

    // 查询获取 指定门店的 上帝手机  不存在提示异常
    public List<DevicesEntity> loadGodDeviceByStore(StoreEntity store) {
        Optional<List<DevicesEntity>> optional = findGodDeviceByStore(store);
        Preconditions.checkState(optional.isPresent(), "门店%s未绑定微信手机.", store.getName());
        return optional.get();
    }

    @Override
    protected ResultSetExtractor<DevicesEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class RowMapperImpl implements RowMapper<DevicesEntity> {
        @Override
        public DevicesEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<DevicesEntity> {

        @Override
        public DevicesEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return buildByResultSet(resultSet);
            }
            return null;
        }
    }

    private DevicesEntity buildByResultSet(ResultSet resultSet) throws SQLException {
        // String id, Integer storeId, Integer companyId, String type weixinId,createtime
        return new DevicesEntity(
                resultSet.getString("deviceId"),
                resultSet.getInt("storeId"),
                resultSet.getInt("companyId"),
                resultSet.getString("type"),
                resultSet.getString("weixinId"),
                resultSet.getDate("createtime"),
                resultSet.getInt("status"));
    }


}
