package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class WechatMessageEntity extends BaseEntity<String> implements BatchSetter {

    private String content;
    private String weixinId;
    private String batchNo;
    private String toDeviceId;
    private int msgType;
    private int isgroup = 0;
    private Integer storeId, companyId;
    private String groupId;
    private int order;

    private WechatMessageEntity(String content, String weixinId, String batchNo, String toDeviceId, int msgType, Integer storeId,
                                String groupId, int order, Long userId, Long companyId) {
        super(weixinId, userId, companyId);
        this.content = content;
        this.weixinId = weixinId;
        this.batchNo = batchNo;
        this.toDeviceId = toDeviceId;
        this.msgType = msgType;
        this.storeId = storeId;
        this.groupId = groupId;
        this.order = order;
        this.companyId = companyId.intValue();
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        // content,  touser, `type`, isgroup, todeviceid, store_id, company_id, UUID,fromuser, recieveTime ,sendFlag, IDGroup, IDSort
        ps.setObject(1, content);
        ps.setObject(2, weixinId);
        ps.setObject(3, msgType);
        ps.setObject(4, isgroup);
        ps.setObject(5, toDeviceId);
        ps.setObject(6, storeId);
        ps.setObject(7, companyId);
        ps.setObject(8, UUID.randomUUID().toString());
        ps.setObject(9, getCreator());
        ps.setString(10, this.batchNo);
        ps.setInt(11, this.order);
    }

    public static WechatMessageEntity createMessage4Txt(String content, String weixinId, String batchNo,
                                                        String toDeviceId, Integer storeId,
                                                        String groupId, int order, Long userId, Long companyId) {
        return new WechatMessageEntity(content, weixinId, batchNo, toDeviceId, 1, storeId,
                groupId, order, companyId, userId);
    }


//    static WechatMessageEntity createSendMsgDetailEntity4SoftTxt(String content, String weixinId, String batchNo,
//                                                                 String toDeviceId, Integer storeId, Integer companyId,
//                                                                 LoginUserContext userContext, String groupId, int order) {
//        return new WechatMessageEntity(content, weixinId, batchNo, toDeviceId, 49, storeId,
//                companyId, userContext, groupId, order);
//    }
//
//    static WechatMessageEntity createSendMsgDetailEntity4Img(String content, String weixinId, String batchNo,
//                                                             String toDeviceId, Integer storeId, Integer companyId,
//                                                             LoginUserContext userContext, String groupId, int order) {
//        return new WechatMessageEntity(content, weixinId, batchNo, toDeviceId, 3, storeId,
//                companyId, userContext, groupId, order);
//    }

    public String getContent() {
        return content;
    }

    public String getWeixinId() {
        return weixinId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public String getToDeviceId() {
        return toDeviceId;
    }

    public int getMsgType() {
        return msgType;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getOrder() {
        return order;
    }

    public int getIsgroup() {
        return isgroup;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("content", content)
                .add("weixinId", weixinId)
                .add("batchNo", batchNo)
                .add("toDeviceId", toDeviceId)
                .add("msgType", msgType)
                .add("isgroup", isgroup)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .add("groupId", groupId)
                .add("order", order)
                .toString();
    }
}
