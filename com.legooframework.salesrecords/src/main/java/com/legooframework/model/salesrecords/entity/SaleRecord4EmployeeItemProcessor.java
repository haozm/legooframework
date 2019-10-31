package com.legooframework.model.salesrecords.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.Optional;

public class SaleRecord4EmployeeItemProcessor implements ItemProcessor<SaleRecord4EmployeeEntity, SaleAlloct4EmpResult> {

    private static final Logger logger = LoggerFactory.getLogger(SaleRecord4EmployeeItemProcessor.class);

    public SaleRecord4EmployeeItemProcessor(SaleAlloctRuleEntityAction saleAlloctRuleAction, StoEntityAction storeAction) {
        this.saleAlloctRuleAction = saleAlloctRuleAction;
        this.storeAction = storeAction;
    }

    @Override
    public SaleAlloct4EmpResult process(SaleRecord4EmployeeEntity item) throws Exception {
        SaleAlloct4EmpResult result = new SaleAlloct4EmpResult(item);
        try {
            Optional<StoEntity> store = storeAction.findById(item.getSaleStoreId());
            Preconditions.checkState(store.isPresent(), "不存在ID=%s 对应的门店", item.getSaleStoreId());
            Optional<SaleAlloctRule4Store> rules = saleAlloctRuleAction.findByStore4Use(store.get());
            Preconditions.checkState(rules.isPresent(), "不存在store=%d 对应的分配规则", store.get().getId());
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
