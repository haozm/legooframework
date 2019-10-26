package com.legooframework.model.salesrecords.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.utils.ExceptionUtil;
import com.legooframework.model.covariant.entity.MemberEntity;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class SaleRecordEntityAction extends BaseEntityAction<SaleRecordEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SaleRecordEntityAction.class);

    public SaleRecordEntityAction() {
        super(null);
    }

    /**
     * 必须是同一家公司的数据信息
     *
     * @param store      门店
     * @param categories 商品分类
     * @param start      开始日期
     * @param end        截至日期
     * @param sample     是否简要信息
     * @return Optional&lt;List&lt;SaleRecordEntity&gt;&gt;
     */
    public Optional<List<SaleRecordEntity>> loadByDateInterval(StoEntity store, String categories, LocalDateTime start, LocalDateTime end,
                                                               boolean sample) {
        Map<String, Object> params = store.toParamMap();
        params.put("sample", !sample);
        if (!Strings.isNullOrEmpty(categories) && !StringUtils.equals("0", categories))
            params.put("categories", categories);
        params.put("startDay", DateTimeUtils.format(start));
        params.put("endDay", DateTimeUtils.format(end));
        params.put("sql", "loadByDateInterval");
        Optional<List<SaleRecordEntity>> saleRecordEntities = queryForEntities("loadByDateInterval", params,
                getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByDateInterval(%s,%s,%s,%s,%s) resisze is %s", store.getId(), start, end, sample,
                    categories, saleRecordEntities.map(List::size).orElse(0)));
        return saleRecordEntities;
    }

  /*  public Optional<List<SaleRecordEntity>> loadByDateInterval(Collection<CrmStoreEntity> stores, String categories,
                                                               LocalDateTime start, LocalDateTime end,
                                                               boolean sample) {
        if (stores.size() <= 100) {
            Map<String, Object> params = Maps.newHashMap();
            Set<Integer> ids = stores.stream().map(CrmStoreEntity::getId).collect(Collectors.toSet());
            params.put("storeIds", ids);
            params.put("sample", !sample);
            if (!Strings.isNullOrEmpty(categories) && StringUtils.equals("0", categories))
                params.put("categories", categories);
            params.put("companyId", stores.iterator().next().getCompanyId());
            params.put("startDay", DateTimeUtils.format(start));
            params.put("endDay", DateTimeUtils.format(end));
            Optional<List<SaleRecordEntity>> saleRecordEntities = queryForEntities("loadByDateInterval", params,
                    getRowMapper());
            if (logger.isDebugEnabled())
                logger.debug(String.format("loadByDateInterval(%s,%s,%s,%s,%s) resisze is %s", ids, start, end, sample,
                        categories, saleRecordEntities.map(List::size).orElse(0)));
            return saleRecordEntities;
        } else {
            List<SaleRecordEntity> saleRecords = Lists.newArrayList();
            List<CrmStoreEntity> stores_list = Lists.newArrayList(stores);
            List<List<CrmStoreEntity>> stores_sub_list = Lists.partition(stores_list, 100);
            Map<String, Object> fixed = Maps.newHashMap();
            fixed.put("startDay", DateTimeUtils.format(start));
            fixed.put("endDay", DateTimeUtils.format(end));
            if (!Strings.isNullOrEmpty(categories)) fixed.put("categories", categories);
            fixed.put("companyId", stores.iterator().next().getCompanyId());
            fixed.put("sample", !sample);
            List<CompletableFuture<Void>> cfs = Lists.newArrayList();
            for (List<CrmStoreEntity> list : stores_sub_list) {
                Map<String, Object> params = Maps.newHashMap();
                params.put("storeIds", list.stream().map(CrmStoreEntity::getId).collect(Collectors.toSet()));
                params.putAll(fixed);
                CompletableFuture<Void> res = asyncQueryForEntities("loadByDateInterval", params, getRowMapper())
                        .thenAccept(x -> {
                            if (CollectionUtils.isNotEmpty(x)) saleRecords.addAll(x);
                        });
                cfs.add(res);
            }
            CompletableFuture<Void> wait_all = CompletableFuture.allOf(cfs.toArray(new CompletableFuture[0]))
                    .whenComplete((v, th) -> {
                        if (logger.isDebugEnabled())
                            logger.debug(String.format("loadByDateInterval(stores' size ::%s,%s,%s,%s) resisze is %s",
                                    stores.size(), start,
                                    end, sample, saleRecords.isEmpty() ? 0 : saleRecords.size()));
                    });
            wait_all.join();
            return Optional.ofNullable(saleRecords.isEmpty() ? null : saleRecords);
        }
    }*/

    public Optional<List<SaleRecordEntity>> loadMemberBy90Days(MemberEntity member) {
        Preconditions.checkNotNull(member);
        Map<String, Object> params = member.toParamMap();
        params.put("hasDetail", true);
        Optional<List<SaleRecordEntity>> detrails = super.queryForEntities("loadMemberBy90Days", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadMemberBy90Days(%s) size is %s .", params, detrails.map(List::size).orElse(0)));
        return detrails;
    }

    public void updateChangeFlag(Collection<SaleRecordEntity> saleRecords) {
        if (CollectionUtils.isEmpty(saleRecords)) return;
        super.batchUpdate("updateChangeFlag", (ps, saleRecord) -> {
            ps.setObject(1, saleRecord.getChangeFlag());
            ps.setObject(2, saleRecord.getId());
        }, saleRecords);
    }


//    public MergeSaleRecords getMergeSaleRecords(Collection<CrmStoreEntity> stores, String categories,
//                                                LocalDateTime start, LocalDateTime end, boolean sample) {
//        Optional<List<SaleRecordEntity>> saleRecords = loadByDateInterval(stores, categories, start, end, sample);
//        Integer companyId = stores.iterator().next().getCompanyId();
//        return saleRecords.map(x -> new MergeSaleRecords(companyId, start, end, x))
//                .orElseGet(() -> new MergeSaleRecords(companyId, start, end, null));
//    }

    public Optional<SaleRecordEntity> findSampleById(Object id, OrgEntity company) {
        Preconditions.checkNotNull(id, "findSampleById(Object id) id is not null");
        try {
            Map<String, Object> params = Maps.newHashMap();
            params.put("id", id);
            params.put("hasDetail", false);
            Integer router = company.getId();
            return super.queryForEntity(getStatementFactory(), getModelName(), "findById",
                    params, getRowMapper());
        } catch (Exception e) {
            String err_msg = String.format("%s(%s) has error", "findSampleById", id);
            throw ExceptionUtil.handleException(e, err_msg, logger);
        }
    }

    @Override
    protected RowMapper<SaleRecordEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SaleRecordEntity> {
        @Override
        public SaleRecordEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            Integer creator = resultSet.getInt("creator");
            Integer tenantId = resultSet.getInt("tenantId");
            boolean sample = resultSet.getInt("sample") == 0;
            return new SaleRecordEntity(resultSet.getInt("id"), resultSet, tenantId.longValue(),
                    creator.longValue(), sample);
        }
    }

}
