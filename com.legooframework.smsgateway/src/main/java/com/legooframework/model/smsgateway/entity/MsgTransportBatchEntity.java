package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.covariant.entity.StoEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

public class MsgTransportBatchEntity extends BaseEntity<Long> implements BatchSetter {

    private final Integer companyId, storeId;
    private final String batchNo;
    private boolean billing;
    private final SendMode sendMode;
    private final int smsWriteCount, smsWriteOkCount, wxWriteCount, wxWriteOkCount;

    MsgTransportBatchEntity(long id, ResultSet res) {
        super(id);
        try {
            this.companyId = res.getInt("company_id");
            this.storeId = res.getInt("store_id");
            this.batchNo = res.getString("send_batchno");
            this.sendMode = SendMode.paras(res.getInt("send_mode"));
            this.smsWriteCount = res.getInt("sms_write_count");
            this.billing = res.getInt("is_billing") == 1;
            this.smsWriteOkCount = res.getInt("sms_write_ok_count");
            this.wxWriteCount = res.getInt("wx_write_count");
            this.wxWriteOkCount = res.getInt("wx_write_ok_count");
        } catch (SQLException e) {
            throw new RuntimeException("Restore MsgTransportBatchEntity has SQLException", e);
        }
    }

    MsgTransportBatchEntity(StoEntity store, String batchNo, SendMode sendMode, Collection<SendMsg4InitEntity> message) {
        super(0L);
        this.companyId = store.getCompanyId();
        this.storeId = store.getId();
        this.batchNo = batchNo;
        this.billing = false;
        this.sendMode = sendMode == null ? SendMode.ManualSingle : sendMode;
        this.smsWriteCount = (int) message.stream().filter(SendMsg4InitEntity::isSMSMsg).count();
        this.wxWriteCount = (int) message.stream().filter(SendMsg4InitEntity::isWxMsg).count();
        this.smsWriteOkCount = (int) message.stream().filter(SendMsg4InitEntity::isSMSMsg).filter(SendMsg4InitEntity::isEnbaled).count();
        this.wxWriteOkCount = (int) message.stream().filter(SendMsg4InitEntity::isWxMsg).filter(SendMsg4InitEntity::isEnbaled).count();
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public boolean isBilling() {
        return billing;
    }

    public Integer getStoreId() {
        return storeId;
    }

    String getBatchNo() {
        return batchNo;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//        company_id, store_id, send_batchno, is_billing, delete_flag, tenant_id, creator
        ps.setObject(1, companyId);
        ps.setObject(2, storeId);
        ps.setObject(3, batchNo);
        ps.setObject(4, billing ? 1 : 0);
//        sms_write_count, sms_write_ok_count, wx_write_count, wx_write_ok_count
        ps.setObject(5, smsWriteCount);
        ps.setObject(6, smsWriteOkCount);
        ps.setObject(7, wxWriteCount);
        ps.setObject(8, wxWriteOkCount);
        ps.setObject(9, companyId);
        ps.setObject(10, sendMode.getMode());
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("batchNo", batchNo);
        params.put("billing", billing ? 1 : 0);
        params.put("sendMode", sendMode.getMode());
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("batchNo", batchNo)
                .add("billing", billing)
                .add("sendMode", sendMode)
                .add("smsWriteCount", smsWriteCount)
                .add("smsWriteOkCount", smsWriteOkCount)
                .add("wxWriteCount", wxWriteCount)
                .add("wxWriteOkCount", wxWriteOkCount)
                .toString();
    }
}
