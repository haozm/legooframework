package com.csosm.module.webchat.event;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.event.BusEvent;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.webchat.entity.SendMsgDetailEntity;
import com.csosm.module.webchat.entity.SendMsgEntity;
import com.google.common.base.MoreObjects;

import java.util.List;

public class BatchSendWetchatMsgEvent implements BusEvent {

    private final StoreEntity store;
    private final OrganizationEntity company;
    private final SendMsgEntity sendMsgEntity;
    private final LoginUserContext userContext;
    private final List<SendMsgDetailEntity> sendMsgDetails;

    public BatchSendWetchatMsgEvent(StoreEntity store, SendMsgEntity sendMsgEntity,
                                    OrganizationEntity company, LoginUserContext userContext) {
        this.store = store;
        this.sendMsgEntity = sendMsgEntity;
        this.company = company;
        this.userContext = userContext;
        this.sendMsgDetails = null;
    }

    @Override
    public void setLoginUser(LoginUserContext user) {

    }

    public BatchSendWetchatMsgEvent(List<SendMsgDetailEntity> sendMsgDetails) {
        this.sendMsgDetails = sendMsgDetails;
        this.store = null;
        this.sendMsgEntity = null;
        this.company = null;
        this.userContext = null;
    }

    public List<SendMsgDetailEntity> getSendMsgDetails() {
        return sendMsgDetails;
    }

    public LoginUserContext getUserContext() {
        return userContext;
    }

    public StoreEntity getStore() {
        return store;
    }

    public OrganizationEntity getCompany() {
        return company;
    }

    public SendMsgEntity getSendMsgEntity() {
        return sendMsgEntity;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("store", store)
                .add("sendMsgEntity", sendMsgEntity)
                .toString();
    }
}
