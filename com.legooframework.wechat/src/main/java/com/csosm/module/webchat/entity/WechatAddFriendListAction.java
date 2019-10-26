package com.csosm.module.webchat.entity;

import com.csosm.commons.adapter.LoginUserContext;

import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.member.entity.MemberEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;

public class WechatAddFriendListAction extends BaseEntityAction<WechatAddFriendListEntity> {

    private static final Logger logger = LoggerFactory.getLogger(WechatAddFriendListAction.class);

    public WechatAddFriendListAction() {
        super("WechatAddFriendList", null);
    }

    public void batchInsertByMember(Collection<MemberEntity> members, LoginUserContext user) {
        if (CollectionUtils.isEmpty(members)) return;
        Preconditions.checkNotNull(user);
        final Object userId = user.getUserId();
        getJdbcTemplate().batchUpdate(getExecSql("batchInsert", null), members, 2000,
                new ParameterizedPreparedStatementSetter<MemberEntity>() {
                    //                    INSERT INTO acp.acp_wxuser_addlist
//                            (member_id, store_id, company_id, push_flag, creator, createTime)
//                    VALUES (    ?,             ?,         ?, 0,          ?,      NOW());
                    @Override
                    public void setValues(PreparedStatement ps, MemberEntity member) throws SQLException {
                        ps.setObject(1, member.getId());
                        ps.setObject(2, member.getStoreId().isPresent() ? member.getStoreId().get() : null);
                        ps.setObject(3, member.getCompanyId());
                        ps.setObject(4, 1);
                        ps.setObject(5, userId);
                    }
                });
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsertByMember() insert list size is %s", members.size()));
    }

    public void batchInsertByQuery(LoginUserContext user, Map<String, Object> params) {
        Preconditions.checkNotNull(user);
        params.put("userId", user.getUserId());
        getJdbcTemplate().execute(getExecSql("batchInsertBySelect", params));
    }

    public void removeByMembers(List<MemberEntity> members, StoreEntity store) {
        if (CollectionUtils.isEmpty(members)) return;
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        List<Integer> memberIds = Lists.newArrayListWithCapacity(members.size());
        for (MemberEntity $it : members) memberIds.add($it.getId());
        params.put("memberIds", memberIds);
        getNamedParameterJdbcTemplate().update(getExecSql("removeByMembers", params), params);
    }

    public void updateStatus4Pushing(List<WechatAddFriendPushListEntity> pushList) {
        if (CollectionUtils.isEmpty(pushList)) return;
        List<Long> ids = Lists.newArrayList();
        for (WechatAddFriendPushListEntity $it : pushList) ids.addAll($it.getAddIds());
        Map<String, Object> params = Maps.newHashMap();
        params.put("addIds", ids);
        super.getNamedParameterJdbcTemplate().update(getExecSql("updateStatus4Pushing", params), (Map<String, ?>) null);
    }

    public void updateStatus4Pushed(List<WechatAddFriendPushListEntity> pushList) {
        if (CollectionUtils.isEmpty(pushList)) return;
        List<Map<String, Object>> params = Lists.newArrayList();
        for (WechatAddFriendPushListEntity $it : pushList) {
            Map<String, Object> param = Maps.newHashMap();
            param.put("addIds", $it.getAddIds());
            param.put("recDate", $it.getReceiveDate().toDate());
            params.add(param);
        }
        for (Map<String, Object> $p : params) {
            super.getNamedParameterJdbcTemplate().update(getExecSql("updateStatus4Pushed", $p), $p);
        }
    }

    public void updateStatus(List<WechatAddFriendListEntity> addList) {
        if (CollectionUtils.isEmpty(addList)) return;
        List<Long> lists = Lists.newArrayList();
        for (WechatAddFriendListEntity $it : addList) lists.add($it.getId());
        Map<String, Object> params = Maps.newHashMap();
        params.put("addIds", lists);
        String sql = getExecSql("updateStatus4Pushing", params);
        getJdbc().update(sql, params);
    }

