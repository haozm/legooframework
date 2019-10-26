package com.legooframework.model.templatemgs.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MsgTemplateEntity extends BaseEntity<String> {

    private Integer companyId, orgId, storeId;
    private String title, template;
    private List<String> classifies;
    private List<UseScope> useScopes;
    private LocalDate expireDate;
    private boolean blacked;
    private List<String> blackList;
    private final TemplateType tempType;
    private boolean defaulted;

    Integer getOrgId() {
        return orgId;
    }

    public boolean isDefaulted() {
        return defaulted;
    }

    boolean isBlacked() {
        return blacked;
    }

    boolean isClassify(String classifyId) {
        return this.classifies.contains(classifyId);
    }

    List<UseScope> getUseScopes() {
        return useScopes;
    }

    public boolean contains(Collection<UseScope> useScopes) {
        if (CollectionUtils.isEmpty(useScopes)) return false;
        for (UseScope _us : this.useScopes) {
            if (useScopes.contains(_us)) return true;
        }
        return false;
    }

    String getUseScopes4Save() {
        List<Integer> _useScopes = this.useScopes.stream().map(UseScope::getType).collect(Collectors.toList());
        return StringUtils.join(_useScopes, ',');
    }

    public String getTitle() {
        return title;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("blackList", "useScopes", "classifies");
        params.put("companyId", companyId);
        params.put("orgId", -1);
        params.put("title", title);
        params.put("storeId", storeId);
        params.put("template", template);
        params.put("defaulted", defaulted ? 1 : 0);
        params.put("blacked", blacked ? 1 : 0);
        params.put("expireDate", null);
        List<Integer> _useScopes = useScopes.stream().map(UseScope::getType).collect(Collectors.toList());
        params.put("useScopes", StringUtils.join(_useScopes, ','));
        params.put("classifies", classifies.get(0));
        return params;
    }

    // 附加字段信息
    public MsgTemplateEntity(LoginContext user, String title, String template, String classifyId,
                             Collection<UseScope> useScopes, boolean defaulted) {
        super(CommonsUtils.randomId(16));
        if (user.isManager()) {
            this.companyId = -1;
            this.orgId = -1;
            this.tempType = TemplateType.GENERAL;
            this.storeId = -1;
        } else if (user.isStoreManager()) {
            this.companyId = user.getTenantId().intValue();
            this.orgId = -1;
            this.tempType = TemplateType.STORE;
            this.storeId = user.getStoreId();
        } else {
            this.companyId = user.getTenantId().intValue();
            this.orgId = -1;
            this.tempType = TemplateType.COMPANY;
            this.storeId = -1;
        }
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title), "模板标题不可以未空...");
        this.title = title;
        this.defaulted = defaulted;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(template), "模板内容不可以为空值...");
        this.template = template;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(classifyId), "模板所属分类ID不可以为空...");
        this.classifies = Lists.newArrayList(classifyId);
        this.useScopes = Lists.newArrayList(useScopes);
        this.expireDate = null;
        this.blacked = false;
    }

    MsgTemplateEntity(String id, ResultSet res) {
        super(id);
        try {
            String _useScopes = ResultSetUtil.getString(res, "useScopes");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(_useScopes), "useScopes can't be null...");
            this.useScopes = Lists.newArrayList();
            Stream.of(StringUtils.split(_useScopes, ',')).forEach(x ->
                    this.useScopes.add(UseScope.paras(Integer.valueOf(x))));
            this.template = ResultSetUtil.getString(res, "template");
            this.title = ResultSetUtil.getString(res, "title");
            String _classifies = ResultSetUtil.getString(res, "classifies");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(_classifies), "模板分类不可为空...");
            this.classifies = Lists.newArrayList(_classifies);
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(useScopes), "模板使用场景不可为空...");
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            String _type = ResultSetUtil.getString(res, "tempType");
            this.tempType = TemplateType.paras(_type);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.orgId = ResultSetUtil.getObject(res, "orgId", Integer.class);
            Object _blacked_val = ResultSetUtil.getOptObject(res, "blacked", Object.class).orElse(null);
            int _blacked = (_blacked_val == null || _blacked_val.toString().equals("0")) ? 0 : 1;
            this.blacked = _blacked != 0;
            this.defaulted = ResultSetUtil.getBooleanByInt(res, "isDefault");
            this.expireDate = ResultSetUtil.getOptObject(res, "expireDate", Date.class).isPresent() ?
                    LocalDate.fromDateFields(ResultSetUtil.getOptObject(res, "expireDate", Date.class).get()) : null;
            String _blackList = ResultSetUtil.getOptString(res, "blackList", null);
            if (Strings.isNullOrEmpty(_blackList)) {
                this.blackList = null;
            } else {
                this.blackList = Lists.newArrayList(StringUtils.split(_blackList, ','));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore MsgTemplateEntity has SQLException", e);
        }
    }

    /**
     * 修改模板内容
     *
     * @param template
     * @param classifyId
     * @param useScopes
     * @param user
     * @return
     */
    Optional<MsgTemplateEntity> changeTemplate(String title, String template, String classifyId, Collection<UseScope> useScopes,
                                               LoginContext user) {
        Preconditions.checkState(isOWner(user), "模板不属于当前拥有者...,禁止修改。");
        Preconditions.checkState(!this.blacked, "当前模板状态禁用，无法修改内容...");
        MsgTemplateEntity clone = (MsgTemplateEntity) cloneMe();
        if (!Strings.isNullOrEmpty(template)) {
            String _temp = StringUtils.trimToEmpty(template);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(_temp), "非法的模板内容:%s", template);
            clone.template = _temp;
        }
        if (!Strings.isNullOrEmpty(title)) {
            clone.title = title;
        }
        if (!Strings.isNullOrEmpty(classifyId)) {
            clone.classifies = Lists.newArrayList(classifyId);
        }
        if (CollectionUtils.isNotEmpty(useScopes)) {
            clone.useScopes = Lists.newArrayList(useScopes);
        }
        if (clone.equals(this)) return Optional.empty();
        return Optional.of(clone);
    }

    /**
     * 无效模板
     *
     * @param user 我的台哦杨
     * @return 我的事件而
     */
    Optional<MsgTemplateEntity> unBlacked(LoginContext user) {
        if (TemplateType.COMPANY == this.tempType)
            Preconditions.checkState(this.companyId == user.getTenantId().intValue(), "公司不一致,中止操作...");
        if (TemplateType.STORE == this.tempType)
            Preconditions.checkState(this.companyId == user.getTenantId().intValue()
                    && this.storeId == user.getStoreId().intValue(), "门店不一致,中止操作...");
        if (!blacked) return Optional.empty();
        MsgTemplateEntity clone = (MsgTemplateEntity) cloneMe();
        clone.blacked = false;
        return Optional.of(clone);
    }


    Optional<MsgTemplateEntity> setDefaulte() {
        if (defaulted) return Optional.empty();
        MsgTemplateEntity clone = (MsgTemplateEntity) cloneMe();
        clone.defaulted = true;
        return Optional.of(clone);
    }

    Optional<MsgTemplateEntity> setUnDefaulte() {
        if (!defaulted) return Optional.empty();
        MsgTemplateEntity clone = (MsgTemplateEntity) cloneMe();
        clone.defaulted = false;
        return Optional.of(clone);
    }

    /**
     * 禁用 指定的模板
     *
     * @param user u屏幕估计
     * @return huijia
     */
    public Optional<MsgTemplateEntity> blacked(LoginContext user) {
        if (TemplateType.COMPANY == this.tempType)
            Preconditions.checkState(this.companyId == user.getTenantId().intValue(), "公司不一致,中止操作...");
        if (TemplateType.STORE == this.tempType)
            Preconditions.checkState(this.companyId == user.getTenantId().intValue()
                    && this.storeId == user.getStoreId().intValue(), "门店不一致,中止操作...");
        if (blacked) return Optional.empty();
        MsgTemplateEntity clone = (MsgTemplateEntity) cloneMe();
        clone.blacked = true;
        clone.defaulted = false;
        return Optional.of(clone);
    }

    boolean isOWner(LoginContext user) {
        return this.companyId.equals(user.getTenantId().intValue()) && this.storeId.equals(user.getStoreId());
    }

    public String getTemplate() {
        return template;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public String getSingleClassifies() {
        return classifies.get(0);
    }

    boolean isUnBlacked() {
        return !blacked;
    }

    public String toSimpleValue(boolean encoding) {
        return String.format("%s|%s|%s|%s|%s|%s", this.companyId, this.storeId, getSingleClassifies(),
                isDefaulted() ? 1 : 0, encoding ? 1 : 0, encoding ? WebUtils.encodeUrl(template) : template);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MsgTemplateEntity)) return false;
        MsgTemplateEntity that = (MsgTemplateEntity) o;
        return blacked == that.blacked &&
                defaulted == that.defaulted &&
                Objects.equal(title, that.title) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(orgId, that.orgId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(template, that.template) &&
                SetUtils.isEqualSet(classifies, that.classifies) &&
                SetUtils.isEqualSet(useScopes, that.useScopes) &&
                Objects.equal(expireDate, that.expireDate);
    }

    public Map<String, Object> toSimpleMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", this.getId());
        map.put("title", this.title);
        map.put("template", this.template);
        return map;
    }

    public boolean isGeneral() {
        return companyId == -1 && storeId == -1;
    }

    public boolean isCompany() {
        return companyId != -1 && storeId == -1;
    }

    public boolean isStore() {
        return companyId != -1 && storeId != -1;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), companyId, orgId, storeId, template, classifies, useScopes,
                expireDate, blacked, title, defaulted);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("orgId", orgId)
                .add("storeId", storeId)
                .add("template", template)
                .add("classifies", classifies)
                .add("useScopes", useScopes)
                .add("expireDate", expireDate)
                .add("blacked", blacked)
                .add("tempType", tempType)
                .add("title", title)
                .add("defaulted", defaulted)
                .add("blackList", blackList)
                .toString();
    }
}
