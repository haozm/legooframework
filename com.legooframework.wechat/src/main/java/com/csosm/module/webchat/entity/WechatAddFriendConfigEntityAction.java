package com.csosm.module.webchat.entity;

import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WechatAddFriendConfigEntityAction extends BaseEntityAction<WechatAddFriendConfigEntity> {

    private static final Logger logger = LoggerFactory.getLogger(DevicesEntityAction.class);

    protected WechatAddFriendConfigEntityAction() {
        super("wechatAddFriendConfig", null);
    }

    public int countConfigs(OrganizationEntity company, Collection<StoreEntity> stores) {
        if (CollectionUtils.isEmpty(stores)) return 0;
        List<Integer> storeIds = Lists.newArrayList();
        for (StoreEntity store : stores) storeIds.add(store.getId());
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeIds", storeIds);
        params.put("companyId", company.getId());
        String sql = getExecSql("countByStores", params);
        Map<String, Object> result = getNamedParameterJdbcTemplate().queryForMap(sql, params);
        int count = MapUtils.getIntValue(result, "count");
        return count;
    }

    public List<WechatAddFriendConfigEntity> findByStores(Collection<StoreEntity> stores) {
        if (CollectionUtils.isEmpty(stores)) return Lists.newArrayList();
        List<Integer> storeIds = Lists.newArrayList();
        for (StoreEntity store : stores) storeIds.add(store.getId());
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeIds", storeIds);
        String sql = getExecSql("findByStoreIds", params);
        return getNamedParameterJdbcTemplate().query(sql, new ConfigListExtractor());
    }

    public Optional<WechatAddFriendConfigEntity> findByStore(StoreEntity store) {
        if (store == null)
            return Optional.absent();
        if (!store.getCompanyId().isPresent())
            return Optional.absent();
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().get());
        String sql = getExecSql("findByStoreId", params);
        WechatAddFriendConfigEntity entity = getNamedParameterJdbcTemplate().query(sql, params,
                new ResultSetExtractor<WechatAddFriendConfigEntity>() {

                    @Override
                    public WechatAddFriendConfigEntity extractData(ResultSet rs)
                            throws SQLException, DataAccessException {
                        while (rs.next()) {
                            return WechatAddFriendConfigEntity.valueOf(rs);
                        }
                        return null;
                    }
                });
        if (entity == null)
            return Optional.absent();
        return Optional.of(entity);
    }


    private void addPushConfig(Collection<WechatAddFriendConfigEntity> configs) {
        if (CollectionUtils.isEmpty(configs))
            return;
        batchInsert(configs);
    }

    private int batchInsert(Collection<WechatAddFriendConfigEntity> configs) {
        if (CollectionUtils.isEmpty(configs))
            return 0;
        getJdbcTemplate().batchUpdate(getExecSql("batchInsert", null), configs, 2000,
                new ParameterizedPreparedStatementSetter<WechatAddFriendConfigEntity>() {
                    @Override
                    public void setValues(PreparedStatement ps, WechatAddFriendConfigEntity entity)
                            throws SQLException {
                        ps.setObject(1, entity.getContent());
                        ps.setObject(2, entity.getSendNum());
                        ps.setObject(3, entity.getBeginTime());
                        ps.setObject(4, entity.getEndTime());
                        ps.setObject(5, entity.getEnable());
                        ps.setObject(6, entity.getRunTimes());
                        ps.setObject(7, entity.getStoreId());
                        ps.setObject(8, entity.getCompanyId());
                        ps.setObject(9, entity.getCreateUserId() == null ? -1 : entity.getCreateUserId());
                    }
                });
        return configs.size();
    }

    /**
     * 清除推送配置
     *
     * @param company
     * @param stores
     */
    public void clearPushConfigs(OrganizationEntity company, Collection<StoreEntity> stores) {
        if (CollectionUtils.isEmpty(stores))
            return;
        List<Integer> storeIds = Lists.newArrayList();
        for (StoreEntity store : stores)
            storeIds.add(store.getId());
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeIds", storeIds);
        params.put("companyId", company.getId());
        String sql = getExecSql("deleteConfigs", params);
        getNamedParameterJdbcTemplate().update(sql, params);
    }

    /**
     * 找某个公司下的门店列表的配置信息
     *
     * @param stores
     * @param company
     * @return
     */
    public List<WechatAddFriendConfigEntity> findByStores(Collection<StoreEntity> stores, OrganizationEntity company) {
        if (stores.isEmpty())
            return Lists.newArrayList();
        Map<String, Object> paramMap = Maps.newHashMap();
        List<Integer> storeIds = Lists.newArrayList();
        for (StoreEntity store : stores)
            storeIds.add(store.getId());
        paramMap.put("storeIds", storeIds);
        Integer companyId = null;
        if (company != null)
            companyId = company.getId();
        paramMap.put("companyId", companyId);
        String sql = getExecSql("findByStoreIds", paramMap);
        return getNamedParameterJdbcTemplate().query(sql, paramMap, new ConfigListExtractor());
    }

    private class ConfigListExtractor implements ResultSetExtractor<List<WechatAddFriendConfigEntity>> {

        @Override
        public List<WechatAddFriendConfigEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<WechatAddFriendConfigEntity> result = Lists.newArrayList();
            while (rs.next()) {
                result.add(WechatAddFriendConfigEntity.valueOf(rs));
            }
            return result;
        }
    }


    @Override
    protected ResultSetExtractor<WechatAddFriendConfigEntity> getResultSetExtractor() {
        // TODO Auto-generated method stub
        return null;
    }

    public void saveAndEnablePushConfigs(OrganizationEntity company, Collection<StoreEntity> stores, String content) {
        Objects.requireNonNull(company);
        if (CollectionUtils.isEmpty(stores)) return;
        List<WechatAddFriendConfigEntity> configs = Lists.newArrayList();
        for (StoreEntity store : stores) {
            WechatAddFriendConfigEntity config = new WechatAddFriendConfigEntity(content, store, company);
            config.enable();
            configs.add(config);
        }
        addPushConfig(configs);
    }
}
