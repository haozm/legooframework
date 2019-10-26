package com.legooframework.model.templatemgs.entity;

import com.google.common.base.*;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.util.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MsgReplaceHoldEntity extends BaseEntity<Long> {

    private String fieldTag, replaceToken, defaultValue;
    private TokenType tokenType;
    private Map<String, String> enumMap;
    private boolean enabled;

    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("fieldTag", fieldTag);
        params.put("replaceToken", String.format("{%s}", replaceToken));
        params.put("enabled", enabled);
        params.put("tokenType", tokenType.toString());
        switch (this.tokenType) {
            case STRING:
                params.put("defaultValue", defaultValue);
                break;
            case ENUM:
                params.put("defaultValue", Joiner.on(',').withKeyValueSeparator('=').join(enumMap));
                break;
            case DATE:
                params.put("defaultValue", defaultValue);
                break;
            default:
                break;
        }
        return params;
    }

    private MsgReplaceHoldEntity(LoginContext user, String fieldTag, String replaceToken, TokenType tokenType,
                                 String defaultValue, Map<String, String> enumMap) {
        super(0L, user.getTenantId(), user.getLoginId());
        Preconditions.checkArgument(!Strings.isNotEmpty(fieldTag), "参数 fieldTag 不可以为空值");
        Preconditions.checkArgument(!Strings.isNotEmpty(replaceToken), "参数 replaceToken 不可以为空值");
        this.fieldTag = fieldTag;
        this.replaceToken = replaceToken;
        this.defaultValue = defaultValue;
        this.tokenType = tokenType;
        if (TokenType.DATE == this.tokenType) {
            Preconditions.checkNotNull(defaultValue, "%s 对应的辅助信息未设置...", this.replaceToken);
            this.defaultValue = defaultValue;
        } else if (TokenType.ENUM == this.tokenType) {
            Preconditions.checkArgument(MapUtils.isNotEmpty(enumMap), "%s 对应的枚举信息不存在...", this.replaceToken);
            this.enumMap = enumMap;
        } else {
            this.defaultValue = defaultValue;
        }
    }

    static MsgReplaceHoldEntity createString(LoginContext user, String fieldTag, String replaceToken, String defaultValue) {
        return new MsgReplaceHoldEntity(user, fieldTag, replaceToken, TokenType.STRING, defaultValue, null);
    }

    static MsgReplaceHoldEntity createDate(LoginContext user, String fieldTag, String replaceToken, String defaultValue) {
        return new MsgReplaceHoldEntity(user, fieldTag, replaceToken, TokenType.DATE, defaultValue, null);
    }

    static MsgReplaceHoldEntity createEnum(LoginContext user, String fieldTag, String replaceToken, Map<String, String> enumMap) {
        return new MsgReplaceHoldEntity(user, fieldTag, replaceToken, TokenType.ENUM, null, enumMap);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap(excludes);
        params.put("fieldTag", fieldTag);
        params.put("replaceToken", replaceToken);
        params.put("tokenType", tokenType.toString());
        if (TokenType.ENUM == this.tokenType) {
            params.put("defaultValue", Joiner.on(',').withKeyValueSeparator('=').join(enumMap));
        } else {
            params.put("defaultValue", defaultValue);
        }
        params.put("enabled", enabled ? 1 : 0);
        return params;
    }

    void formatToken(Map<String, Object> params, List<String> tokens) {
        Object value = MapUtils.getObject(params, fieldTag);
        if (!tokens.contains(this.replaceToken)) return;
        switch (this.tokenType) {
            case STRING:
                if (null == value) {
                    Preconditions.checkNotNull(this.defaultValue, "%s默认替换值为空...", replaceToken);
                    params.put(String.format("{%s}", replaceToken), this.defaultValue);
                } else {
                    String replace = (String) value;
                    params.put(String.format("{%s}", replaceToken), replace);
                }
                break;
            case ENUM:
                if (null == value) {
                    params.put(String.format("{%s}", replaceToken), MapUtils.getString(enumMap, "*", ""));
                } else {
                    String replace = value.toString();
                    if (enumMap.containsKey(replace)) {
                        params.put(String.format("{%s}", replaceToken), enumMap.get(replace));
                    } else {
                        params.put(String.format("{%s}", replaceToken), MapUtils.getString(enumMap, "*", ""));
                    }
                }
                break;
            case DATE:
                Preconditions.checkNotNull(value, "%s 对应的值未 null,无法置换...", this.fieldTag);
                Date replace = (Date) value;
                params.put(String.format("{%s}", replaceToken), DateFormatUtils.format(replace, defaultValue));
                break;
            default:
        }

    }

    MsgReplaceHoldEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.fieldTag = ResultSetUtil.getString(res, "fieldTag");
            this.replaceToken = ResultSetUtil.getString(res, "replaceToken");
            String _tokenType = ResultSetUtil.getString(res, "tokenType");
            this.tokenType = EnumUtils.getEnum(TokenType.class, _tokenType);
            String _defaultValue = ResultSetUtil.getOptString(res, "defaultValue", null);
            switch (this.tokenType) {
                case DATE:
                    Preconditions.checkNotNull(_defaultValue, "%s 为 DATE 需要指定输出格式...", this.replaceToken);
                    this.defaultValue = _defaultValue;
                    break;
                case ENUM:
                    Preconditions.checkNotNull(_defaultValue, "%s 为 ENUM 需指定取值明细", this.replaceToken);
                    this.enumMap = Splitter.on(',').withKeyValueSeparator('=').split(_defaultValue);
                    break;
                case STRING:
                    this.defaultValue = _defaultValue;
                    break;
                default:
                    throw new IllegalArgumentException(String.format("非法的入参 tokenType = %s ", tokenType));
            }
            this.enabled = ResultSetUtil.getBooleanByInt(res, "enabled");
        } catch (SQLException e) {
            throw new RuntimeException("Restore MsgReplaceHoldEntity has SQLException", e);
        }
    }

    public String getFieldTag() {
        return fieldTag;
    }

    public String getReplaceToken() {
        return replaceToken;
    }

    public Optional<String> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public boolean isEnabled() {
        return enabled;
    }

    boolean isMyCompany(LoginContext user) {
        return this.getTenantId().equals(user.getTenantId());
    }

    boolean isMyCompany(CrmOrganizationEntity company) {
        return Ints.compare(this.getTenantId().intValue(), company.getCompanyId()) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MsgReplaceHoldEntity that = (MsgReplaceHoldEntity) o;
        return this.enabled = that.enabled &&
                this.tokenType == that.tokenType &&
                Objects.equal(fieldTag, that.fieldTag) &&
                Objects.equal(replaceToken, that.replaceToken) &&
                Objects.equal(this.getTenantId(), that.getTenantId()) &&
                Objects.equal(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fieldTag, replaceToken, defaultValue, enabled, tokenType, this.getTenantId());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fieldTag", fieldTag)
                .add("replaceToken", replaceToken)
                .add("defaultValue", defaultValue)
                .add("enumMap", enumMap)
                .add("tenantId", this.getTenantId())
                .add("tokenType", this.tokenType)
                .add("enabled", enabled)
                .toString();
    }
}
