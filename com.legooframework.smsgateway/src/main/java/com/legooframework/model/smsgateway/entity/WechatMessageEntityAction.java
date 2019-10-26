package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collection;

public class WechatMessageEntityAction extends BaseEntityAction<WechatMessageEntity> {

    private static final Logger logger = LoggerFactory.getLogger(WechatMessageEntityAction.class);

    public WechatMessageEntityAction() {
        super(null);
    }

    public void batchSend(Collection<WechatMessageEntity> messages) {
        if (CollectionUtils.isEmpty(messages)) return;
        super.batchInsert("batchInsert", messages);
        if (logger.isDebugEnabled())
            logger.debug(String.format("本次发送微信Size 共计 %s 完成 ", messages.size()));
    }

    @Override
    protected RowMapper<WechatMessageEntity> getRowMapper() {
        return null;
    }
}
