package com.legooframework.model.covariant.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class SendWechatEntityAction extends BaseEntityAction<SendWechatEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SendWechatEntityAction.class);

    public SendWechatEntityAction() {
        super(null);
    }

    public void batchSendMsg(Collection<SendWechatEntity> wx_msges) {
        if (CollectionUtils.isEmpty(wx_msges)) return;
        super.batchInsert("batchInsert", wx_msges);
        List<SendWechatDetailEntity> details = Lists.newArrayList();
        wx_msges.forEach(x -> details.addAll(x.toMessageDetail()));
        sendDetail(details);
    }

    public void sendMsg(String msgTxt, String[] imageInfo, Collection<String> wechatIds, StoEntity store,
                        BusinessType businessType) {
        Preconditions.checkState(CollectionUtils.isNotEmpty(wechatIds), "发送的微信ID集合不可以为空.");
        SendWechatEntity entity = new SendWechatEntity(msgTxt, imageInfo, wechatIds, store, businessType);
        super.updateAction(entity, "insert");
        List<SendWechatDetailEntity> details = entity.toMessageDetail();
        sendDetail(details);
    }

    private void sendDetail(List<SendWechatDetailEntity> details) {
        if (CollectionUtils.isEmpty(details)) return;
        super.batchUpdate("batch_insert_details", (ps, entity) -> {
            ps.setString(1, entity.getContent());
            ps.setString(2, entity.getWeixinId());
            ps.setInt(3, entity.getMsgType());
            ps.setInt(4, entity.getIsgroup());
            ps.setString(5, entity.getToDeviceId());
            ps.setInt(6, entity.getStoreId());
            ps.setInt(7, entity.getCompanyId());
            ps.setString(8, UUID.randomUUID().toString());
            ps.setInt(9, 0);
            ps.setString(10, entity.getBatchNo());
            ps.setInt(11, entity.getOrder());
        }, details);
        if (logger.isDebugEnabled())
            logger.debug(String.format("本批次共计写入微信待发送列表记录 共计 %s 条", details.size()));

    }

    @Override
    protected RowMapper<SendWechatEntity> getRowMapper() {
        return null;
    }
}
