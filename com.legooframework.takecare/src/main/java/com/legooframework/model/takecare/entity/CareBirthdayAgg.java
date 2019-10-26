package com.legooframework.model.takecare.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.covariant.service.MemberAgg;

import java.util.List;

public class CareBirthdayAgg {

    private final CareBirthdayEntity birthdayCare;
    private final List<CareRecordEntity> takeCareRecords;
    private final List<CareHisRecordEntity> hisCareRecords;
    private final MemberAgg memberAgg;

    CareBirthdayAgg(CareBirthdayEntity birthdayCare, List<CareRecordEntity> takeCareRecords,
                    List<CareHisRecordEntity> hisCareRecords, MemberAgg memberAgg) {
        this.birthdayCare = birthdayCare;
        this.takeCareRecords = takeCareRecords;
        this.hisCareRecords = hisCareRecords;
        this.memberAgg = memberAgg;
    }

    public boolean hasSavedCare() {
        return this.birthdayCare.hasSaved();
    }

    public CareBirthdayEntity getBirthdayCare() {
        return birthdayCare;
    }

    boolean hasCareError() {
        return birthdayCare.hasError();
    }

    public List<CareHisRecordEntity> getHisCareRecords() {
        hisCareRecords.forEach(x -> x.setPlanId(birthdayCare));
        return hisCareRecords;
    }

    public List<CareRecordEntity> getTakeCareRecords() {
        takeCareRecords.forEach(x -> x.setCareId(birthdayCare));
        return takeCareRecords;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("birthdayCare", birthdayCare.getCareId())
                .add("takeCareRecords", takeCareRecords.size())
                .toString();
    }
}
