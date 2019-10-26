package com.legooframework.model.covariant.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.jdbc.ExecJdbcSqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.Map;
import java.util.Optional;

public class WxUserEntityAction extends BaseEntityAction<WxUserEntity> {

    private static final Logger logger = LoggerFactory.getLogger(WxUserEntityAction.class);

    public WxUserEntityAction() {
        super(null);
    }

    public Optional<WxUserEntity> findById(StoEntity store, Object id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("TABLE_NAME", String.format("CONTACT_%d_%d", store.getCompanyId(), store.getId()));
        params.put("userName", id);
        params.putAll(store.toParamMap());
        params.put("sql", "findById");
        Optional<WxUserEntity> wxUserEntity = super.queryForEntity("query4list", params, getRowMapper());
        wxUserEntity.ifPresent(wx -> wx.setAddInfo(store.getCompanyId(), store.getId()));
        if (logger.isDebugEnabled())
            logger.debug(String.format("findById(%s,%s) is %s", store.getId(), id, wxUserEntity.orElse(null)));
        return wxUserEntity;
    }

    public Optional<WxUserEntity> findByMember(MemberEntity member) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("TABLE_NAME", String.format("CONTACT_%d_%d", member.getCompanyId(), member.getStoreId()));
        params.put("memberId", member.getId());
        params.putAll(member.toParamMap());
        params.put("sql", "findByMember");
        try {
            Optional<WxUserEntity> wxUserEntity = super.queryForEntity("query4list", params, getRowMapper());
            wxUserEntity.ifPresent(wx -> wx.setAddInfo(member.getCompanyId(), member.getStoreId()));
            if (logger.isDebugEnabled())
                logger.debug(String.format("findByMember(%s) is %s", member.getId(), wxUserEntity.orElse(null)));
            return wxUserEntity;
        } catch (ExecJdbcSqlException e) {
            return Optional.empty();
        }
    }

    @Override
    protected RowMapper<WxUserEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<WxUserEntity> {
        @Override
        public WxUserEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new WxUserEntity(resultSet.getString("userName"), resultSet);
        }
    }
}
