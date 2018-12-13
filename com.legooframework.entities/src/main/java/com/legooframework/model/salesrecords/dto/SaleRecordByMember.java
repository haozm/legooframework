package com.legooframework.model.salesrecords.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.legooframework.model.crmadapter.entity.CrmMemberEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SaleRecordByMember {

    private static final Logger logger = LoggerFactory.getLogger(SaleRecordByMember.class);
    private final CrmOrganizationEntity company;
    private final LocalDateTime startDay, endDay;
    private final Table<CrmStoreEntity, CrmMemberEntity, List<SaleRecordEntity>> combineSaleRecords;

    public SaleRecordByMember(CrmOrganizationEntity company, LocalDateTime startDay, LocalDateTime endDay,
                              Table<CrmStoreEntity, CrmMemberEntity, List<SaleRecordEntity>> combineSaleRecords) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.company = company;
        if (combineSaleRecords == null) {
            this.combineSaleRecords = null;
        } else {
            this.combineSaleRecords = ImmutableTable.copyOf(combineSaleRecords);
        }
    }

    //    FlatFileItemReader
    public LocalDateTime getStartDay() {
        return startDay;
    }

    public LocalDateTime getEndDay() {
        return endDay;
    }

    public CrmOrganizationEntity getCompany() {
        return company;
    }

    public boolean hasDetail() {
        return null != combineSaleRecords;
    }

    public Optional<Map<CrmMemberEntity, List<SaleRecordEntity>>> getMemberSaleRecords(CrmStoreEntity store) {
        Preconditions.checkState(hasDetail());
        Map<CrmMemberEntity, List<SaleRecordEntity>> maps = combineSaleRecords.row(store);
        return MapUtils.isEmpty(maps) ? Optional.empty() : Optional.of(maps);
    }

    public List<Table<CrmStoreEntity, CrmMemberEntity, List<SaleRecordEntity>>> partition(int size) {
        int _size = size > 1 ? size : 20;
        List<CrmStoreEntity> _list = Lists.newArrayList(combineSaleRecords.rowKeySet());
        List<List<CrmStoreEntity>> sub_stores = Lists.partition(_list, _size);
        List<Table<CrmStoreEntity, CrmMemberEntity, List<SaleRecordEntity>>> res_list = Lists.newArrayList();
        for (List<CrmStoreEntity> sts : sub_stores) {
            Table<CrmStoreEntity, CrmMemberEntity, List<SaleRecordEntity>> _table = HashBasedTable.create();
            sts.forEach(x -> {
                Map<CrmMemberEntity, List<SaleRecordEntity>> _map = combineSaleRecords.row(x);
                _map.forEach((key, value) -> _table.put(x, key, value));
            });
            res_list.add(_table);
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("共计门店 %s 家，每组 %s 个，分拆为 %s 组...", _list.size(), _size, res_list.size()));
        return res_list;
    }


    public Set<CrmStoreEntity> rowKeySet() {
        Preconditions.checkState(hasDetail());
        return combineSaleRecords.rowKeySet();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("company", company.getId())
                .add("startDay", startDay)
                .add("endDay", endDay)
                .add("combineSaleRecords", combineSaleRecords == null ? 0 : combineSaleRecords.size())
                .toString();
    }
}
