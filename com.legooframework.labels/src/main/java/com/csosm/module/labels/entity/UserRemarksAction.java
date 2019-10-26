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
import org.apache.commons.lang3.StringUtils;
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

public class UserRemarksAction extends BaseEntityAction<UserRemarksEntity> {

    private static final Logger logger = LoggerFactory.getLogger(UserRemarksAction.class);

    public UserRemarksAction() {
        super("UserRemarksEntity", null);
    }

    private UserRemarksEntity checkAndCreateRemark(LoginUserContext userContext, MemberEntity member, WebChatUserEntity wechatUser,
                                                   String remarks) {
        if (wechatUser != null && member != null) {
            if (wechatUser.hasMember())
                Preconditions.checkArgument(wechatUser.isbildMember(member),
                        "当前微信%s 绑定的会员 与 传入会员%s 不一致...", wechatUser.getUserName(), member.getId());
        }

        UserRemarksEntity entity = new UserRemarksEntity(userContext, member, wechatUser, remarks);
        Optional<List<UserRemarksEntity>> entities = findByEntity(entity);
        if (entities.isPresent()) {
            for (UserRemarksEntity $it : entities.get()) {
                if (StringUtils.equals(remarks, $it.getRemarks())) return $it;
            }
            entity = entities.get().get(0).createByClone(userContext, remarks);
        }
        return entity;
    }

    /**
     * 增加备注
     *
     * @param userContext
     * @param member
     * @param wechatUser
     * @param remarks
     * @return
     */
    public UserRemarksEntity addRemarks(LoginUserContext userContext, MemberEntity member, WebChatUserEntity wechatUser,
                                        String remarks) {
        UserRemarksEntity entity = checkAndCreateRemark(userContext, member, wechatUser, remarks);
        int res = getNamedParameterJdbcTemplate().update(getExecSql("insert", null), entity.toMap());
        Preconditions.checkState(1 == res, "持久化 %s 发生异常...", entity);
        Optional<UserRemarksEntity> exits = findOneByEntity(entity);
        Preconditions.checkState(exits.isPresent(), "%s 数据获取异常...", entity);
        return exits.get();
    }

