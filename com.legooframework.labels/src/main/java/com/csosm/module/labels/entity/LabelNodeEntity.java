package com.csosm.module.labels.entity;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.*;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class LabelNodeEntity extends BaseEntity<Long> {

    private final int type;
    private String name, labelCtx;
    private String desc;
    private Long pId;
    private Integer companyId, storeId;
    private Double value;
    private DateTime dateTime;
    private Range<Double> numRange;
    private Range<Date> dateRange;
    private boolean enabled;
    private Set<Long> childIds;

    public Optional<LabelNodeEntity> changeName(String name, String desc) {
        Preconditions.checkState(isEnabled(), "当前标签未激活，无法修改名称");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "名称不可以为空值...");
        if (StringUtils.equals(this.name, name) && StringUtils.equals(this.desc, desc)) return Optional.absent();
        try {
            LabelNodeEntity clone = (LabelNodeEntity) this.clone();
            clone.name = name;
            clone.desc = desc;
            return Optional.of(clone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<LabelNodeEntity> enabled() {
        if (isEnabled()) return Optional.absent();
        try {
            LabelNodeEntity clone = (LabelNodeEntity) this.clone();
            clone.enabled = true;
            return Optional.of(clone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TxtLabelDto getTxtLabelDto() {
        return new TxtLabelDto(this.getId(), this.pId, this.name, this.desc);
    }

    public Optional<LabelNodeEntity> disabled() {
        if (!isEnabled()) return Optional.absent();
        try {
            LabelNodeEntity clone = (LabelNodeEntity) this.clone();
            clone.enabled = false;
            return Optional.of(clone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LabelNodeEntity(Long id, int type, String name, String desc, Long pId,
                            OrganizationEntity company, StoreEntity store,
                            Double minvalue, Double maxValue,
                            DateTime minDate, DateTime maxDate) {
        super(id);
        this.type = type;
        this.desc = desc;
        Preconditions.checkNotNull(pId, "上级ID不可以为空值");
        this.pId = pId;
        this.enabled = true;
        if (null != store) {
            Preconditions.checkArgument(store.getCompanyId().isPresent(), "门店缺少公司信息....");
            this.companyId = store.getCompanyId().get();
            this.storeId = store.getId();
        } else {
            Preconditions.checkNotNull(company, "所属公司信息不可以为空值....");
            this.companyId = company.getId();
        }
        this.name = name;
        // 标签类型  1：文本  2 单值数字  3 数字区间 4 单值日期  5 日期区间
        if (this.type == 1) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "标签说明不可以为空...");
        } else if (2 == type) {
            Preconditions.checkNotNull(minvalue, "数字字段不可以为空值...");
            this.value = minvalue;
        } else if (3 == type) {
            Preconditions.checkNotNull(minvalue, "区间 min 不可以为空值...");
            Preconditions.checkNotNull(maxValue, "区间 max 不可以为空值...");
            Preconditions.checkState(minvalue <= maxValue, "区间 [%s,%s] 大小逆序", minvalue, maxValue);
            this.numRange = Range.closed(minvalue, maxValue);
        } else if (4 == type) {
            Preconditions.checkNotNull(minDate, "日期字段不可以为空值...");
            this.dateTime = minDate;
        } else if (5 == type) {
            Preconditions.checkNotNull(minDate, "日期区间 min 不可以为空值...");
            Preconditions.checkNotNull(maxDate, "日期区间 max 不可以为空值...");
            Preconditions.checkState(minDate.isBefore(maxDate), "区间 [%s,%s] 大小逆序", minvalue, maxValue);
            this.dateRange = Range.closed(minDate.toDate(), maxDate.toDate());
        }
        this.childIds = null;
    }

    LabelNodeEntity(ResultSet res, Map<String, String> ctx) throws Exception {
        super(res.getLong("id"));
        this.pId = res.getLong("pId");
        this.type = res.getInt("labelType");
        this.name = res.getString("labelName");
        this.desc = res.getString("labelDesc");
        this.companyId = res.getInt("companyId");
        this.labelCtx = res.getString("labelCtx");
        this.enabled = res.getInt("labelEnbale") == 1;
        this.storeId = res.getInt("storeId") == -1 ? null : res.getInt("storeId");
        if (this.type != 1)
            Preconditions.checkState(MapUtils.isNotEmpty(ctx), "标签实际内容为空，数据异常...");
// 标签类型  1：文本  2 单值数字  3 数字区间 4 单值日期  5 日期区间
        if (2 == type) {
            this.value = MapUtils.getDoubleValue(ctx, "value");
        } else if (3 == type) {
            this.numRange = Range.closed(MapUtils.getDoubleValue(ctx, "min"), MapUtils.getDoubleValue(ctx, "max"));
        } else if (4 == type) {
            this.dateTime = DateTime.parse(MapUtils.getString(ctx, "value"));
        } else if (5 == type) {
            DateTime min = DateTime.parse(MapUtils.getString(ctx, "min"));
            DateTime max = DateTime.parse(MapUtils.getString(ctx, "max"));
            this.dateRange = Range.closed(min.toDate(), max.toDate());
        }
        String childIds = res.getString("childIds");
        if (Strings.isNullOrEmpty(childIds)) {
            this.childIds = null;
        } else {
            String[] cids = StringUtils.split(childIds, ',');
            this.childIds = Sets.newHashSet();
            for (String $it : cids) this.childIds.add(Long.valueOf($it));
        }
    }

    static LabelNodeEntity txtLabel(Long id, LabelNodeEntity parent, OrganizationEntity company, StoreEntity store,
                                    String name, String desc) {
        return new LabelNodeEntity(id, 1, name, desc, parent.getId(),
                company, store, null, null, null, null);
    }

    static LabelNodeEntity txtRootLabel(OrganizationEntity company) {
        return new LabelNodeEntity(100L, 1, "系统标签", "公司系统标签", 100L, company, null, null, null, null, null);
    }

    static LabelNodeEntity txtLabel(Long id, Long parent, OrganizationEntity company, StoreEntity store,
                                    String name, String desc) {
        return new LabelNodeEntity(id, 1, name, desc, parent,
                company, store, null, null, null, null);
    }

    public boolean isEnabled() {
        return enabled;
    }

    static LabelNodeEntity numberLabel(Long id, LabelNodeEntity parent, OrganizationEntity company, StoreEntity store,
                                       String name, Double value, String desc) {
        return new LabelNodeEntity(id, 2, name, desc, parent.getId(),
                company, store, value, null, null, null);
    }

    // 标签类型  1：文本  2 单值数字  3 数字区间 4 单值日期  5 日期区间
    static LabelNodeEntity numRangeLabel(Long id, LabelNodeEntity parent, OrganizationEntity company, StoreEntity store,
                                         String name, Double min, Double max, String desc) {
        return new LabelNodeEntity(id, 3, name, desc, parent.getId(),
                company, store, min, max, null, null);
    }

    static LabelNodeEntity dateTimeLabel(Long id, LabelNodeEntity parent, OrganizationEntity company, StoreEntity store,
                                         String name, DateTime dateTime, String desc) {
        return new LabelNodeEntity(id, 4, name, desc, parent.getId(),
                company, store, null, null, dateTime, null);
    }

    static LabelNodeEntity dateTimeRangeLabel(Long id, LabelNodeEntity parent, OrganizationEntity company, StoreEntity store,
                                              String name, DateTime min, DateTime max, String desc) {
        return new LabelNodeEntity(id, 5, name, desc, parent.getId(),
                company, store, null, null, min, max);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("id", getId());
        data.put("labelType", type);
        data.put("labelName", name);
        data.put("labelDesc", desc);
        data.put("pId", pId);
        data.put("labelEnbale", this.enabled ? 1 : 0);
        data.put("companyId", companyId);
        data.put("storeId", storeId == null ? -1 : storeId);
        data.put("labelCtx", null);
        if (isNumberLabel()) {
            data.put("labelCtx", String.format("value=%s", this.value));
        } else if (isDateTimeLabel()) {
            data.put("labelCtx", String.format("value=%s", this.dateTime.toString("yyyyMMddHHmmss")));
        } else if (isNumRangeLabel()) {
            data.put("labelCtx", String.format("min=%s,max=%s", this.numRange.lowerEndpoint(),
                    this.numRange.upperEndpoint()));
        } else if (isDateRangeLabel()) {
            data.put("labelCtx", String.format("min=%s,max=%s",
                    DateFormatUtils.format(this.dateRange.lowerEndpoint(), "yyyyMMddHHmmss"),
                    DateFormatUtils.format(this.dateRange.upperEndpoint(), "yyyyMMddHHmmss")));
        }
        return data;
    }

    public boolean isTxtLabel() {
        return 1 == this.type;
    }

    public boolean hasChild() {
        return CollectionUtils.isNotEmpty(this.childIds);
    }

    public Long getNextChildId() {
        if (CollectionUtils.isEmpty(this.childIds)) return this.getId() * 1000 + 100;
        for (long i = 100L; i < 1000L; i++) {
            long next_val = this.getId() * 1000 + i;
            if (!this.childIds.contains(next_val)) return next_val;
        }
        throw new RuntimeException("节点 %s 下级节点耗尽999 个节点ID使用...");
    }

    public boolean isNumberLabel() {
        return 2 == this.type;
    }

    public boolean isNumRangeLabel() {
        return 3 == this.type;
    }

    public boolean isDateRangeLabel() {
        return 5 == this.type;
    }

    public boolean isDateTimeLabel() {
        return 4 == this.type;
    }

    public boolean isRoot() {
        return Objects.equal(this.getId(), this.pId);
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Long getpId() {
        return pId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Double getValue() {
        return value;
    }

    public Range<Double> getNumRange() {
        return numRange;
    }

    public Range<Date> getDateRange() {
        return dateRange;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public boolean hasStore() {
        return null != this.storeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LabelNodeEntity that = (LabelNodeEntity) o;
        return type == that.type &&
                Objects.equal(name, that.name) &&
                Objects.equal(desc, that.desc) &&
                Objects.equal(pId, that.pId) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(labelCtx, that.labelCtx);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), type, name, desc, pId, companyId, storeId, labelCtx);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("type", type)
                .add("name", name)
                .add("desc", desc)
                .add("pId", pId)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("labelCtx", value)
                .omitNullValues()
                .toString();
    }
}
