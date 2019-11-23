package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.util.List;

public class SendMessageAgg {

    private final Integer companyId, storeId;
    private final List<SendMessage> builders;

    public SendMessageAgg(Integer companyId, Integer storeId) {
        this.companyId = companyId;
        this.storeId = storeId;
        this.builders = Lists.newArrayList();
    }

    public void addBuilder(SendMessage messageBuilder) {
        if (null != messageBuilder)
            this.builders.add(messageBuilder);
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public List<SendMessage> getBuilders() {
        return builders;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("builders' size ", builders.size())
                .toString();
    }
}
