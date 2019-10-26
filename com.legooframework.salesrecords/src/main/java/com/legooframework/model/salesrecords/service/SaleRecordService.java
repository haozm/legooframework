package com.legooframework.model.salesrecords.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.legooframework.model.covariant.entity.MemberEntity;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDateTime;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SaleRecordService extends BundleService {

    /**
     * 获取指定公司  门店 指定时间段 内的销售记录
     *
     * @param companyId 我是
     * @param storeId   一科
     * @param startDay  来自雅苑
     * @param endDay    未来的
     * @param sample    行人
     * @return 归途
     */
    public Optional<SaleRecordByStore> loadSaleRecordByStore(Integer companyId, Integer storeId, String categories,
                                                             Date startDay, Date endDay, boolean sample) {
        Preconditions.checkNotNull(companyId);
        LocalDateTime start = LocalDateTime.fromDateFields(startDay);
        LocalDateTime end = LocalDateTime.fromDateFields(endDay);
        final Optional<OrgEntity> company = getCompanyAct().findById(companyId);
        if (!company.isPresent()) return Optional.empty();
        Optional<StoEntity> store = getStoreAct().findById(storeId);
        if (!store.isPresent()) return Optional.empty();

        Optional<List<SaleRecordEntity>> saleRecords = getSaleRecordAction()
                .loadByDateInterval(store.get(), categories, start, end, sample);
        if (!saleRecords.isPresent()) return Optional.empty();
        List<Integer> memberIds = saleRecords.get().stream().map(SaleRecordEntity::getMemberId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(memberIds)) return Optional.empty();
        Optional<List<MemberEntity>> members = getMemberAct().findByIds(memberIds);
        if (!members.isPresent()) return Optional.empty();

        ArrayListMultimap<MemberEntity, SaleRecordEntity> arrayListMultimap = ArrayListMultimap.create();
        Optional<MemberEntity> _member;
        for (SaleRecordEntity $it : saleRecords.get()) {
            _member = members.get().stream().filter(x -> x.getId().equals($it.getMemberId())).findFirst();
            _member.ifPresent(m -> arrayListMultimap.put(m, $it));
        }
        SaleRecordByStore saleRecordByStore = new SaleRecordByStore(store.get(), categories, start, end,
                arrayListMultimap.asMap());
        return Optional.of(saleRecordByStore);
    }

}
