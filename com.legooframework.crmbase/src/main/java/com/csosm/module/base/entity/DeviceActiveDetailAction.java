package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class DeviceActiveDetailAction extends BaseEntityAction<DeviceActiveDetailEntity> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceActiveDetailAction.class);

    public DeviceActiveDetailAction() {
        super("DeviceActiveDetailEntity", null);
    }

    public Optional<DeviceActiveDetailEntity> findByPinCode(String pinCode) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("pinCode", pinCode);
        try {
            DeviceActiveDetailEntity exits = getNamedParameterJdbcTemplate()
                    .queryForObject(getExecSql("select_by_pincode", null), params, new RowMapperImpl());
            return Optional.fromNullable(exits);
        } catch (EmptyResultDataAccessException e) {
            return Optional.absent();
        }
    }

    public boolean recodeActiveDetail(StoreEntity store, String pinCode, String deviceId) {
        Preconditions.checkNotNull(store);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId));
        Optional<DeviceActiveDetailEntity> pinCodeEntity = findByPinCode(pinCode);
        if (pinCodeEntity.isPresent()) {
            Preconditions.checkState(pinCodeEntity.get().equals(store, deviceId), "数据异常，序列号已经被使用....");
            return true;
        } else {
            DeviceActiveDetailEntity instance = new DeviceActiveDetailEntity(store, pinCode, deviceId);
            int res = getNamedParameterJdbcTemplate().update(getExecSql("bild_store", null), instance.toMap());
            Preconditions.checkState(1 == res, "通过pingCode =%s 码，持久化绑定门店信息失败，", pinCode);
            return true;
        }
    }

    @Override
    protected ResultSetExtractor<DeviceActiveDetailEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class RowMapperImpl implements RowMapper<DeviceActiveDetailEntity> {
        @Override
        public DeviceActiveDetailEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<DeviceActiveDetailEntity> {

        @Override
        public DeviceActiveDetailEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return buildByResultSet(resultSet);
            }
            return null;
        }
    }

    private DeviceActiveDetailEntity buildByResultSet(ResultSet resultSet) throws SQLException {
        return new DeviceActiveDetailEntity(resultSet.getLong("id"), resultSet.getObject("createUserId"),
                resultSet.getDate("createTime"), resultSet.getInt("companyId"), resultSet.getString("pinCode"),
                resultSet.getInt("storeId"), resultSet.getInt("enabled") == 1, resultSet.getString("deviceId"),
                resultSet.getDate("bindDate"));
    }
}
