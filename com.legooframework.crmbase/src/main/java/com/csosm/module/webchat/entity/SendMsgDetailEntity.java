package com.csosm.module.webchat.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.StoreEntity;
import org.joda.time.DateTime;

public class SendMsgDetailEntity extends BaseEntity<String> {

    private String content;
    private String weixinId;
    private String batchNo;
    private String toDeviceId;
    private int msgType;
    private int isgroup = 0;
    private Integer storeId, companyId;
    private String groupId;
    private int order;

    public SendMsgDetailEntity(String content, String weixinId, String batchNo,
                               String toDeviceId, int msgType, Integer storeId, Integer companyId,
                               LoginUserContext userContext, String groupId, int order) {
        super(weixinId, userContext.getUserId(), DateTime.now().toDate());
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

    static SendMsgDetailEntity createSendMsgDetailEntity4Txt(String content, String weixinId, String batchNo,
                                                             String toDeviceId, Integer storeId, Integer companyId,
                                                             LoginUserContext userContext, String groupId, int order) {
        return new SendMsgDetailEntity(content, weixinId, batchNo, toDeviceId, 1, storeId,
                companyId, userContext, groupId, order);
    }
    
    
    static SendMsgDetailEntity createSendMsgDetailEntity4SoftTxt(String content, String weixinId, String batchNo,
            String toDeviceId, Integer storeId, Integer companyId,
            LoginUserContext userContext, String groupId, int order) {
    	return new SendMsgDetailEntity(content, weixinId, batchNo, toDeviceId, 49, storeId,
    			companyId, userContext, groupId, order);
    }

    static SendMsgDetailEntity createSendMsgDetailEntity4Img(String content, String weixinId, String batchNo,
                                                             String toDeviceId, Integer storeId, Integer companyId,
                                                             LoginUserContext userContext, String groupId, int order) {
        return new SendMsgDetailEntity(content, weixinId, batchNo, toDeviceId, 3, storeId,
                companyId, userContext, groupId, order);
    }


    public SendMsgDetailEntity(String content, String weixinId, String batchNo,
                               String toDeviceId, int msgType, StoreEntity store,
                               Integer userId, String groupId, int order) {
        super(weixinId, userId, DateTime.now().toDate());
        this.content = content;
        this.weixinId = weixinId;
        this.batchNo = batchNo;
        this.toDeviceId = toDeviceId;
        this.msgType = msgType;
        this.storeId = store.getId();
        this.groupId = groupId;
        this.order = order;
        this.companyId = store.getCompanyId().or(-1);
    }

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
}
