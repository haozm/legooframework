package com.legooframework.model.membercare.jobs;

import com.google.common.collect.Lists;
import com.legooframework.model.salesrecords.service.SaleRecordByStore;
import com.legooframework.model.salesrecords.service.SaleRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Touch90ItemReader implements ItemReader<List<SaleRecordByStore>>, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(Touch90ItemReader.class);

    @Override
    public List<SaleRecordByStore> read() throws Exception, UnexpectedInputException, ParseException,
            NonTransientResourceException {
        if (steps == null) doRead();
        // start read data
        if (index == this.steps.size()) return null;
        List<SaleRecordByStore> res = this.steps.get(index);
        index++;
        return res;
    }

    private void doRead() {
        Long comanyId = (Long) params.get(0);
        Optional<List<SaleRecordByStore>> saleRecordByStore = this.appCtx.getBean(SaleRecordService.class)
                .loadSaleRecordByStore(comanyId.intValue(), null, (Date) params.get(1), (Date) params.get(2), true);
        if (logger.isDebugEnabled())
            logger.debug(String.format("doRead(%s,%s,%s) size is %s", params.get(0), params.get(1), params.get(2),
                    saleRecordByStore.map(List::size).orElse(0)));
        List<SaleRecordByStore> sale_recodes = saleRecordByStore.orElseGet(Lists::newArrayList);
        if (CollectionUtils.isEmpty(sale_recodes)) {
            this.steps = Lists.newArrayList();
        } else {
            int size = sale_recodes.size();
            if (size <= 10) {
                this.steps = Lists.partition(sale_recodes, 2);
            } else if (size <= 50) {
                this.steps = Lists.partition(sale_recodes, 5);
            } else if (size <= 100) {
                this.steps = Lists.partition(sale_recodes, 10);
            } else if (size <= 500) {
                this.steps = Lists.partition(sale_recodes, 50);
            } else if (size <= 1000) {
                this.steps = Lists.partition(sale_recodes, 100);
            } else if (size <= 2000) {
                this.steps = Lists.partition(sale_recodes, 150);
            } else {
                int _size = sale_recodes.size() % 20 + 2;
                this.steps = Lists.partition(sale_recodes, _size);
            }
        }
    }

    private List<?> params;
    private int index = 0;

    public void setParams(List<?> params) {
        this.params = params;
    }

    private List<List<SaleRecordByStore>> steps;

    private ApplicationContext appCtx;

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }

}
