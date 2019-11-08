package com.legooframework.model.smsprovider.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SMSProviderEntity extends BaseEntity<String> {

    private String name;
    private boolean enabled;
    private List<SMSSubAccountEntity> subAccounts;

    SMSProviderEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.name = ResultSetUtil.getString(res, "name");
            this.enabled = ResultSetUtil.getBooleanByInt(res, "enabled");
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSProviderEntity has SQLException", e);
        }
    }

    void setSubAccounts(List<SMSSubAccountEntity> subAccounts) {
        this.subAccounts = subAccounts;
    }

    /**
     * 发送短信通道
     *
     * @param smsChannel 短信通道
     * @return String
     */
    public String getHttpSendUrl(SMSChannel smsChannel) {
        List<SMSSubAccountEntity> subList = this.subAccounts.stream()
                .filter(SMSSubAccountEntity::isEnabled)
                .filter(x -> x.isChannel(smsChannel)).collect(Collectors.toList());
        Preconditions.checkState(CollectionUtils.isNotEmpty(subList), "当前无合适通道...");
        return subList.get(0).getHttpSendUrl();
    }

    public Optional<String> getSmsSuffix(SMSChannel channel) {
        Optional<SMSSubAccountEntity> account = this.subAccounts.stream().filter(x -> x.isChannel(channel)).findFirst();
        Preconditions.checkState(account.isPresent(), "不存在 SMSChannel = %s 对应的通道定义...", channel);
        return account.get().getSmsSuffix();
    }

    /**
     * 回复短信通道
     *
     * @return 妈妈喊你回家吃饭
     */
    public Optional<List<String>> getHttpReplayUrl() {
        List<SMSSubAccountEntity> subList = this.subAccounts.stream()
                .filter(SMSSubAccountEntity::isEnabled)
                .filter(x -> x.isChannel(SMSChannel.MarketChannel)).collect(Collectors.toList());
        if (subList.isEmpty()) return Optional.empty();
        List<String> list = subAccounts.stream().map(SMSSubAccountEntity::getHttpReplyUrl).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

    /**
     * 状态回查短信通道
     *
     * @param channel
     * @return
     */
    public String getHttpStatusByMobilesUrl(SMSChannel channel) {
        List<SMSSubAccountEntity> subList = this.subAccounts.stream()
                .filter(SMSSubAccountEntity::isEnabled)
                .filter(x -> x.isChannel(channel)).collect(Collectors.toList());
        Preconditions.checkState(CollectionUtils.isNotEmpty(subList), "当前无合适通道用于%s...", channel);
        return subList.get(0).getHttpStatusUrl();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("subAccounts", subAccounts)
                .toString();
    }
}
