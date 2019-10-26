package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * 短信发送规则定义
 */
public class SMSSendRuleEntity extends BaseEntity<String> {

    private final BusinessType businessType;
    private SMSChannel smsChannel;
    private boolean freeSend;
    private boolean enabled;

    SMSSendRuleEntity(BusinessType businessType, SMSChannel smsChannel, boolean freeSend) {
        super(CommonsUtils.randomId(16).toUpperCase(), -1L, -1L);
        Preconditions.checkNotNull(businessType, "错误的参数businessType...不可以为空值");
        this.businessType = businessType;
        this.smsChannel = smsChannel;
        this.freeSend = freeSend;
        this.enabled = true;
    }

    SMSSendRuleEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.businessType = BusinessType.parse(ResultSetUtil.getString(res, "businessType"));
            this.enabled = ResultSetUtil.getObject(res, "enabled", Integer.class) == 1;
            this.freeSend = ResultSetUtil.getObject(res, "freeSend", Integer.class) == 1;
            int _smsChannel = ResultSetUtil.getObject(res, "smsChannel", Integer.class);
            this.smsChannel = SMSChannel.paras(_smsChannel);
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSSendRuleEntity has SQLException", e);
        }
    }

    private SMSSendRuleEntity(SMSSendRuleEntity smsSendRule, SMSChannel smsChannel, boolean freeSend) {
        super(CommonsUtils.randomId(16).toUpperCase());
        this.businessType = smsSendRule.getBusinessType();
        this.smsChannel = smsChannel;
        this.freeSend = freeSend;
        this.enabled = true;
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

    BusinessType getBusinessType() {
        return businessType;
    }

    Optional<SMSSendRuleEntity> modify(SMSChannel smsChannel, boolean freeSend) {
        SMSSendRuleEntity clone = new SMSSendRuleEntity(this, smsChannel, freeSend);
        clone.freeSend = freeSend;
        clone.smsChannel = smsChannel;
        if (this.equals(clone)) return Optional.empty();
        return Optional.of(clone);
    }

    public String addPrefix(String content, String prefix) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "短信前缀不可以为空值...");
        String _prefix = String.format("【%s】", prefix);
        if (StringUtils.startsWith(content, _prefix)) return content;
        return String.format("【%s】%s", prefix, content);
    }

    public String addPrefixAndSuffix(String content, String prefix, String suffix) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "短信前缀不可以为空值...");
        String _prefix = String.format("【%s】", prefix);
        if (StringUtils.startsWith(content, _prefix) && !StringUtils.endsWith(content, suffix)) {
            content = String.format("%s%s", content, suffix);
        } else if (!StringUtils.startsWith(content, _prefix) && StringUtils.endsWith(content, suffix)) {
            content = String.format("%s%s", _prefix, content);
        } else if (!StringUtils.startsWith(content, _prefix) && !StringUtils.endsWith(content, suffix)) {
            content = String.format("【%s】%s%s", prefix, content, suffix);
        }
        return content;
    }

    SMSChannel getSmsChannel() {
        return smsChannel;
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
                .toString();
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("businessType", "businessDesc", "smsChannel", "freeSend", "enabled");
        params.put("businessType", businessType.toString());
        params.put("smsChannel", smsChannel.getChannel());
        params.put("freeSend", freeSend ? 1 : 0);
        params.put("enabled", enabled ? 1 : 0);
        return params;
    }
}
