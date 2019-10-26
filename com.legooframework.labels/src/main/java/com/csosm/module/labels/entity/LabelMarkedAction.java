package com.csosm.module.labels.entity;

import com.csosm.commons.adapter.LoginUserContext;

import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.webchat.entity.WebChatUserEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LabelMarkedAction extends BaseEntityAction<LabelMarkedEntity> {

    private static final Logger logger = LoggerFactory.getLogger(LabelMarkedAction.class);

    public LabelMarkedAction() {
        super("LabelMarkedEntity", null);
    }

    public void loadLables(MemberEntity member, WebChatUserEntity webChatUser) {
        Map<String, Object> params = Maps.newHashMap();
        if (webChatUser != null) {
            params.put("weixiId", webChatUser.getUserName());
            params.put("hasMember", webChatUser.hasMember());
            params.put("memberId", webChatUser.getBildMemberId());
            params.put("storeId", webChatUser.getStoreId());
        } else {
            params.put("weixiId", null);
            params.put("memberId", member.getId());
            params.put("storeId", member.getStoreId());
        }
    }

    /**
     * @param labelNode
     * @param members
     * @param weixins
     * @param store
     * @param loginUser
     */
    public void markLables(LabelNodeEntity labelNode, List<MemberEntity> members,
                           List<WebChatUserEntity> weixins, StoreEntity store, LoginUserContext loginUser) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("labelId", labelNode.getId());
        params.put("storeId", store.getId());
        Set<Integer> m_ids = Sets.newHashSet();
        Set<String> w_ids = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(members)) {
            for (MemberEntity $mm : members) m_ids.add($mm.getId());
            params.put("memberIds", m_ids);
        }

        if (CollectionUtils.isNotEmpty(weixins)) {
            for (WebChatUserEntity $mm : weixins) w_ids.add($mm.getUserName());
            params.put("weixinIds", w_ids);
        }

        List<LabelMarkedEntity> exits = getJdbc().query(getExecSql("findByIds", params), params, new RowMapperImpl());
        // 去掉重复的数据
        if (CollectionUtils.isNotEmpty(exits)) {
            for (LabelMarkedEntity $it : exits) {
                if ($it.hasWeixin() && CollectionUtils.isNotEmpty(w_ids)) {
                    w_ids.remove($it.getWeixinId());
                }
                if ($it.hasMember() && CollectionUtils.isNotEmpty(m_ids)) {
                    m_ids.remove($it.getMemberId());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(weixins)) {
            for (WebChatUserEntity $it : weixins) {
                if ($it.hasMember()) {
                    m_ids.remove($it.getBildMemberId());
                }
            }
        }

        Set<LabelMarkedEntity> instances = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(w_ids)) {
            for (WebChatUserEntity $wx : weixins) {
                if (w_ids.contains($wx.getUserName())) {
                    instances.add(new LabelMarkedEntity(labelNode, null, $wx, loginUser.getUserId()));
                }
            }
        }

        if (CollectionUtils.isNotEmpty(m_ids)) {
            for (MemberEntity $wx : members) {
                if (m_ids.contains($wx.getId())) {
                    instances.add(new LabelMarkedEntity(labelNode, $wx, null, loginUser.getUserId()));
                }
            }
        }

        if (CollectionUtils.isNotEmpty(instances)) {
            batchUpdate(instances);
        }
    }

    public void removeLabel(LabelNodeEntity labelNode, MemberEntity member, WebChatUserEntity weixin, StoreEntity store) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("labelId", labelNode.getId());
        params.put("storeId", store.getId());
        Preconditions.checkState(member != null || weixin != null, "微信 会员需指定其中之一....");
        params.put("memberId", null);
        params.put("weixinId", null);
        if (member != null) {
            params.put("memberId", member.getId());
        }
        if (weixin != null) {
            params.put("weixinId", weixin.getUserName());
        }
        getJdbc().update(getExecSql("removeLabel", params), params);
    }

    public Optional<List<LabelMarkedEntity>> findByStore(StoreEntity store, Set<Integer> memberIds, Set<String> weixinIds) {
        Preconditions.checkNotNull(store, "入参 StoreEntity store 不可以为空值...");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(memberIds) || CollectionUtils.isNotEmpty(weixinIds),
                "入参 Set<Integer> memberIds, Set<String> weixinIds 不允许同时为empty...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("memberIds", memberIds);
        params.put("weixinIds", weixinIds);
        List<LabelMarkedEntity> exits = getJdbc().query(getExecSql("findByWeixinOrMember", params), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(exits) ? null : exits);
    }

    /**
     * 分开
     *
     * @param store 门店
     */
    public void unMergeWxWithMm(StoreEntity store, WebChatUserEntity webChatUser, LoginUserContext user) {
        Preconditions.checkNotNull(store, "入参 StoreEntity store 不可以为空值...");
        Optional<List<LabelMarkedEntity>> markedEntity = findByWeixinAndMember(store, webChatUser.getBildMemberId(),
                webChatUser.getUserName());
        if (!markedEntity.isPresent()) return;
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("memberIds", new Integer[]{webChatUser.getBildMemberId()});
        params.put("weixinIds", new String[]{webChatUser.getUserName()});
        getJdbc().update(getExecSql("clearByWeixinOrMember", params), params);
        List<LabelMarkedEntity> instance = Lists.newArrayList();
        for (LabelMarkedEntity $it : markedEntity.get()) instance.addAll($it.spliter(user));
        batchUpdate(instance);
    }

    /**
     * @param store
     * @param weixinUsers
     */
    public void mergeWxWithMm(StoreEntity store, List<WebChatUserEntity> weixinUsers) {
        Preconditions.checkNotNull(store, "入参 StoreEntity store 不可以为空值...");
        if (CollectionUtils.isEmpty(weixinUsers)) return;
        Map<Integer, String> maps = Maps.newHashMap();
        Set<Integer> memberIds = Sets.newHashSet();
        Set<String> weixinIds = Sets.newHashSet();
        for (WebChatUserEntity $it : weixinUsers) {
            memberIds.add($it.getBildMemberId());
            weixinIds.add($it.getUserName());
            maps.put($it.getBildMemberId(), $it.getUserName());
        }
        Optional<List<LabelMarkedEntity>> exits = findByStore(store, memberIds, weixinIds);
        if (!exits.isPresent()) return;
        Multimap<WebChatUserEntity, Long> multimap = ArrayListMultimap.create();
        for (WebChatUserEntity $weixin : weixinUsers) {
            for (LabelMarkedEntity $l : exits.get()) {
                if ($weixin.getBildMemberId().equals($l.getMemberId())) multimap.put($weixin, $l.getLabelId());
            }
            for (LabelMarkedEntity $l : exits.get()) {
                if ($weixin.getUserName().equals($l.getWeixinId())) multimap.put($weixin, $l.getLabelId());
            }
        }
        if (multimap.size() == 0) return;

        List<LabelMarkedEntity> save_list = Lists.newArrayList();
        for (WebChatUserEntity $it : multimap.keySet()) {
            Collection<Long> label_ids = multimap.get($it);
            if (CollectionUtils.isEmpty(label_ids)) continue;
            for (Long id : label_ids) save_list.add(new LabelMarkedEntity(id, $it));
        }
        if (CollectionUtils.isEmpty(save_list)) return;
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("memberIds", memberIds);
        params.put("weixinIds", weixinIds);
        getJdbc().update(getExecSql("clearByWeixinOrMember", params), params);
        batchUpdate(save_list);
    }

    private Optional<List<LabelMarkedEntity>> findByWeixinAndMember(StoreEntity store, Integer memberId, String weixinId) {
        Preconditions.checkNotNull(store);
        Preconditions.checkNotNull(memberId);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId));
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("memberId", memberId);
        params.put("weixinId", weixinId);
        List<LabelMarkedEntity> entity = getJdbc().query(getExecSql("findByWeixinAndMember", null), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(entity) ? null : entity);
    }

    private void batchUpdate(Collection<LabelMarkedEntity> instances) {
        if (CollectionUtils.isEmpty(instances)) return;
        getJdbcTemplate().batchUpdate(getExecSql("batchInsert", null), instances, 500, new ParameterizedPreparedStatementSetter<LabelMarkedEntity>() {
            //                INSERT INTO acp.user_label_remark
//                        (member_id, weixin_id, lable_id, enabled, store_id, company_id, createUserId, createTime)
//                VALUES (?, ?, ?, 1, ?, ?, ?, NOW())
            @Override
            public void setValues(PreparedStatement ps, LabelMarkedEntity entity) throws SQLException {
                if (entity.getMemberId() == null) {
                    ps.setObject(1, null);
                } else {
                    ps.setInt(1, entity.getMemberId());
                }
                if (Strings.isNullOrEmpty(entity.getWeixinId())) {
                    ps.setString(2, null);
                } else {
                    ps.setString(2, entity.getWeixinId());
                }
                ps.setLong(3, entity.getLabelId());
                ps.setInt(4, entity.getStoreId());
                ps.setInt(5, entity.getCompanyId());
                ps.setObject(6, entity.getCreateUserId());
            }
        });
    }

    @Override
    protected ResultSetExtractor<LabelMarkedEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<LabelMarkedEntity> {

        @Override
        public LabelMarkedEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return new LabelMarkedEntity(resultSet);
            }
            return null;
        }
    }

    class RowMapperImpl implements RowMapper<LabelMarkedEntity> {
        @Override
        public LabelMarkedEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            try {
                return new LabelMarkedEntity(resultSet);
            } catch (Exception e) {
                logger.error("还原 LabelMarkedEntity from db has error...", e);
                throw new RuntimeException(e);
            }

        }
    }
}
