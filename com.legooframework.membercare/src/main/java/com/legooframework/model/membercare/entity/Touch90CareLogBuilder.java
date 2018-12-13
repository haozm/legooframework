package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Optional;

public class Touch90CareLogBuilder {

    private final CrmOrganizationEntity company;
    private final CrmStoreEntity store;
    private List<Detail> delegate;

    public Touch90CareLogBuilder(CrmOrganizationEntity company, CrmStoreEntity store, List<SaleRecordEntity> saleRecords) {
        this.company = company;
        this.store = store;
        this.delegate = Lists.newArrayList();
        saleRecords.forEach($cur -> {
            LocalDate _date = $cur.getModifyDate().toLocalDate();
            Optional<Detail> _detail = this.delegate.stream().filter(x -> x.isSameDay(_date)).findFirst();
            if (_detail.isPresent()) {
                _detail.get().addSaleRecordEntity($cur);
            } else {
                Detail new_detail = new Detail(_date);
                new_detail.addSaleRecordEntity($cur);
                this.delegate.add(new_detail);
            }
        });
    }

    public List<Touch90CareLogEntity> build() {
        List<Touch90CareLogEntity> logEntities = Lists.newArrayList();
        this.delegate.forEach(x ->
                logEntities.add(new Touch90CareLogEntity(company, store, x.logDate, x.addList, x.uploadList)));
        return logEntities;
    }

    class Detail {
        private final LocalDate logDate;
        private List<Integer> addList;
        private List<Integer> uploadList;

        Detail(LocalDate logDate) {
            this.logDate = logDate;
            this.addList = Lists.newArrayList();
            this.uploadList = Lists.newArrayList();
        }

        boolean isSameDay(LocalDate date) {
            return this.logDate.equals(date);
        }

        private void addSaleRecordEntity(SaleRecordEntity saleRecord) {
            if (saleRecord.isModified()) {
                uploadList.add(saleRecord.getId());
            } else {
                addList.add(saleRecord.getId());
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("company", company.getId())
                .add("store", store.getId())
                .add("delegate", delegate.size())
                .toString();
    }
}
