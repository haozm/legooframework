package com.legooframework.model.covariant.entity;

import com.legooframework.model.core.base.entity.BaseEntity;

public class SendWechatDetailEntity extends BaseEntity<String> {

    private String content;
    private String weixinId;
    private String batchNo;
    private String toDeviceId;
    private int msgType;
    private int isgroup = 0;
    private Integer storeId, companyId;
    private String groupId;
    private int order;

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

    public int getIsgroup() {
        return isgroup;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getOrder() {
        return order;
    }

    private SendWechatDetailEntity(String content, String weixinId, String batchNo, String toDeviceId, int msgType,
                                   Integer storeId, Integer companyId,
                                   String groupId, int order) {
        super(weixinId);
        this.content = content;
        this.weixinId = weixinId;
        this.batchNo = batchNo;
        this.toDeviceId = toDeviceId;
        this.msgType = msgType;
        this.storeId = storeId;
        this.groupId = groupId;
        this.order = order;
        this.companyId = companyId;
    }

    static SendWechatDetailEntity createWechatMsg4Txt(String content, String weixinId, String batchNo,
                                                      String toDeviceId, Integer storeId, Integer companyId,
                                                      String groupId, int order) {
        return new SendWechatDetailEntity(content, weixinId, batchNo, toDeviceId, 1, storeId, companyId, groupId, order);
    }

    static SendWechatDetailEntity createWechatMsg4SoftTxt(String content, String weixinId, String batchNo,
                                                          String toDeviceId, Integer storeId, Integer companyId,
                                                          String groupId, int order) {
        return new SendWechatDetailEntity(content, weixinId, batchNo, toDeviceId, 49, storeId,
                companyId, groupId, order);
    }


    static SendWechatDetailEntity createWechatMsg4Img(String content, String weixinId, String batchNo,
                                                      String toDeviceId, Integer storeId, Integer companyId,
                                                      String groupId, int order) {
        return new SendWechatDetailEntity(content, weixinId, batchNo, toDeviceId, 3, storeId,
                companyId, groupId, order);
    }

}
