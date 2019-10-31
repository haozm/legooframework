package com.legooframework.model.salesrecords.service;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.covariant.entity.MemberEntity;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.salesrecords.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SaleRecordService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SaleRecordService.class);

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
    public Optional<SaleRecordByStoreAgg> loadSaleRecordByStore(Integer companyId, Integer storeId, String categories,
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
        SaleRecordByStoreAgg saleRecordByStore = new SaleRecordByStoreAgg(store.get(), categories, start, end,
                arrayListMultimap.asMap());
        return Optional.of(saleRecordByStore);
    }

    /**
     * @param store OO
     * @param start XX
     * @param end   OOXX
     */
    public void alloctSaleOrder4StoreWithPeriod(StoEntity store, LocalDate start, LocalDate end) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("alloctSaleOrder4StoreWithPeriod(%d,%s,%s) start ....", store.getId(),
                    start.toString("yyyy-MM-dd"), end.toString("yyyy-MM-dd")));
        Optional<List<Map<String, Object>>> rule_inf = getBean(SaleAlloctRuleEntityAction.class).loadEnabledCompanies();
        Preconditions.checkState(rule_inf.isPresent(), "尚未配置规则....");
        Set<Integer> companyIds = rule_inf.get().stream().mapToInt(x -> MapUtils.getInteger(x, "companyId")).boxed()
                .collect(Collectors.toSet());
        Preconditions.checkState(CollectionUtils.isNotEmpty(companyIds) && companyIds.contains(store.getCompanyId()), "尚未配置规则....");
        Optional<SaleAlloctRule4Store> rules = getBean(SaleAlloctRuleEntityAction.class).findByStore4Use(store);
        Preconditions.checkState(rules.isPresent(), "不存在 门店=%d对应的分配规则", store.getId());

        Optional<List<SaleRecord4EmployeeEntity>> saleRecord4Emps = getBean(SaleRecord4EmployeeEntityAction.class)
                .findByStoreWithPeriod(store, start, end);
        if (!saleRecord4Emps.isPresent()) return;
        List<SaleAlloct4EmpResult> saleAlloct4EmpResults = Lists.newArrayList();

        for (SaleRecord4EmployeeEntity $it : saleRecord4Emps.get()) {
            SaleAlloct4EmpResult result = new SaleAlloct4EmpResult($it);
            try {
                rules.get().allocation(result);
            } catch (Exception e) {
                logger.error("alloct has error...", e);
                result.setException(e);
            }
            saleAlloct4EmpResults.add(result);
        }
        List<SaleAlloctResultEntity> _list = Lists.newArrayList();
        saleAlloct4EmpResults.forEach(x -> _list.addAll(x.processResult()));
        getBean(SaleAlloctResultEntityAction.class).batchInsert(_list);
    }

    /**
     * @Doc * FK
     */
    public void alloctSaleOrder4EmployeeJob() {
        if (logger.isDebugEnabled()) logger.debug("Run alloctSaleOrder4EmployeeJob() .... start..");
        LoginContextHolder.setAnonymousCtx();
        try {
            //companyId startDate
            Optional<List<Map<String, Object>>> run_info = getBean(SaleAlloctRuleEntityAction.class).loadEnabledCompanies();
            if (!run_info.isPresent()) return;
            List<CompletableFuture<Void>> cfs = Lists.newArrayList();
            for (Map<String, Object> $it : run_info.get()) {
                Integer companyId = MapUtils.getInteger($it, "companyId");
                LocalDate yyyy_mm_dd = DateTimeUtils.parseYYYYMMDD(MapUtils.getString($it, "startDate"));
                OrgEntity company = getBean(OrgEntityAction.class).loadComById(companyId);
                long count = getBean(SaleRecord4EmployeeEntityAction.class).loadUndoCountByCompany(company, yyyy_mm_dd);
                if (count == 0L) continue;
                if (logger.isDebugEnabled())
                    logger.debug(String.format("saleRecord4EmployeeJob(companyId=%d,startDate = %s) undo count %d, JOb start",
                            companyId, yyyy_mm_dd, count));
                // 一起奔跑吧~~~~ 骚年......
                cfs.add(CompletableFuture.runAsync(new AlloctSaleOrderJob(company, yyyy_mm_dd)));
            }
            if (CollectionUtils.isNotEmpty(cfs))
                CompletableFuture.allOf(cfs.toArray(new CompletableFuture[]{})).join();
        } finally {
            LoginContextHolder.clear();
        }
    }

    // OXOX
    private class AlloctSaleOrderJob implements Runnable {

        private final OrgEntity company;
        private final LocalDate startDate;

        AlloctSaleOrderJob(OrgEntity company, LocalDate startDate) {
            this.company = company;
            this.startDate = startDate;
        }

        @Override
        public void run() {
            LoginContextHolder.setAnonymousCtx();
            try {
                JobParametersBuilder jb = new JobParametersBuilder();
                Map<String, Object> params = Maps.newHashMap();
                params.put("companyId", company.getId());
                params.put("startDate", startDate.toString("yyyy-MM-dd"));
                jb.addString("job.params", Joiner.on('$').withKeyValueSeparator('=').join(params));
                jb.addDate("job.tamptime", LocalDateTime.now().toDate());
                JobParameters jobParameters = jb.toJobParameters();
                try {
                    Job job = getBean("saleRecord4EmployeeJob", Job.class);
                    getJobLauncher().run(job, jobParameters);
                } catch (Exception e) {
                    logger.error("saleRecord4EmployeeJob() has error", e);
                }
            } finally {
                LoginContextHolder.clear();
            }
        }
    }

}
