package com.legooframework.model.imchat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class ChatKeywordEntity extends BaseEntity<Long> {

    private final String keywords;
    private boolean enabled;

    ChatKeywordEntity(Long id, String keywords, LoginContext loginContext) {
        super(id, loginContext.getTenantId(), loginContext.getLoginId());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(keywords), "过滤关键字不可以为空.");
        this.keywords = keywords;
        this.enabled = true;
    }

    ChatKeywordEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.keywords = ResultSetUtil.getString(res, "keywords");
            this.enabled = ResultSetUtil.getBooleanByInt(res, "enabledFlag");
        } catch (SQLException e) {
            throw new RuntimeException("Restore ChatKeywordEntity has SQLException", e);
        }
    }

    Optional<ChatKeywordEntity> enabled() {
        if (this.isEnabled()) return Optional.empty();
        ChatKeywordEntity clone = (ChatKeywordEntity) super.cloneMe();
        clone.enabled = true;
        return Optional.of(clone);
    }

    Optional<ChatKeywordEntity> disEnabled() {
        if (!this.isEnabled()) return Optional.empty();
        ChatKeywordEntity clone = (ChatKeywordEntity) super.cloneMe();
        clone.enabled = false;
        return Optional.of(clone);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> map = super.toParamMap("enabled");
        map.put("enabledFlag", enabled ? 1 : 0);
        return map;
    }

    public String getKeywords() {
        return keywords;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean equesKeywords(String keywords) {
        return StringUtils.equals(this.keywords, keywords);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatKeywordEntity)) return false;
        if (!super.equals(o)) return false;
        ChatKeywordEntity that = (ChatKeywordEntity) o;
        return enabled == that.enabled &&
                Objects.equal(keywords, that.keywords);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("keywords", this.getKeywords());
        map.put("id", this.getId());
        map.put("enbaled", this.isEnabled());
        return map;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), keywords, enabled);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("keywords", keywords)
                .add("enabled", enabled)
                .add("tenant", getTenantId())
                .toString();
    }
}
