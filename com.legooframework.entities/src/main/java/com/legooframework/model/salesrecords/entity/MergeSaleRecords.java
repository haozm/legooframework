package com.legooframework.model.salesrecords.entity;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDateTime;

import java.util.List;

public class MergeSaleRecords {

    private final LocalDateTime startDay, endDay;
    private final Integer companyId;
    private final Table<Integer, Integer, List<SaleRecordEntity>> combineSaleRecords;

    MergeSaleRecords(Integer companyId, LocalDateTime startDay, LocalDateTime endDay, List<SaleRecordEntity> saleRecords) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.companyId = companyId;
        if (CollectionUtils.isEmpty(saleRecords)) {
            this.combineSaleRecords = null;
        } else {
            Table<Integer, Integer, List<SaleRecordEntity>> _temp = HashBasedTable.create();
            saleRecords.forEach(x -> {
                if (_temp.contains(x.getStoreId(), x.getMemberId())) {
                    _temp.get(x.getStoreId(), x.getMemberId()).add(x);
                } else {
                    List<SaleRecordEntity> list = Lists.newArrayList();
                    list.add(x);
                    _temp.put(x.getStoreId(), x.getMemberId(), list);
                }
            });
            this.combineSaleRecords = ImmutableTable.copyOf(_temp);
        }
    }

    public LocalDateTime getStartDay() {
        return startDay;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public LocalDateTime getEndDay() {
        return endDay;
    }

    public boolean hasDetail() {
        return combineSaleRecords != null;
    }


}
