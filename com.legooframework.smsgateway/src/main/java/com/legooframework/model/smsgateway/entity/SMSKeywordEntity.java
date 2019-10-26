package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class SMSKeywordEntity extends BaseEntity<Long> {

    private String keyword;
    private boolean enabled = true;

    SMSKeywordEntity(String keyword, boolean enabled) {
        super(0L);
        this.keyword = keyword;
        this.enabled = enabled;
    }

    SMSKeywordEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.keyword = res.getString("keyword");
            this.enabled = ResultSetUtil.getBooleanByInt(res, "enabled");
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSKeywordEntity has SQLException", e);
        }
    }

    boolean isEnabled() {
        return enabled;
    }

    Optional<SMSKeywordEntity> enabled(boolean enabled) {
        if (this.enabled == enabled) return Optional.empty();
        SMSKeywordEntity clone = (SMSKeywordEntity) cloneMe();
        clone.enabled = enabled;
        return Optional.of(clone);
    }

    boolean equalsByKeyword(String keyword) {
        return StringUtils.equals(this.keyword, keyword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SMSKeywordEntity that = (SMSKeywordEntity) o;
        return enabled == that.enabled &&
                Objects.equal(keyword, that.keyword);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap(excludes);
        params.put("keyword", keyword);
        params.put("enabled", enabled ? 1 : 0);
        return params;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(keyword, enabled);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("keyword", keyword)
                .add("enabled", enabled)
                .toString();
    }
}
