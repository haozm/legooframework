package com.legooframework.model.salesrecords.service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.legooframework.model.covariant.entity.MemberEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.LocalDateTime;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SaleRecordByStoreAgg {

    private final LocalDateTime startDay, endDay;
    private final StoEntity store;
    private final String categories;
    private final ArrayListMultimap<MemberEntity, SaleRecordEntity> multimap;

    SaleRecordByStoreAgg(StoEntity store, String categories, LocalDateTime startDay,
                         LocalDateTime endDay, Map<MemberEntity, Collection<SaleRecordEntity>> datas) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.store = store;
        this.categories = categories;
        this.multimap = ArrayListMultimap.create();
        if (MapUtils.isNotEmpty(datas))
            datas.forEach(this.multimap::putAll);
    }

    public StoEntity getStore() {
        return store;
    }

    public int size() {
        return multimap.size();
    }

    public Collection<MemberEntity> getMember() {
        return multimap.keySet();
    }

    public List<SaleRecordEntity> getSaleRecords(MemberEntity crmMember) {
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
