package com.csosm.module.webchat.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class WxMsgWhiteListAction extends BaseEntityAction<WxMsgWhiteListEntity> {

    private static final Logger logger = LoggerFactory.getLogger(WxMsgWhiteListAction.class);

    public WxMsgWhiteListAction() {
        super("WxMsgWhiteListEntity", "defCache");
    }

    @Override
    public Optional<WxMsgWhiteListEntity> findById(Object id) {
        throw new UnsupportedOperationException();
    }

    public Optional<WxMsgWhiteListEntity> findByStore(StoreEntity store) {
        Preconditions.checkNotNull(store);
        Preconditions.checkState(store.getCompanyId().isPresent());
        final String cache_key = String.format("%s_store_%s", getModel(), store.getId());
        if (getCache().isPresent()) {
            WxMsgWhiteListEntity cached = (WxMsgWhiteListEntity) getCache().get().getIfPresent(cache_key);
            if (cached != null) {
                if (logger.isDebugEnabled())
                    logger.debug(String.format("Hit Cache(%s) return -> WxMsgWhiteListEntity is %s", cache_key, cached));
                return Optional.of(cached);
            }
        }

        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().get());
        WxMsgWhiteListEntity entity = getNamedParameterJdbcTemplate()
                .query(getExecSql("findByStore", null), params, getResultSetExtractor());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByStore(%s) return -> WxMsgWhiteListEntity is %s", store.getName(), entity));
        if (getCache().isPresent() && entity != null) getCache().get().put(cache_key, entity);
        return Optional.fromNullable(entity);
    }

    public Optional<List<Map<String, Object>>> findWhitListByStore(StoreEntity store) {
        Optional<WxMsgWhiteListEntity> exits = findByStore(store);
        if (!exits.isPresent()) return Optional.absent();
        if (!exits.get().getIncloudIds().isPresent()) return Optional.absent();
        Map<String, Object> params = exits.get().toMap();
        params.put("employIds", exits.get().getIncloudIds().get());
        List<Map<String, Object>> list = getNamedParameterJdbcTemplate()
                .queryForList(getExecSql("findWhitListByStore", params), (Map<String, ?>) null);
        return Optional.fromNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

    public void addWhiteList(StoreEntity store, EmployeeEntity employee, LoginUserContext loginUserContext) {
        Preconditions.checkNotNull(store);
        Preconditions.checkNotNull(employee);
        Preconditions.checkState(store.getCompanyId().isPresent());
        Optional<WxMsgWhiteListEntity> exits = findByStore(store);
        if (exits.isPresent()) {
            Optional<WxMsgWhiteListEntity> clone = exits.get().addWithList(employee);
            if (!clone.isPresent()) return;
            Map<String, Object> params = loginUserContext.toMap();
            params.putAll(clone.get().toMap());
            getNamedParameterJdbcTemplate().update(getExecSql("updateWhiteList", null), params);
        } else {
            WxMsgWhiteListEntity save_entity = new WxMsgWhiteListEntity(employee, store);
            Map<String, Object> params = loginUserContext.toMap();
            params.putAll(save_entity.toMap());
            getNamedParameterJdbcTemplate().update(getExecSql("insert", null), params);
        }
        invalidate(store);
    }

    public void removeWhiteList(StoreEntity store, EmployeeEntity employee, LoginUserContext loginUserContext) {
        Preconditions.checkNotNull(store);
        Preconditions.checkNotNull(employee);
        Preconditions.checkState(store.getCompanyId().isPresent());
        Optional<WxMsgWhiteListEntity> exits = findByStore(store);
        if (!exits.isPresent()) return;
        Optional<WxMsgWhiteListEntity> clone = exits.get().removeWithList(employee);
        if (!clone.isPresent()) return;
        Map<String, Object> params = loginUserContext.toMap();
        params.putAll(clone.get().toMap());
        getNamedParameterJdbcTemplate().update(getExecSql("updateWhiteList", null), params);
        invalidate(store);
    }

    protected void invalidate(StoreEntity store) {
        if (getCache().isPresent()) {
            final String cache_key = String.format("%s_store_%s", getModel(), store.getId());
            getCache().get().invalidate(cache_key);
        }
    }

    public void switchProhibit(StoreEntity store, LoginUserContext loginUserContext, boolean prohibitTag) {
        Preconditions.checkNotNull(store);
        Preconditions.checkNotNull(loginUserContext);
        Optional<WxMsgWhiteListEntity> exits = findByStore(store);
        if (exits.isPresent()) {
            Optional<WxMsgWhiteListEntity> clone = prohibitTag ? exits.get().close() : exits.get().open();
            if (!clone.isPresent()) return;
            Map<String, Object> params = loginUserContext.toMap();
            params.putAll(clone.get().toMap());
            getNamedParameterJdbcTemplate().update(getExecSql("updateSwitch", null), params);
        } else {
            WxMsgWhiteListEntity save_entity = new WxMsgWhiteListEntity(store, prohibitTag);
            Map<String, Object> params = loginUserContext.toMap();
            params.putAll(save_entity.toMap());
            getNamedParameterJdbcTemplate().update(getExecSql("insert", null), params);
        }
        invalidate(store);
    }


    @Override
    protected ResultSetExtractor<WxMsgWhiteListEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<WxMsgWhiteListEntity> {

        @Override
        public WxMsgWhiteListEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                // Integer id, Object createUserId, Date createTime, ResultSet res
                return new WxMsgWhiteListEntity(resultSet.getInt("id"),
                        resultSet.getObject("createUserId"),
                        resultSet.getDate("createTime"), resultSet);
            }
            return null;
        }
    }
}