    public void updateStatus(OrganizationEntity company, StoreEntity store, MemberEntity member, String weixinId, String phone, PushStatus status) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(member);
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", company.getId());
        params.put("memberId", member.getId());
        params.put("pushFlag", status.getValue());
        String sql = null;
        switch (status) {
            case PUSH_TO_DEVICE:
                sql = getExecSql("updatePushDeviceStatus", params);
                break;
            case SEND_ADD_MEMBER:
                sql = getExecSql("updateAddMemebrStatus", params);
                break;
            case ADD_MEMBER_FAIL:
                sql = getExecSql("updateStatus", params);
                break;
            case ADD_MEMBER_SUCCESS:
                sql = getExecSql("updateBindSuccess", params);
                params.put("weixinId", weixinId);
                break;
            default:
                break;
        }
        getNamedParameterJdbcTemplate().update(sql, params);
    }

    /**
     * 判断是否需要推送
     *
     * @param store
     * @return
     */
    public boolean pushable(StoreEntity store) {
        Preconditions.checkState(store.getCompanyId().isPresent());
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().get());
        String sql = getExecSql("queryPushMemberCount", null);
        Map<String, Object> result = getJdbc().queryForMap(sql, params);
        Integer count = MapUtils.getInteger(result, "count");
        return count != 0;
    }

    /**
     * 创建推送名单
     *
     * @param store
     * @param config
     */
    public void createPushListNew(StoreEntity store, WechatAddFriendConfigEntity config) {
        Preconditions.checkState(store.getCompanyId().isPresent());
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().get());
        params.put("runTimes", config.getRunTimes());
        params.put("limit", config.getSendNum());
        if (!pushable(store)) return;
        List<Map<String, Object>> maps = getJdbc().queryForList(getExecSql("createPushListNew", params), params);
        if (CollectionUtils.isEmpty(maps)) {
            params.put("runTimes", config.getRunTimes() + 1);
            maps = getJdbc().queryForList(getExecSql("createPushListNew", params), params);
        }
        if (CollectionUtils.isEmpty(maps)) return;
        getJdbc().update(getExecSql("updateConfigRunTimes", null), params);
        final int run_timers = MapUtils.getIntValue(params, "runTimes");

        getJdbcTemplate().batchUpdate(getExecSql("batchInsert", null), maps, 200,
                new ParameterizedPreparedStatementSetter<Map<String, Object>>() {
                    @Override
                    public void setValues(PreparedStatement ps, Map<String, Object> map) throws SQLException {
                        ps.setObject(1, MapUtils.getIntValue(map, "memberId"));
                        ps.setObject(2, MapUtils.getIntValue(map, "storeId"));
                        ps.setObject(3, MapUtils.getIntValue(map, "companyId"));
                        ps.setObject(4, 2);
                        ps.setObject(5, run_timers);
                        ps.setObject(6, -1);
                    }
                });
    }

    public Optional<List<WechatAddFriendListEntity>> loadByOneForBatch() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("my_date", DateTime.now().toString("yyyy-MM-dd"));
        List<WechatAddFriendListEntity> list = getJdbc().query(getExecSql("loadByGroup4One", null), params,
                new WechatAddFriendListAction.RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

    public Optional<List<WechatAddFriendListEntity>> loadByStatus(PushStatus status) {
        Objects.requireNonNull(status);
        Map<String, Object> params = Maps.newHashMap();
        params.put("pushFlag", status.getValue());
        String sql = getExecSql("loadByStatus", params);
        List<WechatAddFriendListEntity> list = getNamedParameterJdbcTemplate().query(sql, params, new WechatAddFriendListAction.RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

    public void updateStatus(Collection<WechatAddFriendListEntity> members, PushStatus status) {
        if (CollectionUtils.isEmpty(members)) return;
        if (status == null) return;
        List<Long> ids = Lists.newArrayList();
        for (WechatAddFriendListEntity member : members) ids.add(member.getId());
        Map<String, Object> params = Maps.newHashMap();
        params.put("ids", ids);
        params.put("pushFlag", status.getValue());
        String sql = getExecSql("updateAnyStatus", params);
        getNamedParameterJdbcTemplate().update(sql, params);
    }

    public Optional<List<WechatAddFriendPushListEntity>> createPushList(Date date) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("pushDate", date);
        List<Map<String, Object>> list = getJdbc().queryForList(getExecSql("createPushList", params), params);
        if (CollectionUtils.isEmpty(list)) return Optional.absent();
        List<WechatAddFriendPushListEntity> pushList = Lists.newArrayListWithCapacity(list.size());
        for (Map<String, Object> data : list) {
            // WechatAddFriendPushListEntity(Long id, Integer userSize, Integer storeId, Integer companyId)
            int exit_size = MapUtils.getIntValue(data, "exitSize", 0);
            int all_size = MapUtils.getIntValue(data, "allSize", 0);
            int size = ((20 - exit_size) <= all_size) ? (20 - exit_size) : all_size;
            String[] memberIds = StringUtils.split(MapUtils.getString(data, "memberIds"), ',');
            String[] ids = size == all_size ? memberIds : ArrayUtils.subarray(memberIds, 0, size);
            WechatAddFriendPushListEntity instance = new WechatAddFriendPushListEntity(MapUtils.getInteger(data, "storeId"),
                    MapUtils.getInteger(data, "companyId"), ids);
            pushList.add(instance);
        }
        return Optional.of(pushList);
    }

    @Override
    protected ResultSetExtractor<WechatAddFriendListEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class RowMapperImpl implements RowMapper<WechatAddFriendListEntity> {
        @Override
        public WechatAddFriendListEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<WechatAddFriendListEntity> {

        @Override
        public WechatAddFriendListEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return buildByResultSet(resultSet);
            }
            return null;
        }
    }

    private WechatAddFriendListEntity buildByResultSet(ResultSet resultSet) throws SQLException {
        // Long id, Integer memberId, Integer storeId, Integer companyId, String memberName,
        // String phoneNo, int pushFlag, Date joinDate, Date pushDate, String weixinId
        return new WechatAddFriendListEntity(resultSet.getLong("id"),
                resultSet.getInt("memberId"), resultSet.getInt("storeId"), resultSet.getInt("companyId"),
                resultSet.getString("memberName"), resultSet.getString("phoneNo"),
                resultSet.getInt("pushFlag"), resultSet.getDate("joinDate"),
                resultSet.getDate("pushDate"),
                resultSet.getString("weixinId"),
                resultSet.getInt("pushType"),
                resultSet.getInt("runTimes"));
    }

}
