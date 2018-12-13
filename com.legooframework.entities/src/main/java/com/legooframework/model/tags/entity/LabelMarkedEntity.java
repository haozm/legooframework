package com.legooframework.model.tags.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.customer.entity.CustomerId;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class LabelMarkedEntity extends BaseEntity<Long> {

    private CustomerId customerId;
    private Set<LabelInfo> labelInfos;
    private Long labelId;

    LabelMarkedEntity(Long id, CustomerId customerId, LabelNodeEntity label) {
        super(id);
        this.customerId = customerId;
        this.labelId = label.getId();
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> data = super.toParamMap("customerId", "labelId", "labelInfos");
        data.putAll(customerId.toParamMap());
        data.put("labelId", labelId);
        return data;
    }

    LabelMarkedEntity(ResultSet res) {
        super(0L, res);
        try {
            this.customerId = new CustomerId(res);
            this.labelInfos = Sets.newHashSet();
            String _labelInfos = ResultSetUtil.getString(res, "labelInfos");
            String[] args = StringUtils.split(_labelInfos, ',');
            Stream.of(args).forEach(s -> this.labelInfos.add(new LabelInfo(s)));
        } catch (SQLException e) {
            throw new RuntimeException("Restore LabelMarkedEntity has SQLException", e);
        }
    }

    public Optional<LabelMarkedEntity> addLable(Long recId, LabelNodeEntity labelNode) {
        if (containLabel(labelNode)) return Optional.empty();
        return Optional.of(new LabelMarkedEntity(recId, this.customerId, labelNode));
    }

    public boolean containLabel(LabelNodeEntity labelNode) {
        Preconditions.checkNotNull(labelNode);
        Optional<LabelInfo> exits = this.labelInfos.stream().filter(x -> x.getLabelId()
                .equals(labelNode.getId())).findFirst();
        return exits.isPresent();
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Set<LabelInfo> getLabelInfos() {
        return ImmutableSet.copyOf(labelInfos);
    }

    class LabelInfo {
        private Long recId;
        private Long labelId;

        LabelInfo(String all) {
            String[] args = StringUtils.split(all, ':');
            this.recId = Long.valueOf(args[0]);
            this.labelId = Long.valueOf(args[1]);
        }

        LabelInfo(Long recId, Long labelId) {
            this.recId = recId;
            this.labelId = labelId;
        }

        public Long getRecId() {
            return recId;
        }

        public Long getLabelId() {
            return labelId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LabelInfo labelInfo = (LabelInfo) o;
            return Objects.equal(recId, labelInfo.recId) &&
                    Objects.equal(labelId, labelInfo.labelId);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(recId, labelId);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("recId", recId)
                    .add("labelId", labelId)
                    .toString();
        }
    }
}
