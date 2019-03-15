package com.legooframework.model.rfm.entity;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Date;

public class MemberRFMEntity extends BaseEntity<Integer> {

    private final int recencyStore, frequencyStore, monetaryStore;
    private final int recencyCom, frequencyCom, monetaryCom;
    private final Integer companyId, memberId;

    MemberRFMEntity(Integer id, Integer companyId, Integer memberId,
                    int recencyStore, int frequencyStore, int monetaryStore, int recencyCom,
                    int frequencyCom, int monetaryCom,
                    Date createTime) {
        super(id, -1, createTime);
        this.recencyStore = recencyStore;
        this.frequencyStore = frequencyStore;
        this.monetaryStore = monetaryStore;
        this.recencyCom = recencyCom;
        this.frequencyCom = frequencyCom;
        this.monetaryCom = monetaryCom;
        this.companyId = companyId;
        this.memberId = memberId;
    }

    public int getRecencyStore() {
        return recencyStore;
    }

    public int getFrequencyStore() {
        return frequencyStore;
    }

    public int getMonetaryStore() {
        return monetaryStore;
    }

    public int getRecencyCom() {
        return recencyCom;
    }

    public int getFrequencyCom() {
        return frequencyCom;
    }

    public int getMonetaryCom() {
        return monetaryCom;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberRFMEntity)) return false;
        MemberRFMEntity that = (MemberRFMEntity) o;
        return recencyStore == that.recencyStore &&
                frequencyStore == that.frequencyStore &&
                monetaryStore == that.monetaryStore &&
                recencyCom == that.recencyCom &&
                frequencyCom == that.frequencyCom &&
                monetaryCom == that.monetaryCom &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(recencyStore, frequencyStore, monetaryStore, recencyCom,
                frequencyCom, monetaryCom, companyId, memberId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("recencyStore", recencyStore)
                .add("frequencyStore", frequencyStore)
                .add("monetaryStore", monetaryStore)
                .add("recencyCom", recencyCom)
                .add("frequencyCom", frequencyCom)
                .add("monetaryCom", monetaryCom)
                .add("companyId", companyId)
                .add("memberId", memberId)
                .add("createTime", getCreateTime())
                .toString();
    }
}
