package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.util.List;

public class SendMessageAgg {

    private final Integer companyId, storeId;
    private final List<SendMessageBuilder> builders;

    public SendMessageAgg(Integer companyId, Integer storeId) {
        this.companyId = companyId;
        this.storeId = storeId;
        this.builders = Lists.newArrayList();
    }

    public void addBuilder(SendMessageBuilder messageBuilder) {
        if (null != messageBuilder)
            this.builders.add(messageBuilder);
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