package com.legooframework.model.core.base.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

// 实体类抽象建模
public abstract class BaseEntity<T extends Serializable> implements Cloneable {

    private final T id;
    private final Long tenantId;
    // 创建者与创建时间
    private final Long creator;
    private final DateTime createTime;
    // 最后一次修改者与修改时间
    private Long editor;
    private DateTime editTime;

    protected BaseEntity(T id) {
        this.id = id;
        this.tenantId = null;
        this.creator = null;
        this.createTime = DateTime.now();
    }

    protected BaseEntity(T id, DateTime createTime) {
        Preconditions.checkNotNull(id, "实体唯一标识ID不可以为空值.");
        this.id = id;
        this.creator = -1L;
        this.tenantId = -1L;
        this.createTime = createTime;
    }

    protected BaseEntity(T id, Long tenantId, Long creator) {
        Preconditions.checkNotNull(id, "实体唯一标识ID不可以为空值.");
        Preconditions.checkNotNull(creator, "实体的造物主不可以为空.");
        Preconditions.checkNotNull(tenantId, "租户ID不可以为空.");
        this.id = id;
        this.creator = creator;
        this.tenantId = tenantId;
        this.createTime = DateTime.now();
    }

    protected BaseEntity(T id, Long tenantId, Long creator, DateTime createTime, Long editor, DateTime editTime) {
        this.id = id;
        this.tenantId = tenantId;
        this.creator = creator;
        this.createTime = createTime;
        this.editor = editor;
        this.editTime = editTime;
    }

    protected BaseEntity(T id, ResultSet res) {
        this.id = id;
        try {
            this.tenantId = ResultSetUtil.getOptObject(res, "tenantId", Long.class).orElse(null);
            this.creator = ResultSetUtil.getOptObject(res, "creator", Long.class).orElse(null);
            this.createTime = ResultSetUtil.getDateTime(res, "createTime");
            this.editor = ResultSetUtil.getOptObject(res, "editor", Long.class).orElse(null);
            this.editTime = ResultSetUtil.getDateTime(res, "editTime") == null ? this.createTime :
                    ResultSetUtil.getDateTime(res, "editTime");
        } catch (SQLException e) {
            throw new RuntimeException("Restore BaseEntity has SQLException", e);
        }
    }

    protected BaseEntity(T id, ResultSet res, Long creator) {
        this.id = id;
        try {
            this.tenantId = ResultSetUtil.getOptObject(res, "tenantId", Long.class).orElse(null);
            this.creator = creator;
            this.createTime = ResultSetUtil.getDateTime(res, "createTime");
            this.editor = ResultSetUtil.getOptObject(res, "editor", Long.class).orElse(null);
            this.editTime = ResultSetUtil.getDateTime(res, "editTime") == null ? this.createTime :
                    ResultSetUtil.getDateTime(res, "editTime");
        } catch (SQLException e) {
            throw new RuntimeException("Restore BaseEntity has SQLException", e);
        }
    }

    public T getId() {
        return id;
    }

    public Long getCreator() {
        return creator;
    }

    protected void setEditTime(DateTime editTime) {
        this.editTime = editTime;
    }

    public DateTime getCreateTime() {
        return createTime;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public <E extends BaseEntity> boolean isSameTenant(E entity) {
        if (null == entity) return false;
        return Objects.equal(this.getTenantId(), entity.getTenantId());
    }

    public boolean isSameTenant(LoginContext lc) {
        if (null == lc) return false;
        return Objects.equal(this.getTenantId(), lc.getTenantId());
    }

    public Optional<Long> getEditor() {
        return Optional.ofNullable(editor);
    }

    public Optional<DateTime> getEditTime() {
        return Optional.ofNullable(editTime);
    }

    // 转换为Map 参数类型
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(this);
        try {
            for (PropertyDescriptor $it : descriptors) {
                if (ArrayUtils.isNotEmpty(excludes) && ArrayUtils.contains(excludes, $it.getName())) continue;
                if (PropertyUtils.isReadable(this, $it.getName())) {
                    Object value = PropertyUtils.getSimpleProperty(this, $it.getName());
                    if (value instanceof Optional) {
                        params.put($it.getName(), ((Optional<?>) value).orElse(null));
                    } else if (value instanceof DateTime) {
                        params.put($it.getName(), ((DateTime) value).toDate());
                    } else if (value instanceof Boolean) {
                        params.put($it.getName(), ((Boolean) value) ? 1 : 0);
                    } else {
                        params.put($it.getName(), value);
                    }
                }
            }
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
        return params;
    }

    public Map<String, Object> toViewMap() {
        return null;
    }

    protected Object cloneMe() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public final void setEditor(Long editor) {
        this.editor = editor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return Objects.equal(id, that.id);
    }

    // 业务实体判断是否一致
    protected boolean equalsEntity(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> baseEnity = (BaseEntity<?>) o;
        return Objects.equal(tenantId, baseEnity.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("tenantId", tenantId)
                .add("creator", creator)
                .add("createTime", createTime)
                .add("editor", editor)
                .add("editTime", editTime)
                .toString();
    }

}
