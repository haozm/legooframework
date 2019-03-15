package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.dict.entity.KvDictEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class SMSSendRuleEntity extends BaseEntity<String> {

    private final String businessType, businessDesc;
    private SMSChannel smsChannel;
    private boolean freeSend;
    private boolean enabled;

    SMSSendRuleEntity(KvDictEntity businessType, SMSChannel smsChannel, boolean freeSend) {
        super(CommonsUtils.randomId(16).toUpperCase(), -1L, -1L);
        Preconditions.checkArgument(businessType.getType().equals("SMS_BUS_TYPE"), "错误的参数类型...%s", businessType);
        this.businessType = businessType.getValue();
        this.smsChannel = smsChannel;
        this.freeSend = freeSend;
        this.enabled = true;
        this.businessDesc = businessType.getName();
    }

    SMSSendRuleEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.businessType = ResultSetUtil.getString(res, "businessType");
            this.enabled = ResultSetUtil.getObject(res, "enabled", Integer.class) == 1;
            this.freeSend = ResultSetUtil.getObject(res, "freeSend", Integer.class) == 1;
            int _smsChannel = ResultSetUtil.getObject(res, "smsChannel", Integer.class);
            this.smsChannel = SMSChannel.paras(_smsChannel);
            this.businessDesc = ResultSetUtil.getString(res, "businessDesc");
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSSendRuleEntity has SQLException", e);
        }
    }

    private SMSSendRuleEntity(SMSSendRuleEntity businessRule, SMSChannel smsChannel, boolean freeSend) {
        super(CommonsUtils.randomId(16).toUpperCase());
        this.businessType = businessRule.getBusinessType();
        this.businessDesc = businessRule.getBusinessDesc();
        this.smsChannel = smsChannel;
        this.freeSend = freeSend;
    }

    public boolean isFreeSend() {
        return freeSend;
    }

    public boolean isTradeChannel() {
        return SMSChannel.TradeChannel == smsChannel;
    }

    public boolean isMarketChannel() {
        return SMSChannel.MarketChannel == smsChannel;
    }

    String getBusinessType() {
        return businessType;
    }

    Optional<SMSSendRuleEntity> modify(SMSChannel smsChannel, boolean freeSend) {
        SMSSendRuleEntity clone = new SMSSendRuleEntity(this, smsChannel, freeSend);
        clone.freeSend = freeSend;
        clone.smsChannel = smsChannel;
        if (this.equals(clone)) return Optional.empty();
        return Optional.of(clone);
    }

    SMSChannel getSmsChannel() {
        return smsChannel;
    }

    public String getBusinessDesc() {
        return businessDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SMSSendRuleEntity)) return false;
        SMSSendRuleEntity that = (SMSSendRuleEntity) o;
        return freeSend == that.freeSend &&
                enabled == that.enabled &&
                Objects.equal(businessType, that.businessType) &&
                smsChannel == that.smsChannel;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(businessType, smsChannel, freeSend, enabled);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("businessType", businessType)
                .add("smsChannel", smsChannel)
                .add("freeSend", freeSend)
                .add("enabled", enabled)
                .add("businessDesc", businessDesc)
                .toString();
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("businessType", "businessDesc", "smsChannel", "freeSend", "enabled");
        params.put("businessType", businessType);
        params.put("smsChannel", smsChannel.getChannel());
        params.put("freeSend", freeSend ? 1 : 0);
        params.put("enabled", enabled ? 1 : 0);
        return params;
    }
}
