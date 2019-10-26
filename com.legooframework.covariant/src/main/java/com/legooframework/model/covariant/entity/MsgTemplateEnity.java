package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class MsgTemplateEnity extends BaseEntity<Integer> {
    /**
     * 模版业务模块：会员关怀
     */
    public final static int TEMPLETTYPE_MEMBER_CARE = 1;
    /**
     * 模版业务类型： 90服务
     */
    public final static int USETYPE_NINETYPLAN = 1;
    /**
     * 模版业务类型： 返单计划
     */
    public final static int USETYPE_REORDERPLAN = 2;
    /**
     * 模版业务类型： 感动计划
     */
    public final static int USETYPE_TOUCHPLAN = 3;

    /**
     * 模版业务类型： 生日关怀
     */
    public final static int USETYPE_BIRTHDAYCARE = 4;
    /**
     * 模版业务类型： 节日关怀
     */
    public final static int USETYPE_HOLIDAYCARE = 5;

    // 模板名称、摘要
    private String name;
    // 模板内容
    private String content;
    // 模板分类 （如：适用计划）
    // 模板分类（子类型）（计划阶段）
    private final int useType, subUseType;

    // 启用/停用：1 - 启用，2 - 禁用
    // templetType 模板种类（目标）（针对不同业务类型的模板）
    private final int templetState, defaultState, useRange, messageTempletType, sortNo, templetType;

    private final Integer storeId, companyId;

    public Optional<String> getContent() {
        return Optional.ofNullable(content);
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    MsgTemplateEnity(Integer id, ResultSet res) {
        super(id);
        try {
            this.companyId = ResultSetUtil.getObject(res, "company_id", Integer.class);
            this.storeId = ResultSetUtil.getOptObject(res, "store_id", Number.class).orElse(0).intValue();
            this.content = ResultSetUtil.getOptString(res, "content", null);
            this.name = ResultSetUtil.getOptString(res, "name", null);
            this.sortNo = ResultSetUtil.getOptObject(res, "sortNo", Number.class).orElse(0).intValue();
            this.useType = ResultSetUtil.getOptObject(res, "useType", Integer.class).orElse(0);
            this.subUseType = ResultSetUtil.getOptObject(res, "subUseType", Number.class).orElse(0).intValue();
            this.templetState = ResultSetUtil.getOptObject(res, "templetState", Number.class).orElse(2).intValue();
            this.templetType = ResultSetUtil.getOptObject(res, "templetType", Number.class).orElse(0).intValue();
            this.useRange = ResultSetUtil.getOptObject(res, "useRange", Number.class).orElse(0).intValue();
            this.defaultState = ResultSetUtil.getOptObject(res, "defaultState", Number.class).orElse(2).intValue();
            this.messageTempletType = ResultSetUtil
                    .getOptObject(res, "messageTempletType", Number.class).orElse(0).intValue();
        } catch (SQLException e) {
            throw new RuntimeException("Restore MsgTemplateEnity has SQLException", e);
        }
    }

    boolean isDefault() {
        return this.defaultState == 1;
    }

    boolean isEnabled() {
        return this.templetState == 1;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("content", content)
                .add("templetType", templetType)
                .add("useType", useType)
                .add("subUseType", subUseType)
                .add("useRange", useRange)
                .add("templetState", templetState)
                .add("sortNo", sortNo)
                .add("defaultState", defaultState)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .add("messageTempletType", messageTempletType)
                .toString();
    }
}
