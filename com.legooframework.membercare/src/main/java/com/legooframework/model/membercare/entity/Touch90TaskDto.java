package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.util.List;

public class Touch90TaskDto {

    private final Integer companyId;
    private final List<UpcomingTaskEntity> updates;
    private final List<UpcomingTaskEntity> inserts;
    private final List<UpcomingTaskDetailEntity> updateDetail;

    Touch90TaskDto(Integer companyId) {
        this.updates = Lists.newArrayList();
        this.inserts = Lists.newArrayList();
        this.updateDetail = Lists.newArrayList();
        this.companyId = companyId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setUpdates(UpcomingTaskEntity update) {
        this.updates.add(update);
    }

    public void setInserts(UpcomingTaskEntity insert) {
        this.inserts.add(insert);
    }

    public void setUpdateDetail(List<UpcomingTaskDetailEntity> updateDetail) {
        this.updateDetail.addAll(updateDetail);
    }

    public List<UpcomingTaskEntity> getUpdates() {
        return updates;
    }

    public List<UpcomingTaskEntity> getInserts() {
        return inserts;
    }

    public List<UpcomingTaskDetailEntity> getUpdateDetail() {
        return updateDetail;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("updates", updates.size())
                .add("inserts", inserts.size())
                .add("updateDetail", updateDetail.size())
                .toString();
    }
}
