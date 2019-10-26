package com.csosm.module.webchat.entity;

import com.csosm.commons.adapter.LoginUserContext;

import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.member.entity.MemberEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class WebChatUserAction extends BaseEntityAction<WebChatUserEntity> {

    private static final Logger logger = LoggerFactory.getLogger(WebChatUserAction.class);

    public WebChatUserAction() {
        super("webchat", null);
    }

    @Deprecated
    @Override
    public Optional<WebChatUserEntity> findById(Object id) {
        throw new UnsupportedOperationException("不支持该方法调用,选用findById(StoreEntity store, Object id) 执行");
    }

    public void buildBySingle(String wexinId, MemberEntity member, StoreEntity store) {
        Preconditions.checkNotNull(member, "待绑定会员不存在...");
        Preconditions.checkNotNull(store);
        Preconditions.checkState(store.hasDevice());
        Preconditions.checkState(store.getCompanyId().isPresent());
        Optional<WebChatUserEntity> weixin = findById(store, wexinId);
        if (!weixin.isPresent()) return;
        weixin = weixin.get().bildMember(member);
        if (weixin.isPresent()) {
            List<WebChatUserEntity> weixin_list = Lists.newArrayList(weixin.get());
            batchInsert(weixin_list, null);
        }
    }

    public Optional<WebChatUserEntity> findByMember(StoreEntity store, MemberEntity member) {
        Preconditions.checkNotNull(member, "findByWechatUser(MemberEntity member) 入参不可以为空..");
        Preconditions.checkArgument(member.getStoreId().isPresent(), "会员无门店属性....");
        Optional<List<WebChatUserEntity>> all_list = loadAllByStore(store, null);
        if (!all_list.isPresent()) return Optional.absent();
        WebChatUserEntity res = null;
        for (WebChatUserEntity $is : all_list.get()) {
            if ($is.hasMember() && $is.isbildMember(member)) {
                res = $is;
                break;
            }
        }
        return Optional.fromNullable(res);
    }

    public void batchUpdateSyncWechat(List<Map<String, Object>> datas) {
        if (CollectionUtils.isEmpty(datas)) return;
        getJdbcTemplate().batchUpdate(getExecSql("batchUpdateSyncWechat", null), datas, 2000,
                new ParameterizedPreparedStatementSetter<Map<String, Object>>() {
                    @Override
                    public void setValues(PreparedStatement ps, Map<String, Object> data) throws SQLException {
                        ps.setObject(1, MapUtils.getObject(data, "id"));
                        ps.setObject(2, MapUtils.getObject(data, "username"));
                        ps.setObject(3, MapUtils.getObject(data, "syncRes"));
                        ps.setObject(4, MapUtils.getObject(data, "storeId"));
                        ps.setObject(5, MapUtils.getObject(data, "companyId"));
                    }
                });
        if (logger.isDebugEnabled()) {
            logger.debug("batchUpdateSyncWechat() update datas size is %s for Sync....", datas.size());
        }
    }

    public Optional<WebChatUserEntity> findById(StoreEntity store, Object weixinId) {
        Preconditions.checkNotNull(store, "请指定需要加载通讯录的门店.");
        Optional<List<WebChatUserEntity>> all_list = loadAllByStore(store, null);
        if (!all_list.isPresent()) return Optional.absent();
        WebChatUserEntity res = null;
        for (WebChatUserEntity webChatUser : all_list.get()) {
            if (Objects.equals(weixinId, webChatUser.getUserName())) {
                res = webChatUser;
                break;
            }
        }
        return Optional.fromNullable(res);
    }

    public Optional<WebChatUserEntity> findOrginById(StoreEntity store, Object weixinId) {
        Preconditions.checkNotNull(store, "请指定需要加载通讯录的门店.");
        Preconditions.checkState(store.getCompanyId().isPresent());
        Map<String, Object> params = Maps.newHashMap();
        Preconditions.checkArgument(store.getCompanyId().isPresent(), "当前门店 %s 无公司信息.", store.getName());
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().get());
        params.put("wenxinId", weixinId);
        Preconditions.checkState(store.hasDevice(), "当前门店无绑定设备...");
        params.put("tablename", store.getContactTableName());
        WebChatUserEntity webChatUser = getNamedParameterJdbcTemplate()
                .query(getExecSql("findOrginById", params), params, getResultSetExtractor());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findById(%s,%s) return -> webChatUser is %s", store.getName(),
                    weixinId, webChatUser));
        return Optional.fromNullable(webChatUser);
    }

    // 获取指定门店的微信 未绑定会员的微信列表
    public Optional<List<WebChatUserEntity>> loadAllByStore(StoreEntity store, Map<String, Object> searchs, boolean isbildmember) {
        Optional<List<WebChatUserEntity>> all_list = loadAllByStore(store, searchs);
        if (!all_list.isPresent()) return Optional.absent();
        List<WebChatUserEntity> list = Lists.newArrayList();
        for (WebChatUserEntity $user : all_list.get()) {
            if (isbildmember) {
                if ($user.hasMember()) list.add($user);
            } else {
                if (!$user.hasMember()) list.add($user);
            }
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByStore(%s,%s) return -> weixin size is %s", store.getName(),
                    isbildmember ? "绑定" : "未绑定",
                    CollectionUtils.isEmpty(list) ? 0 : list.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

    // 获取指定门店的微信好友清单
    public Optional<List<WebChatUserEntity>> loadAllByStore(StoreEntity store, Map<String, Object> searchs) {
        Preconditions.checkNotNull(store, "请指定需要加载通讯录的门店.");
        if (!store.hasDevice()) return Optional.absent();
        Map<String, Object> params = Maps.newHashMap();
        Preconditions.checkArgument(store.getCompanyId().isPresent(), "当前门店 %s 无公司信息.", store.getName());
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().get());
        params.put("tablename", store.getContactTableName());
        if (MapUtils.isNotEmpty(searchs)) {
            for (Map.Entry<String, Object> $it : searchs.entrySet()) {
                params.put($it.getKey(), String.format("%%%s%%", $it.getValue()));
            }
        }
        List<WebChatUserEntity> webChatUsers = getNamedParameterJdbcTemplate()
                .query(getExecSql("load_weixins_store", params), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByStore(%s) return -> webChatUsers size is %s", store.getName(),
                    CollectionUtils.isEmpty(webChatUsers) ? 0 : webChatUsers.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(webChatUsers) ? null : webChatUsers);
    }

    // 获取指定门店的微信好友清单  指定微信ID列表
    public Optional<List<WebChatUserEntity>> loadAllByIds(StoreEntity store, Collection<String> weixinIds) {
        Preconditions.checkNotNull(store, "请指定需要加载通讯录的门店.");
        Optional<List<WebChatUserEntity>> webChatUsers = loadAllByStore(store, null);
        if (!webChatUsers.isPresent()) return Optional.absent();
        if (CollectionUtils.isEmpty(weixinIds)) return webChatUsers;
        List<WebChatUserEntity> res = Lists.newArrayList();
        for (WebChatUserEntity $usr : webChatUsers.get()) {
            if (weixinIds.contains($usr.getUserName())) res.add($usr);
        }
        return Optional.fromNullable(CollectionUtils.isEmpty(res) ? null : res);
    }

    public Optional<List<WebChatUserEntity>> loadByOneStore(StoreEntity store, Collection<String> groupIds, Collection<String> exclouds) {
        Preconditions.checkNotNull(store);
        Preconditions.checkState(store.getCompanyId().isPresent());
        Map<String, Object> params = Maps.newHashMap();
        params.put("tablename", store.getContactTableName());
        if (!CollectionUtils.isEmpty(groupIds)) groupIds.remove("0000");
        if (!CollectionUtils.isEmpty(groupIds)) params.put("groupIds", groupIds);
        if (!CollectionUtils.isEmpty(exclouds)) params.put("excludes", exclouds);
        params.put("companyId", store.getCompanyId().get());
        params.put("storeId", store.getId());
        List<WebChatUserEntity> webChatUsers = getNamedParameterJdbcTemplate()
                .query(getExecSql("load_weixins_store", params), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(webChatUsers) ? null : webChatUsers);
    }

    // 解除绑定
    public void unBildMember(Collection<String> weixinIds, Collection<Integer> memberIds, StoreEntity store, LoginUserContext user) {
        if (CollectionUtils.isEmpty(weixinIds)) return;
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(store);
        Preconditions.checkState(store.getCompanyId().isPresent());
        Map<String, Object> clear_params = Maps.newHashMap();
        clear_params.put("storeId", store.getId());
        clear_params.put("companyId", store.getCompanyId().get());
        clear_params.put("weixinIds", weixinIds);
        clear_params.put("memberIds", memberIds);
        getNamedParameterJdbcTemplate().update(getExecSql("clear_mapping_byweixinids", clear_params), clear_params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("本次门店%s 解除微信 %s 绑定会员, 操作人 %s", store.getName(),
                    weixinIds, user.getUsername()));
    }

    // 批量绑定
    public void batchBildMember(List<WebChatUserEntity> mapping, StoreEntity store, final LoginUserContext user) {
        if (CollectionUtils.isEmpty(mapping)) return;
        Preconditions.checkNotNull(user);
        Preconditions.checkState(user.getCompany().isPresent());
        List<String> weixinIds = Lists.newArrayListWithCapacity(mapping.size());
        List<Integer> memberIds = Lists.newArrayListWithCapacity(mapping.size());
        for (WebChatUserEntity $it : mapping) weixinIds.add($it.getUserName());
        for (WebChatUserEntity $it : mapping) memberIds.add($it.getBildMemberId());

        unBildMember(weixinIds, memberIds, store, user);

        batchInsert(mapping, user);
        if (logger.isDebugEnabled())
            logger.debug(String.format("本次门店%s 微信批量绑定会员共计 %s 人, 操作人 %s", store.getName(),
                    mapping.size(), user.getUsername()));
    }

    private void batchInsert(List<WebChatUserEntity> mapping, final LoginUserContext user) {
        getJdbcTemplate().batchUpdate(getExecSql("batch_bild_member", null), mapping, 100,
                new ParameterizedPreparedStatementSetter<WebChatUserEntity>() {
                    //id, weixin_id, member_id, store_id, company_id, createUserId
                    @Override
                    public void setValues(PreparedStatement ps, WebChatUserEntity entity) throws SQLException {
                        //id,batch_id,serial_num,life_status,createUserId
                        ps.setString(1, UUID.randomUUID().toString());
                        ps.setString(2, entity.getUserName());
                        ps.setInt(3, entity.getBildMemberId());
                        ps.setInt(4, entity.getStoreId());
                        ps.setInt(5, entity.getCompanyId());
                        if (user != null) {
                            ps.setInt(6, user.getEmployee().getId());
                        } else {
                            ps.setInt(6, -1);
                        }
                    }
                });
    }


    @Override
    protected ResultSetExtractor<WebChatUserEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class RowMapperImpl implements RowMapper<WebChatUserEntity> {
        @Override
        public WebChatUserEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<WebChatUserEntity> {

        @Override
        public WebChatUserEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return buildByResultSet(resultSet);
            }
            return null;
        }
    }

    private WebChatUserEntity buildByResultSet(ResultSet resultSet) throws SQLException {
        Integer memberId = resultSet.getObject("memberId") == null ? null : resultSet.getInt("memberId");
        return new WebChatUserEntity(
                resultSet.getString("ownerUserName"),
                resultSet.getString("userName"),
                resultSet.getString("nickName"),
                resultSet.getInt("wxType"),
                resultSet.getString("iconUrl"),
                resultSet.getString("conRemark"),
                resultSet.getInt("storeId"),
                resultSet.getInt("companyId"),
                memberId);
    }

}
