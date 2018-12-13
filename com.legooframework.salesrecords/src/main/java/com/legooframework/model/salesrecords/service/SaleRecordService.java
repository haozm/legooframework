package com.legooframework.model.salesrecords.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.legooframework.model.crmadapter.entity.CrmMemberEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDateTime;

import java.util.*;
import java.util.stream.Collectors;

public class SaleRecordService extends AbstractSaleRecordService {

    /**
     * 获取指定公司  门店 指定时间段 内的销售记录
     *
     * @param companyId
     * @param storeIds
     * @param startDay
     * @param endDay
     * @param sample
     * @return
     */
    public Optional<List<SaleRecordByStore>> loadSaleRecordByStore(Integer companyId, Collection<Integer> storeIds,
                                                                   Date startDay, Date endDay, boolean sample) {
        Preconditions.checkNotNull(companyId);
        LocalDateTime start = LocalDateTime.fromDateFields(startDay);
        LocalDateTime end = LocalDateTime.fromDateFields(endDay);
        Optional<CrmOrganizationEntity> company = getCompanyAct().findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "ID=%s 对应的公司不存在...", companyId);
        Optional<List<CrmStoreEntity>> storesOpts = getStoreAct().loadAllByCompany(company.get());
        if (!storesOpts.isPresent()) return Optional.empty();

        final List<CrmStoreEntity> stores = CollectionUtils.isNotEmpty(storeIds) ? storesOpts.get().stream()
                .filter(x -> storeIds.contains(x.getId()))
                .collect(Collectors.toList()) : storesOpts.get();
        Optional<List<SaleRecordEntity>> saleRecords = getSaleRecordAction().loadByDateInterval(stores, start, end, sample);

        if (!saleRecords.isPresent()) return Optional.empty();
        Optional<List<CrmMemberEntity>> members = getMemberAct().loadByCompany(company.get(), saleRecords.get().stream()
                .map(SaleRecordEntity::getMemberId).collect(Collectors.toSet()));
        if (!members.isPresent()) return Optional.empty();

        Table<CrmStoreEntity, CrmMemberEntity, List<SaleRecordEntity>> _table = HashBasedTable.create();
        Optional<CrmStoreEntity> _store;
        Optional<CrmMemberEntity> _member;
        for (SaleRecordEntity $it : saleRecords.get()) {
            _store = stores.stream().filter(y -> y.getId().equals($it.getStoreId())).findFirst();
            if (!_store.isPresent()) continue;
            _member = members.get().stream().filter(x -> x.getId().equals($it.getMemberId())).findFirst();
            if (!_member.isPresent()) continue;
            if (_table.contains(_store.get(), _member.get())) {
                _table.get(_store.get(), _member.get()).add($it);
            } else {
                List<SaleRecordEntity> _list = Lists.newArrayList();
                _list.add($it);
                _table.put(_store.get(), _member.get(), _list);
            }
        }
        Set<CrmStoreEntity> storeEntities = _table.rowKeySet();
        List<SaleRecordByStore> saleRecordByStore = Lists.newArrayList();
        storeEntities.forEach(st -> saleRecordByStore.add(new SaleRecordByStore(company.get(), st, start, end, _table.row(st))));
        return Optional.of(saleRecordByStore);
    }


}
