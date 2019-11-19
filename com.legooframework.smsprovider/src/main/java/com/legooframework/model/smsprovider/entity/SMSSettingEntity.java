package com.legooframework.model.smsprovider.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class SMSSettingEntity extends BaseEntity<Integer> {

    private final Integer companyId, storeId;
    private String smsPrefix;

    SMSSettingEntity(Integer companyId, Integer storeId, String smsPrefix) {
        super(0);
        this.companyId = companyId;
        this.storeId = storeId;
        this.smsPrefix = smsPrefix;
    }

    SMSSettingEntity(ResultSet res) {
        super(0, res);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getOptObject(res, "storeId", Integer.class).orElse(-1);
            String _prefix = ResultSetUtil.getString(res, "smsPrefix");
            this.smsPrefix = _prefix.length() > 13 ? _prefix.substring(0, 13) : _prefix;
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSSettingEntity has SQLException", e);
        }
    }

    Optional<SMSSettingEntity> changeSmsPrefix(String prefix, LoginContext user) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(StringUtils.trimToEmpty(prefix)), "前缀值不可以为空值...");
        if (StringUtils.equals(smsPrefix, prefix)) return Optional.empty();
        SMSSettingEntity clone = (SMSSettingEntity) this.cloneMe();
        clone.smsPrefix = StringUtils.trimToEmpty(prefix);
        clone.setEditor(user.getLoginId());
        return Optional.of(clone);
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public String getSmsPrefix() {
        return smsPrefix;
    }


    public boolean isCompany(Integer companyId) {
        return this.storeId == -1 && this.companyId.equals(companyId);
    }

    boolean isStore(Integer companyId, Integer storeId) {
        return this.companyId.equals(companyId) && this.storeId.equals(storeId);
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> map = super.toViewMap();
        map.put("companyId", this.companyId);
        map.put("storeId", this.storeId);
        map.put("smsPrefix", this.smsPrefix);
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SMSSettingEntity)) return false;
        SMSSettingEntity that = (SMSSettingEntity) o;
        return Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(smsPrefix, that.smsPrefix);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, storeId, smsPrefix);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("smsPrefix", smsPrefix)
                .toString();
    }
}
