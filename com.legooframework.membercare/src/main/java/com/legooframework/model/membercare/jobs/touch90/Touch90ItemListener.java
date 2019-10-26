package com.legooframework.model.membercare.jobs.touch90;

import com.google.common.collect.Lists;
import com.legooframework.model.membercare.entity.Touch90CareLogBuilder;
import com.legooframework.model.membercare.entity.Touch90CareLogEntity;
import com.legooframework.model.membercare.entity.Touch90CareLogEntityAction;
import com.legooframework.model.membercare.entity.Touch90TaskDto;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import com.legooframework.model.salesrecords.entity.SaleRecordEntityAction;
import com.legooframework.model.salesrecords.service.SaleRecordByStore;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ItemListenerSupport;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Touch90ItemListener extends ItemListenerSupport<SaleRecordByStore, List<Touch90TaskDto>> {

    private static final Logger logger = LoggerFactory.getLogger(Touch90ItemListener.class);

    @Override
    public void onProcessError(SaleRecordByStore item, Exception ex) {
        logger.error(String.format("Touch90JobEntity onProcessError: %s", ex.getMessage()), ex);
    }

    @Override
    public void onWriteError(Exception ex, List<? extends List<Touch90TaskDto>> item) {
        logger.error(String.format("Touch90JobEntity onWriteError: %s", ex.getMessage()), ex);
    }

    @Override
    public void afterProcess(SaleRecordByStore item, List<Touch90TaskDto> result) {
        List<Touch90CareLogEntity> logs = Lists.newArrayList();
        Touch90CareLogBuilder logBuilder = new Touch90CareLogBuilder(item.getStore(), item.getCategories(),
                Lists.newArrayList(item.loadAllSaleRecords()));
        logs.addAll(logBuilder.build());
        List<SaleRecordEntity> saleRecords = item.loadAllSaleRecords().stream().filter(SaleRecordEntity::isNeedUpdateChangeFlag)
                .collect(Collectors.toList());
        saleRecordEntityAction.updateChangeFlag(saleRecords);
        touch90CareLogAction.savaOrUpdate(logs);
    }

    @Override
    public void afterWrite(List<? extends List<Touch90TaskDto>> items) {
        int size = 0;
        if (CollectionUtils.isNotEmpty(items))
            for (List<Touch90TaskDto> item : items) size += item.size();
        if (logger.isInfoEnabled())
            logger.info(String.format("[FlowJob: [name=touch90Job]] finish process data size is %s", size));
    }

    @Override
    public void onReadError(Exception ex) {
        logger.error(String.format("Touch90JobEntity onReadError: %s", ex.getMessage()), ex);
    }

    private Touch90CareLogEntityAction touch90CareLogAction;
    private SaleRecordEntityAction saleRecordEntityAction;

    public void setSaleRecordEntityAction(SaleRecordEntityAction saleRecordEntityAction) {
        this.saleRecordEntityAction = saleRecordEntityAction;
    }

    public void setTouch90CareLogAction(Touch90CareLogEntityAction touch90CareLogAction) {
        this.touch90CareLogAction = touch90CareLogAction;
    }

}
