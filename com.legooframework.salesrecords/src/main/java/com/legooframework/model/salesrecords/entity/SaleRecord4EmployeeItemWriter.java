package com.legooframework.model.salesrecords.entity;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.stream.Collectors;

public class SaleRecord4EmployeeItemWriter implements ItemWriter<SaleAlloct4EmpResult> {

    private static final Logger logger = LoggerFactory.getLogger(SaleRecord4EmployeeItemWriter.class);

    public SaleRecord4EmployeeItemWriter(SaleAlloctResultEntityAction saleAlloctResultAction,
                                         SaleRecord4EmployeeEntityAction saleRecord4EmployeeAction) {
        this.saleAlloctResultAction = saleAlloctResultAction;
        this.saleRecord4EmployeeAction = saleRecord4EmployeeAction;
    }

    @Override
    public void write(List<? extends SaleAlloct4EmpResult> results) throws Exception {
        if (CollectionUtils.isEmpty(results)) return;
        List<Integer> saleRecordIds = results.stream().map(SaleAlloct4EmpResult::getSaleRecordId).collect(Collectors.toList());
        List<SaleAlloctResultEntity> alloctResults = Lists.newArrayList();
        results.forEach(x -> alloctResults.addAll(x.processResult()));
        saleAlloctResultAction.batchInsert(alloctResults);
        saleRecord4EmployeeAction.updateStatus(saleRecordIds);
        if (logger.isDebugEnabled())
            logger.debug(String.format("write(....) finsh size is %d", results.size()));
    }

    private SaleAlloctResultEntityAction saleAlloctResultAction;
    private SaleRecord4EmployeeEntityAction saleRecord4EmployeeAction;
}
