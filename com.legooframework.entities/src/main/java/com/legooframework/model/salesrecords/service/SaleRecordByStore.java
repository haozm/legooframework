package com.legooframework.model.salesrecords.service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.legooframework.model.crmadapter.entity.CrmMemberEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.salesrecords.dto.SaleRecordByMember;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SaleRecordByStore {

    private final LocalDateTime startDay, endDay;
    private final CrmStoreEntity store;
    private final String categories;
    private final ArrayListMultimap<CrmMemberEntity, SaleRecordEntity> multimap;

    SaleRecordByStore(CrmStoreEntity store, String categories, LocalDateTime startDay,
                      LocalDateTime endDay, Map<CrmMemberEntity, Collection<SaleRecordEntity>> datas) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.store = store;
        this.categories = categories;
        this.multimap = ArrayListMultimap.create();
        if (MapUtils.isNotEmpty(datas))
            datas.forEach(this.multimap::putAll);
    }

    public CrmStoreEntity getStore() {
        return store;
    }

    public int size() {
        return multimap.size();
    }

    public Collection<CrmMemberEntity> getMember() {
        return multimap.keySet();
    }

    public List<SaleRecordEntity> getSaleRecords(CrmMemberEntity crmMember) {
        return multimap.get(crmMember);
    }

    public String getCategories() {
        return categories;
    }

    public Collection<SaleRecordEntity> loadAllSaleRecords() {
        return multimap.values();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("company", store.getCompanyId())
                .add("store", store.getId())
                .add("categories", categories)
                .add("startDay", startDay)
                .add("endDay", endDay)
                .add("multimap", multimap.size())
                .toString();
    }
}
