package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.util.List;

public class SendMessageAgg {

    private final Integer companyId, storeId;
    private final List<SendMessageBuilder> messages;

    public SendMessageAgg(Integer companyId, Integer storeId) {
        this.companyId = companyId;
        this.storeId = storeId;
        this.messages = Lists.newArrayList();
    }

    public void addMessageBuilder(SendMessageBuilder messages) {
        if (null != messages)
            this.messages.add(messages);
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public List<SendMessageBuilder> getMessagesBuilder() {
        return messages;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("messages' size ", messages.size())
                .toString();
    }
}
