package com.legooframework.model.salesrecords.entity;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.Optional;

public class SaleRecord4EmployeeItemProcessor implements ItemProcessor<SaleRecord4EmployeeEntity, SaleAlloct4EmpResult> {

    private static final Logger logger = LoggerFactory.getLogger(SaleRecord4EmployeeItemProcessor.class);
    private static final String DEF_CACHE_POLICY = "initialCapacity=16,maximumSize=1024,expireAfterAccess=1m";
    private final Cache<Integer, Object> cache;

    public SaleRecord4EmployeeItemProcessor(SaleAlloctRuleEntityAction saleAlloctRuleAction, StoEntityAction storeAction) {
        this.saleAlloctRuleAction = saleAlloctRuleAction;
        this.storeAction = storeAction;
        this.cache = CacheBuilder.from(DEF_CACHE_POLICY).build();
    }

    @Override
    public SaleAlloct4EmpResult process(SaleRecord4EmployeeEntity item) throws Exception {
        SaleAlloct4EmpResult result = new SaleAlloct4EmpResult(item);
        try {
            Integer storeId = item.getSaleStoreId();
            Object value = cache.getIfPresent(storeId);
            if (value == null) {
                Optional<StoEntity> store_opt = storeAction.findById(storeId);
                value = store_opt.isPresent() ? store_opt.get() : "NULL";
                cache.put(item.getSaleStoreId(), value);
            }
            if (value instanceof String)
                throw new RuntimeException(String.format("不存在ID=%d 对应的门店", item.getSaleStoreId()));
            StoEntity store = (StoEntity) value;
            Optional<SaleAlloctRule4Store> rules = saleAlloctRuleAction.findByStore4Use(store);
            Preconditions.checkState(rules.isPresent(), "不存在storeId=%d 对应的分配规则", store.getId());
            rules.get().allocation(result);
        } catch (Exception e) {
            logger.error("Process has error", e);
            result.setException(e);
        }
        return result;
    }

    private SaleAlloctRuleEntityAction saleAlloctRuleAction;
    private StoEntityAction storeAction;

}