    /**
     * 增加用于关联绑定的备注
     *
     * @param userContext
     * @param wechatUser
     * @param remarks
     * @return
     */
    public UserRemarksEntity addBindRemarks(LoginUserContext userContext, WebChatUserEntity wechatUser,
                                            String remarks) {
        Objects.requireNonNull(wechatUser);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(remarks), "备注不能为空");
        UserRemarksEntity entity = new UserRemarksEntity(userContext, null, wechatUser, remarks);
        int res = getNamedParameterJdbcTemplate().update(getExecSql("insert_bind", null), entity.toMap());
        Preconditions.checkState(1 == res, "持久化 %s 发生异常...", entity);
        Optional<UserRemarksEntity> exits = findOneByEntity(entity);
        Preconditions.checkState(exits.isPresent(), "%s 数据获取异常...", entity);
        return exits.get();
    }

    public Optional<List<UserRemarksEntity>> findRemarksByUser(StoreEntity store, MemberEntity member,
                                                               WebChatUserEntity wechatUser) {
        Preconditions.checkNotNull(store, "StoreEntity store 不可以为空...");
        Preconditions.checkArgument(store.getCompanyId().isPresent(), "门店 %s 无公司属性", store.getName());

        Preconditions.checkArgument(member != null || wechatUser != null, "会员 或者 微信需指定其中之一...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().get());
        params.put("memberId", null);
        params.put("weixinId", null);
        if (null != member) {
            params.put("memberId", member.getId());
        } else {
            params.put("weixinId", wechatUser.getUserName());
        }
        List<UserRemarksEntity> entities = getJdbc().query(getExecSql("findByEntity", params), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(entities) ? null : entities);
    }

    public void removeRemarks(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) return;
        Map<String, Object> params = Maps.newHashMap();
        params.put("ids", ids);
        getJdbc().update(getExecSql("deleteByIds", params), params);
    }

    private Optional<List<UserRemarksEntity>> findByEntity(UserRemarksEntity entity) {
        Map<String, Object> params = entity.toMap();
        List<UserRemarksEntity> entities = getJdbc().query(getExecSql("findByEntity", params), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(entities) ? null : entities);
    }

    public Optional<UserRemarksEntity> findOneByEntity(UserRemarksEntity entity) {
        Map<String, Object> params = entity.toMap();
        UserRemarksEntity entities = getJdbc().query(getExecSql("findOneByEntity", params), params, getResultSetExtractor());
        return Optional.fromNullable(entities);
    }

    public void mergeWxWithMm(StoreEntity store, List<WebChatUserEntity> weixinUsers) {
        Preconditions.checkNotNull(store, "入参 StoreEntity store 不可以为空值...");
        if (CollectionUtils.isEmpty(weixinUsers)) return;
        Set<Integer> memberIds = Sets.newHashSet();
        Set<String> weixinIds = Sets.newHashSet();
        for (WebChatUserEntity $it : weixinUsers) {
            memberIds.add($it.getBildMemberId());
            weixinIds.add($it.getUserName());
        }

        Optional<List<UserRemarksEntity>> exits = findByStore(store, memberIds, weixinIds);
        if (!exits.isPresent()) return;
        Multimap<WebChatUserEntity, UserRemarksEntity> multimap = ArrayListMultimap.create();
        for (WebChatUserEntity $weixin : weixinUsers) {
            for (UserRemarksEntity $l : exits.get()) {
                if ($weixin.getBildMemberId().equals($l.getMemberId().or(-1))) multimap.put($weixin, $l);
            }
            for (UserRemarksEntity $l : exits.get()) {
                if ($weixin.getUserName().equals($l.getWeixinId().or("-1"))) multimap.put($weixin, $l);
            }
        }
        if (multimap.size() == 0) return;

        List<UserRemarksEntity> save_list = Lists.newArrayList();
        for (WebChatUserEntity $it : multimap.keySet()) {
            Collection<UserRemarksEntity> remark_ids = multimap.get($it);
            if (CollectionUtils.isEmpty(remark_ids)) continue;
            for (UserRemarksEntity marks : remark_ids)
                save_list.add(
                        new UserRemarksEntity((Integer) marks.getCreateUserId(), marks.getRemarks(), $it));
        }
        if (CollectionUtils.isEmpty(save_list)) return;
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("memberIds", memberIds);
        params.put("weixinIds", weixinIds);
        getJdbc().update(getExecSql("clearByWeixinOrMember", params), params);
        batchUpdate(save_list);
    }

    public Optional<List<UserRemarksEntity>> findByWechatUser(WebChatUserEntity webChatUser) {
        Preconditions.checkNotNull(webChatUser, "入参WebChatUserEntity webChatUser 不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", webChatUser.getStoreId());
        params.put("memberIds", null);
        params.put("weixinIds", new String[]{webChatUser.getUserName()});
        List<UserRemarksEntity> exits = getJdbc().query(getExecSql("findByWeixinOrMember", params), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(exits) ? null : exits);
    }

    public Optional<List<UserRemarksEntity>> findByWechatUser(MemberEntity member) {
        Preconditions.checkNotNull(member, "入参MemberEntity member 不可以为空值...");
        Preconditions.checkArgument(member.getStoreId().isPresent(), "会员 %s 无门店信息..", member.getName());
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", member.getStoreId().get());
        params.put("memberIds", new Integer[]{member.getId()});
        params.put("weixinIds", null);
        List<UserRemarksEntity> exits = getJdbc().query(getExecSql("findByWeixinOrMember", params), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(exits) ? null : exits);
    }

    public Optional<List<UserRemarksEntity>> findByStore(StoreEntity store, Set<Integer> memberIds, Set<String> weixinIds) {
        Preconditions.checkNotNull(store, "入参 StoreEntity store 不可以为空值...");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(memberIds) || CollectionUtils.isNotEmpty(weixinIds),
                "入参 Set<Integer> memberIds, Set<String> weixinIds 不允许同时为empty...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("memberIds", memberIds);
        params.put("weixinIds", weixinIds);
        List<UserRemarksEntity> exits = getJdbc().query(getExecSql("findByWeixinOrMember", params), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(exits) ? null : exits);
    }

    public void unMergeWxWithMm(StoreEntity store, WebChatUserEntity webChatUser, LoginUserContext user) {
        Preconditions.checkNotNull(store, "入参 StoreEntity store 不可以为空值...");
        Optional<List<UserRemarksEntity>> userRemarks = findByWeixinAndMember(store, webChatUser.getBildMemberId(),
                webChatUser.getUserName());
        if (!userRemarks.isPresent()) return;
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("memberIds", new Integer[]{webChatUser.getBildMemberId()});
        params.put("weixinIds", new String[]{webChatUser.getUserName()});
        getNamedParameterJdbcTemplate().update(getExecSql("clearByWeixinOrMember", params), params);
        List<UserRemarksEntity> instance = Lists.newArrayList();
        for (UserRemarksEntity $it : userRemarks.get()) instance.addAll($it.spliter(user));
        batchUpdate(instance);
    }

    private void batchUpdate(Collection<UserRemarksEntity> instances) {
        if (CollectionUtils.isEmpty(instances)) return;
        getJdbcTemplate().batchUpdate(getExecSql("batchInsert", null), instances, 500,
                new ParameterizedPreparedStatementSetter<UserRemarksEntity>() {
                    //                INSERT INTO acp.user_label_remark
//                        (member_id, weixin_id, lable_id, enabled, store_id, company_id, createUserId, createTime)
//                VALUES (?, ?, ?, 1, ?, ?, ?, NOW())
                    @Override
                    public void setValues(PreparedStatement ps, UserRemarksEntity entity) throws SQLException {
                        if (entity.getMemberId().isPresent()) {
                            ps.setObject(1, entity.getMemberId().get());
                        } else {
                            ps.setObject(1, null);
                        }
                        if (entity.getWeixinId().isPresent()) {
                            ps.setObject(2, entity.getWeixinId().get());
                        } else {
                            ps.setObject(2, null);
                        }
                        ps.setString(3, entity.getRemarks());
                        ps.setInt(4, entity.getStoreId());
                        ps.setInt(5, entity.getCompanyId());
                        ps.setObject(6, entity.getCreateUserId());
                    }
                });
    }

    private Optional<List<UserRemarksEntity>> findByWeixinAndMember(StoreEntity store, Integer memberId, String weixinId) {
        Preconditions.checkNotNull(store);
        Preconditions.checkNotNull(memberId);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId));
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("memberId", memberId);
        params.put("weixinId", weixinId);
        List<UserRemarksEntity> entity = getJdbc().query(getExecSql("findByWeixinAndMember", null), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(entity) ? null : entity);
    }

    @Override
    protected ResultSetExtractor<UserRemarksEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<UserRemarksEntity> {

        @Override
        public UserRemarksEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return new UserRemarksEntity(resultSet);
            }
            return null;
        }
    }

    class RowMapperImpl implements RowMapper<UserRemarksEntity> {
        @Override
        public UserRemarksEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            try {
                return new UserRemarksEntity(resultSet);
            } catch (Exception e) {
                logger.error("还原 UserRemarksEntity from db has error...", e);
                throw new RuntimeException(e);
            }

        }
    }
}
