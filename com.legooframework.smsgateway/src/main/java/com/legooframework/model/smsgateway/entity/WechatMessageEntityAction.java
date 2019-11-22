package com.legooframework.model.smsgateway.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public void sendWxMessage(MsgTransportBatchEntity transportBatch) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("batchNo", transportBatch.getBatchNo());
        Optional<List<WechatMessageEntity>> messaeges = super.queryForEntities("loadMsg4Send", params, getRowMapper());
        if (!messaeges.isPresent()) return;
        List<String> msgIds = messaeges.get().stream().map(x -> String.format("'%s'", x.getId())).collect(Collectors.toList());
        params.put("msgIds", msgIds);
        params.put("sendStatus", 2);
        super.updateAction("updateSendStatus", params);
        this.batchSend(messaeges.get());
        params.put("sendStatus", 3);
        super.updateAction("updateSendStatus", params);
    }

    @Override
    protected RowMapper<WechatMessageEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<WechatMessageEntity> {
        @Override
        public WechatMessageEntity mapRow(ResultSet res, int i) throws SQLException {
            return new WechatMessageEntity(res.getString("id"), res);
        }
    }
}
