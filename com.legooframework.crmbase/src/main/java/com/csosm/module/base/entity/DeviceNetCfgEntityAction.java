package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DeviceNetCfgEntityAction extends BaseEntityAction<DeviceNetCfgEntity> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceNetCfgEntityAction.class);

    public DeviceNetCfgEntityAction() {
        super("DeviceNetCfgEntity", null);
    }

    public void addByCompany(OrganizationEntity company, String centerId, String udpDomain,
                             String uploadDomain, int updPort, int uploadPort, int udpPageSize, int msgDelayTime,
                             int keepliveDelayTime) {
        DeviceNetCfgEntity entity = new DeviceNetCfgEntity(company, centerId, udpDomain,
                uploadDomain, updPort, uploadPort, udpPageSize, msgDelayTime, keepliveDelayTime);
        Optional<DeviceNetCfgEntity> exits = loadByCompany(company);
        Preconditions.checkState(!exits.isPresent(), "已经存在公司%s的参数设置...", company.getName());
        getJdbc().update(getExecSql("insert", null), entity.toMap());
    }

    public void addByStore(StoreEntity store, DeviceNetCfgEntity com_net_cfg, int udpPageSize, int msgDelayTime, int keepliveDelayTime) {
        Preconditions.checkNotNull(com_net_cfg);
        DeviceNetCfgEntity entity = new DeviceNetCfgEntity(store, com_net_cfg.getCenterId(), com_net_cfg.getUdpDomain(),
                com_net_cfg.getUploadDomain(), com_net_cfg.getUpdPort(), com_net_cfg.getUploadPort(),
                udpPageSize, msgDelayTime, keepliveDelayTime);
        Optional<DeviceNetCfgEntity> exits = loadDeviceNetCfg(store);
        if (exits.isPresent() && exits.get().hasStore())
            throw new IllegalStateException(String.format("已经存在门店%s的参数设置...", store.getName()));
        getJdbc().update(getExecSql("insert", null), entity.toMap());
    }

    public void change(long id, int udpPageSize, int msgDelayTime, int keepliveDelayTime) {
        Optional<DeviceNetCfgEntity> opt = findById(id);
        Preconditions.checkState(opt.isPresent(), "不存在ID=%s对应的配置", id);
        Optional<DeviceNetCfgEntity> clone = opt.get().change(udpPageSize, msgDelayTime, keepliveDelayTime);
        if (clone.isPresent()) {
            getJdbc().update(getExecSql("update", null), clone.get().toMap());
        }
    }

    @Override
    public Optional<DeviceNetCfgEntity> findById(Object id) {
        Preconditions.checkNotNull(id, "入参ID不可为空值...");
        DeviceNetCfgEntity confing = super.selectById(id);
        if (logger.isDebugEnabled())
            logger.debug(String.format("findById(%s) res is %s", id, confing));
        return Optional.fromNullable(confing);
    }

    public Optional<DeviceNetCfgEntity> loadByCompany(OrganizationEntity company) {
        Optional<List<DeviceNetCfgEntity>> res = loadAllByCompanyId(company.getId(), true);
        if (!res.isPresent()) return Optional.absent();
        Preconditions.checkState(res.get().size() == 1, "数据异常，存在重复数据...");
        return Optional.of(res.get().get(0));
    }

    public Optional<List<DeviceNetCfgEntity>> loadAllByCompany(OrganizationEntity company) {
        return loadAllByCompanyId(company.getId(), false);
    }

    public Optional<List<DeviceNetCfgEntity>> loadAllByCompanyId(Integer companyId, boolean companyOnly) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("companyOnly", companyOnly);
        List<DeviceNetCfgEntity> configs = getJdbc().query(getExecSql("findByCompanyId", params), params,
                new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByCompanyId(%s) size is %s", companyId, CollectionUtils.isEmpty(configs)
                    ? 0 : configs.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(configs) ? null : configs);
    }

    /**
     * @param store
     * @return
     */
    public Optional<DeviceNetCfgEntity> loadDeviceNetCfg(StoreEntity store) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadDeviceNetCfg(StoreEntity %s)", store.toString()));
        Preconditions.checkNotNull(store);
        Preconditions.checkState(store.getCompanyId().isPresent());
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().get());
        List<DeviceNetCfgEntity> configs = getNamedParameterJdbcTemplate()
                .query(getExecSql("loadDeviceNetCfg", null), params, new RowMapperImpl());
        if (org.springframework.util.CollectionUtils.isEmpty(configs)) return Optional.absent();
        if (configs.size() == 1) return Optional.of(configs.get(0));
        DeviceNetCfgEntity res = null;
        for (DeviceNetCfgEntity $it : configs) {
            if ($it.hasStore()) {
                res = $it;
                break;
            }
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadDeviceNetCfg(StoreEntity %s), return %s", store.getId(), res.toString()));
        return Optional.fromNullable(res);
    }

    @Override
    protected ResultSetExtractor<DeviceNetCfgEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class RowMapperImpl implements RowMapper<DeviceNetCfgEntity> {
        @Override
        public DeviceNetCfgEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<DeviceNetCfgEntity> {

        @Override
        public DeviceNetCfgEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return buildByResultSet(resultSet);
            }
            return null;
        }
    }

    private DeviceNetCfgEntity buildByResultSet(ResultSet res) throws SQLException {
//        Long id, Integer companyId, Integer storeId, String centerId, String udpDomain,
//        String uploadDomain, int updPort, int uploadPort, int udpPageSize, int msgDelayTime,
//        int keepliveDelayTime
        return new DeviceNetCfgEntity(res.getLong("id"), res.getInt("companyId"), res.getInt("storeId"), res.getString("centerId"),
                res.getString("udpDomain"), res.getString("uploadDomian"), res.getInt("udpPort"),
                res.getInt("uploadPort"), res.getInt("updPageSize"), res.getInt("msgDelayTime"), res.getInt("keepliveDelayTime"),
                res.getString("company"), res.getString("storeName"));
    }
}
