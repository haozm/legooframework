package com.csosm.module.webchat.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SendMsgEntityAction extends BaseEntityAction<SendMsgEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SendMsgEntityAction.class);


    public SendMsgEntityAction() {
        super("webchatlog", null);
    }

    /**
     * 重新启动发送失败的信息
     *
     * @param loginUser 当前登录账户
     */
    public void sendByFailsInToday(LoginUserContext loginUser) {
        Preconditions.checkNotNull(loginUser, "登陆账户信息不可以空值...");
        Preconditions.checkState(loginUser.getCompany().isPresent(), "登陆账户所在公司不可以为空..");
        Preconditions.checkState(loginUser.getStore().isPresent(), "登陆账户门店不可以为空...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", loginUser.getStore().get().getId());
        params.put("companyId", loginUser.getCompany().get().getId());
        int res = getNamedParameterJdbcTemplate().update(getExecSql("sendByFailsInToday", null), params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("Company=%s,Store=%s 发送失败微信重发....", loginUser.getCompany().get().getId(),
                    loginUser.getStore().get().getId()));
        // Preconditions.checkState(1 == res, "...");
    }

    public String insert(String msgTxt, Long tempId, String[] imageInfo, Set<WebChatUserEntity> weixins,
                         StoreEntity store, LoginUserContext userContext) {
        Preconditions.checkNotNull(userContext);
        Preconditions.checkNotNull(store);
        Preconditions.checkState(CollectionUtils.isNotEmpty(weixins), "发送的微信ID集合不可以为空.");
        Preconditions.checkArgument(store.getCompanyId().isPresent(), "门店%s无公司信息，无法执行该操作.", store.getName());
        Set<String> ids = Sets.newHashSet();
        for (WebChatUserEntity $it : weixins) ids.add($it.getUserName());
        SendMsgEntity entity = new SendMsgEntity(userContext, msgTxt, imageInfo, ids, store, tempId);
        int res = getNamedParameterJdbcTemplate().update(getExecSql("insert", null), entity.toMap());
        Preconditions.checkState(1 == res, "新增微信群发历史失败...");
        return entity.getId();
    }

    public String insert(String msgTxt, Long tempId, String[] imageInfo, String weixins, StoreEntity store, LoginUserContext userContext) {
        Set<String> ids = Sets.newHashSet();
        ids.add(weixins);
        SendMsgEntity entity = new SendMsgEntity(userContext, msgTxt, imageInfo, ids, store, tempId);
        int res = getNamedParameterJdbcTemplate().update(getExecSql("insert", null), entity.toMap());
        Preconditions.checkState(1 == res, "新增微信群发历史失败...");
        return entity.getId();
    }

    public String insert(String msgTxt, Long tempId, String[] imageInfo, Set<WebChatUserEntity> weixins, StoreEntity store) {
        Preconditions.checkNotNull(store);
        Preconditions.checkState(CollectionUtils.isNotEmpty(weixins), "发送的微信ID集合不可以为空.");
        Preconditions.checkArgument(store.getCompanyId().isPresent(), "门店%s无公司信息，无法执行该操作.", store.getName());
        Set<String> ids = Sets.newHashSet();
        for (WebChatUserEntity $it : weixins) ids.add($it.getUserName());
        SendMsgEntity entity = new SendMsgEntity(-99, msgTxt, imageInfo, ids, store, tempId);
        int res = getNamedParameterJdbcTemplate().update(getExecSql("insert", null), entity.toMap());
        Preconditions.checkState(1 == res, "新增微信群发历史失败...");
        return entity.getId();
    }

    public void sendFrieds(String msgTxt, LoginUserContext userContext) {
        Preconditions.checkNotNull(userContext);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(msgTxt), "朋友圈内容不可以为空.");
        if (logger.isDebugEnabled())
            logger.debug(String.format("发送朋友圈 %s", msgTxt));
        // TODO
    }

    public void batchToSend(SendMsgEntity entity, DevicesEntity device, LoginUserContext userContext) {
        Preconditions.checkNotNull(entity);
        Objects.requireNonNull(device);
        List<SendMsgDetailEntity> details = Lists.newArrayList();
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchToSend( entity=%s,  device = %s ...)", entity, device));
        if (entity.isImgOnly() || entity.isTxtOnly() || entity.isMixOnly())
            details.addAll(sendTxtOrImage(entity, device, userContext));
        if (entity.isSoftTxt())
            details.addAll(sendSoftTxt(entity, device, userContext));
        if (details.isEmpty()) return;
        batchToSend(details);
    }

    private List<SendMsgDetailEntity> sendTxtOrImage(SendMsgEntity entity, DevicesEntity device, LoginUserContext userContext) {
        List<SendMsgDetailEntity> details = Lists.newArrayList();
        for (String $id : entity.getWeixinIds()) {
            String groupId = UUID.randomUUID().toString();
            int order = 0;
            if (entity.getImageUrl().isPresent()) {
                for (String $it : entity.getImageUrl().get()) {
                    SendMsgDetailEntity img_detail = new SendMsgDetailEntity($it, $id, entity.getId(),
                            device.getId(), 3, device.getStoreId(), device.getCompanyId(), userContext, groupId, order);
                    order += 1;
                    details.add(img_detail);
                }
            }
            if (entity.getMsgTxt().isPresent()) {
                SendMsgDetailEntity txt_detail = new SendMsgDetailEntity(entity.getMsgTxt().get(), $id, entity.getId(),
                        device.getId(), 1, device.getStoreId(), device.getCompanyId(), userContext, groupId, order);
                details.add(txt_detail);
            }
        }
        return details;
    }

    private List<SendMsgDetailEntity> sendSoftTxt(SendMsgEntity entity, DevicesEntity device, LoginUserContext userContext) {
        List<SendMsgDetailEntity> details = Lists.newArrayList();
        for (String $id : entity.getWeixinIds()) {
            String groupId = UUID.randomUUID().toString();
            int order = 0;
            if (entity.getMsgTxt().isPresent()) {
                SendMsgDetailEntity txt_detail = new SendMsgDetailEntity(entity.getMsgTxt().get(), $id, entity.getId(),
                        device.getId(), 49, device.getStoreId(), device.getCompanyId(), userContext, groupId, order);
                details.add(txt_detail);
            }
        }
        return details;
    }

    public void batchToSend(List<SendMsgDetailEntity> details) {
        if (CollectionUtils.isEmpty(details)) return;
        getJdbcTemplate().batchUpdate(getExecSql("batch_insert_details", null), details, 2000,
                new ParameterizedPreparedStatementSetter<SendMsgDetailEntity>() {

                    @Override
                    public void setValues(PreparedStatement ps, SendMsgDetailEntity entity) throws SQLException {
                        //content,  touser, `type`, isgroup, todeviceid, store_id, company_id, UUID
                        ps.setString(1, entity.getContent());
                        ps.setString(2, entity.getWeixinId());
                        ps.setInt(3, entity.getMsgType());
                        ps.setInt(4, entity.getIsgroup());
                        ps.setString(5, entity.getToDeviceId());
                        ps.setInt(6, entity.getStoreId());
                        ps.setInt(7, entity.getCompanyId());
                        ps.setString(8, UUID.randomUUID().toString());
                        ps.setInt(9, (Integer) entity.getCreateUserId());
                        ps.setString(10, entity.getBatchNo());
                        ps.setInt(11, entity.getOrder());
                    }
                });
        if (logger.isDebugEnabled())
            logger.debug(String.format("本批次共计写入微信待发送列表记录 共计 %s 条", details.size()));

    }


    @Override
    protected ResultSetExtractor<SendMsgEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<SendMsgEntity> {

        @Override
        public SendMsgEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return buildByResultSet(resultSet);
            }
            return null;
        }

    }

    private SendMsgEntity buildByResultSet(ResultSet rs) throws SQLException {
        // String id, String msgTxt, String imageId, String imageUrl, int sum, int msgType,
        //                  Collection<String> weixinIds, Integer storeId, Integer companyId,
        //                  Integer sendUserId, Date sendTime
        String img_vals = rs.getString("imageUrl");
        String[] map = Strings.isNullOrEmpty(img_vals) ? null : StringUtils.split(img_vals, ',');
        return new SendMsgEntity(rs.getString("id"),
                rs.getString("msgText"), map, rs.getInt("msgNum"), rs.getInt("weixinNum"),
                rs.getInt("msgType"),
                Splitter.on(',').splitToList(rs.getString("weixinIds")),
                rs.getInt("storeId"), rs.getInt("companyId"),
                rs.getInt("createUserId"), rs.getDate("createTime"), rs.getLong("msgTempId"), rs.getString("deviceId"));
    }

}
