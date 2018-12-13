package com.legooframework.model.salesrecords.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.utils.ExceptionUtil;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SaleRecordEntityAction extends BaseEntityAction<SaleRecordEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SaleRecordEntityAction.class);

    public SaleRecordEntityAction() {
        super(null);
    }

    /**
     * 必须是同一家公司的数据信息
     *
     * @param stores 门店列表
     * @param start  开始日期
     * @param end    截至日期
     * @param sample 是否简要信息
     * @return Optional&lt;List&lt;SaleRecordEntity&gt;&gt;
     */
    public Optional<List<SaleRecordEntity>> loadByDateInterval(Collection<CrmStoreEntity> stores,
                                                               LocalDateTime start, LocalDateTime end, boolean sample) {
        if (stores.size() <= 100) {
            Map<String, Object> params = Maps.newHashMap();
            Set<Integer> ids = stores.stream().map(CrmStoreEntity::getId).collect(Collectors.toSet());
            params.put("storeIds", ids);
            params.put("sample", !sample);
            params.put("startDay", DateTimeUtils.format(start));
            params.put("endDay", DateTimeUtils.format(end));
            Optional<List<SaleRecordEntity>> saleRecordEntities = queryForEntities("loadByDateInterval", params, getRowMapper());
            if (logger.isDebugEnabled())
                logger.debug(String.format("loadByDateInterval(%s,%s,%s,%s) resisze is %s", ids, start, end, sample,
                        saleRecordEntities.map(List::size).orElse(0)));
            return saleRecordEntities;
        } else {
            List<SaleRecordEntity> saleRecords = Lists.newArrayList();
            List<CrmStoreEntity> stores_list = Lists.newArrayList(stores);
            List<List<CrmStoreEntity>> stores_sub_list = Lists.partition(stores_list, 100);
            Map<String, Object> fixed = Maps.newHashMap();
            fixed.put("startDay", DateTimeUtils.format(start));
            fixed.put("endDay", DateTimeUtils.format(end));
            fixed.put("sample", !sample);
            List<CompletableFuture<Void>> cfs = Lists.newArrayList();
            for (List<CrmStoreEntity> list : stores_sub_list) {
                Map<String, Object> params = Maps.newHashMap();
                params.put("storeIds", list.stream().map(CrmStoreEntity::getId).collect(Collectors.toSet()));
                params.putAll(fixed);
                CompletableFuture<Void> res = asyncQueryForEntities("loadByDateInterval", params, getRowMapper()).thenAccept(x -> {
                    if (CollectionUtils.isNotEmpty(x)) saleRecords.addAll(x);
                });
                cfs.add(res);
            }
            CompletableFuture<Void> wait_all = CompletableFuture.allOf(cfs.toArray(new CompletableFuture[0]))
                    .whenComplete((v, th) -> {
                        if (logger.isDebugEnabled())
                            logger.debug(String.format("loadByDateInterval(stores' size ::%s,%s,%s,%s) resisze is %s", stores.size(), start,
                                    end, sample, saleRecords.isEmpty() ? null : saleRecords.size()));
                    });
            wait_all.join();
            return Optional.ofNullable(saleRecords.isEmpty() ? null : saleRecords);
        }
    }

    public MergeSaleRecords getMergeSaleRecords(Collection<CrmStoreEntity> stores,
                                                LocalDateTime start, LocalDateTime end, boolean sample) {
        Optional<List<SaleRecordEntity>> saleRecords = loadByDateInterval(stores, start, end, sample);
        Integer companyId = stores.iterator().next().getCompanyId();
        return saleRecords.map(x -> new MergeSaleRecords(companyId, start, end, x))
                .orElseGet(() -> new MergeSaleRecords(companyId, start, end, null));
    }

    @Override
    public Optional<SaleRecordEntity> findById(Object id) {
        try {
            Map<String, Object> params = Maps.newHashMap();
            params.put("id", id);
            params.put("hasDetail", true);
            return super.queryForEntity(getStatementFactory(), getModelName(), "findById",
                    params, getRowMapper());
        } catch (Exception e) {
            String err_msg = String.format("%s(%s) has error", "findById", id);
            throw ExceptionUtil.handleException(e, err_msg, logger);
        }
    }

    public Optional<SaleRecordEntity> findSampleById(Object id) {
        Preconditions.checkNotNull(id, "findSampleById(Object id) id is not null");
        try {
            Map<String, Object> params = Maps.newHashMap();
            params.put("id", id);
            params.put("hasDetail", false);
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

    class RowMapperImpl implements RowMapper<SaleRecordEntity> {
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
