package com.legooframework.model.tags.entity;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.organization.entity.StoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LabelNodeEntity extends BaseEntity<Long> {

    private final static int TXT_LABEL = 1;
    private final int labelType;
    private String labelName, labelDesc, labelCtx;
    private Long pId;
    private Long storeId;
    private boolean labelEnabled;
    private Set<Long> childIds;

    public Map<String, Object> toViewMap() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("id", getId());
        data.put("labelType", labelType);
        data.put("labelName", labelName);
        data.put("labelDesc", labelDesc);
        data.put("labelCtx", labelCtx);
        data.put("pId", pId);
        data.put("labelEnabled", labelEnabled);
        data.put("storeId", storeId);
        return data;
    }

    private LabelNodeEntity(Long id, int type, String name, String desc, String labelCtx,
                            Long pId, Long storeId, boolean enabled, Set<Long> childIds) {
        super(id);
        this.labelType = type;
        this.labelName = name;
        this.labelDesc = desc;
        this.labelCtx = labelCtx;
        this.pId = pId;
        this.storeId = storeId;
        this.labelEnabled = enabled;
        this.childIds = childIds;
    }

    static LabelNodeEntity txtLabel(Long id, LabelNodeEntity parent, String labelName, String desc) {
        return new LabelNodeEntity(id, TXT_LABEL, labelName, desc, labelName,
                parent.getId(), -1L, true, null);
    }

    static LabelNodeEntity txtLabel(Long id, Long pId, StoreEntity store, String labelName, String desc) {
        return new LabelNodeEntity(id, TXT_LABEL, labelName, desc, labelName,
                pId, store.getId(), true, null);
    }

    LabelNodeEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.labelType = res.getInt("labelType");
            this.labelName = res.getString("labelName");
            if (TXT_LABEL == this.labelType) {
                this.labelCtx = res.getString("labelCtx");
            }
            this.labelDesc = ResultSetUtil.getOptString(res, "labelDesc", null);
            this.pId = res.getLong("pId");
            this.labelEnabled = res.getInt("labelEnable") == 1;
            this.storeId = ResultSetUtil.getObject(res, "storeId", Long.class);
            String childIds = res.getString("childIds");
            if (Strings.isNullOrEmpty(childIds)) {
                this.childIds = null;
            } else {
                this.childIds = Stream.of(StringUtils.split(childIds, ','))
                        .map(Long::valueOf).collect(Collectors.toSet());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore LabelNodeEntity has SQLException", e);
        }
    }

    public String getLabelName() {
        return labelName;
    }

    public String getLabelDesc() {
        return labelDesc;
    }

    public String getLabelCtx() {
        return labelCtx;
    }

    public Long getpId() {
        return pId;
    }

    public TxtLabelDto toTxtLabelDto() {
        return new TxtLabelDto(this.getId(), this.pId, this.labelName, this.labelDesc);
    }


    Long getNextChildId() {
        if (CollectionUtils.isEmpty(this.childIds)) return this.getId() * 1000 + 100;
        for (long i = 100L; i < 1000L; i++) {
            long next_val = this.getId() * 1000 + i;
            if (!this.childIds.contains(next_val)) return next_val;
        }
        throw new RuntimeException("节点 %s 下级节点耗尽999 个节点ID使用...");
    }

    Optional<LabelNodeEntity> enabled() {
        if (isLabelEnabled()) return Optional.empty();
        try {
            LabelNodeEntity clone = (LabelNodeEntity) this.clone();
            clone.labelEnabled = true;
            return Optional.of(clone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Optional<LabelNodeEntity> disabled() {
        if (!isLabelEnabled()) return Optional.empty();
        try {
            LabelNodeEntity clone = (LabelNodeEntity) this.clone();
            clone.labelEnabled = false;
            return Optional.of(clone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Optional<LabelNodeEntity> changeName(String labelName, String labelDesc) {
        Preconditions.checkState(isLabelEnabled(), "当前标签未激活，无法修改名称");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(labelName), "名称不可以为空值...");
        if (StringUtils.equals(this.labelName, labelName) && StringUtils.equals(this.labelDesc, labelDesc))
            return Optional.empty();
        try {
            LabelNodeEntity clone = (LabelNodeEntity) this.clone();
            clone.labelName = labelName;
            clone.labelDesc = labelDesc;
            return Optional.of(clone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> map = super.toParamMap("storeId", "labelEnabled", "labelType", "childIds");
        map.put("labelType", labelType);
        map.put("labelEnabled", labelEnabled);
        map.put("storeId", storeId == null ? -1L : storeId);
        return map;
    }

    public Long getStoreId() {
        return storeId;
    }

    public boolean isLabelEnabled() {
        return labelEnabled;
    }

    public Set<Long> getChildIds() {
        return childIds;
    }

    public boolean isStoreLabel() {
        return -1L != this.storeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LabelNodeEntity that = (LabelNodeEntity) o;
        return labelType == that.labelType &&
                labelEnabled == that.labelEnabled &&
                Objects.equal(labelName, that.labelName) &&
                Objects.equal(labelDesc, that.labelDesc) &&
                Objects.equal(labelCtx, that.labelCtx) &&
                Objects.equal(pId, that.pId) &&
                Objects.equal(storeId, that.storeId) &&
                SetUtils.isEqualSet(childIds, that.childIds);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), labelType, labelName, labelDesc, labelCtx, pId, storeId, labelEnabled, childIds);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("pId", pId)
                .add("labelType", labelType)
                .add("labelName", labelName)
                .add("labelDesc", labelDesc)
                .add("labelCtx", labelCtx)
                .add("storeId", storeId)
                .add("labelEnabled", labelEnabled)
                .add("childIds", childIds)
                .toString();
    }
}
