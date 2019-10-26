package com.csosm.module.webchat.entity;

import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WechatAddFriendPushListAction extends BaseEntityAction<WechatAddFriendPushListEntity> {

    private static final Logger logger = LoggerFactory.getLogger(WechatAddFriendPushListAction.class);

    public WechatAddFriendPushListAction() {
        super("WechatAddFriendPushList", null);
    }

    public int batchInsert(Collection<WechatAddFriendPushListEntity> pushList) {
        if (CollectionUtils.isEmpty(pushList)) return 0;
        getJdbcTemplate().batchUpdate(getExecSql("batchInsert", null), pushList, 2000,
                new ParameterizedPreparedStatementSetter<WechatAddFriendPushListEntity>() {
                    @Override
                    public void setValues(PreparedStatement ps, WechatAddFriendPushListEntity entity) throws SQLException {
                        ps.setObject(1, entity.getStoreId());
                        ps.setObject(2, entity.getCompanyId());
                        ps.setObject(3, entity.getUserSize());
                        ps.setObject(4, entity.getPushInfosToString());
                        ps.setObject(5, entity.getUuid());
                    }
                });
        return pushList.size();
    }

    public int batchMarkPushed(Collection<WechatAddFriendPushListEntity> pushList) {
        if (CollectionUtils.isEmpty(pushList)) return 0;
        List<String> uuids = Lists.newArrayList();
        for (WechatAddFriendPushListEntity $it : pushList) uuids.add($it.getUuid());
        Map<String, Object> param = Maps.newHashMap();
        param.put("uuids", uuids);
        getNamedParameterJdbcTemplate().update(getExecSql("batchMarkPushed", param), param);
        return pushList.size();
    }

    public int batchMarkPushedMarked(Collection<WechatAddFriendPushListEntity> pushList) {
        if (CollectionUtils.isEmpty(pushList)) return 0;
        List<String> uuids = Lists.newArrayList();
        for (WechatAddFriendPushListEntity $it : pushList) uuids.add($it.getUuid());
        Map<String, Object> param = Maps.newHashMap();
        param.put("uuids", uuids);
        getNamedParameterJdbcTemplate().update(getExecSql("batchMarkPushedMarked", param), param);
        return pushList.size();
    }

    public Optional<WechatAddFriendPushListEntity> findByUUID(String uuid) {
        if (Strings.isNullOrEmpty(uuid)) return Optional.absent();
        Map<String, Object> param = Maps.newHashMap();
        param.put("uuid", uuid);
        WechatAddFriendPushListEntity enity = getNamedParameterJdbcTemplate()
                .queryForObject(getExecSql("findByUUID", param), param, new RowMapperImpl());
        return Optional.fromNullable(enity);
    }

    public Optional<List<WechatAddFriendPushListEntity>> loadByStoreAndDate(StoreEntity store, Date date) {
        Preconditions.checkNotNull(store, "入参门店不可以为空...");
        Date _date = date == null ? new Date() : date;
        Map<String, Object> param = Maps.newHashMap();
        param.put("storeId", store.getId());
        param.put("date", _date);
        List<WechatAddFriendPushListEntity> enities = getNamedParameterJdbcTemplate()
                .query(getExecSql("loadByStoreAndDate", param), param, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s:%s load WechatAddFriendPushListEntity is %s", store.getName(), _date, enities));
        return Optional.fromNullable(CollectionUtils.isEmpty(enities) ? null : enities);
    }

    public Optional<List<WechatAddFriendPushListEntity>> loadAllByAckList() {
        List<WechatAddFriendPushListEntity> enities = getNamedParameterJdbcTemplate()
                .query(getExecSql("loadAllByAckList", null), (Map<String, ?>) null, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByAckList is %s", CollectionUtils.isEmpty(enities) ? 0 : enities.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(enities) ? null : enities);
    }

    @Override
    protected ResultSetExtractor<WechatAddFriendPushListEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class RowMapperImpl implements RowMapper<WechatAddFriendPushListEntity> {
        @Override
        public WechatAddFriendPushListEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<WechatAddFriendPushListEntity> {

        @Override
        public WechatAddFriendPushListEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return buildByResultSet(resultSet);
            }
            return null;
        }
    }

    private WechatAddFriendPushListEntity buildByResultSet(ResultSet resultSet) throws SQLException {
//        Long id, Date createTime, Integer user_size, Integer storeId,
//                Integer companyId, int status, DateTime receiveDate, Set< WechatAddFriendPushListEntity.PushInfo > pushInfos
        Date receiveDate = resultSet.getDate("receiveDate");
//        Long id, Date createTime, Integer user_size, Integer storeId,
//                Integer companyId, int status, DateTime receiveDate, Set< WechatAddFriendPushListEntity.PushInfo > pushInfos
        String pushInfos = resultSet.getString("pushInfos");
        Map<String, String> maps = mapSplitter.split(pushInfos);
        return new WechatAddFriendPushListEntity(resultSet.getLong("id"), resultSet.getDate("createDate"),
                resultSet.getInt("userSize"), resultSet.getInt("storeId"), resultSet.getInt("companyId"),
                resultSet.getInt("status"), receiveDate == null ? null : new DateTime(receiveDate), maps,
                resultSet.getString("uuid"));
    }

    private static Splitter.MapSplitter mapSplitter = Splitter.on(',').withKeyValueSeparator(':');
}
