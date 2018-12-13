package com.legooframework.model.membercare.jobs;

import com.google.common.collect.Lists;
import com.legooframework.model.membercare.entity.Touch90CareLogBuilder;
import com.legooframework.model.membercare.entity.Touch90CareLogEntity;
import com.legooframework.model.membercare.entity.Touch90CareLogEntityAction;
import com.legooframework.model.membercare.entity.UpcomingTaskEntity;
import com.legooframework.model.salesrecords.service.SaleRecordByStore;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ItemListenerSupport;

import java.util.List;

public class Touch90ItemListener extends ItemListenerSupport<List<SaleRecordByStore>, List<UpcomingTaskEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(Touch90ItemListener.class);

    @Override
    public void onProcessError(List<SaleRecordByStore> item, Exception ex) {
        logger.error(String.format("Touch90JobEntity onProcessError: %s", ex.getMessage()), ex);
    }

    @Override
    public void onWriteError(Exception ex, List<? extends List<UpcomingTaskEntity>> item) {
        logger.error(String.format("Touch90JobEntity onWriteError: %s", ex.getMessage()), ex);
    }

    @Override
    public void afterProcess(List<SaleRecordByStore> item, List<UpcomingTaskEntity> result) {
        List<Touch90CareLogEntity> buler = Lists.newArrayList();
        item.forEach(x -> {
            Touch90CareLogBuilder logBuilder = new Touch90CareLogBuilder(x.getCompany(), x.getStore(),
                    Lists.newArrayList(x.loadAllSaleRecords()));
            buler.addAll(logBuilder.build());
        });
        touch90CareLogAction.savaOrUpdate(buler);
    }

    @Override
    public void afterWrite(List<? extends List<UpcomingTaskEntity>> items) {
        int size = 0;
        if (CollectionUtils.isNotEmpty(items))
            for (List<UpcomingTaskEntity> item : items) size += item.size();
        if (logger.isInfoEnabled())
            logger.info(String.format("[FlowJob: [name=touch90Job]] finish process data size is %s", size));
    }

    @Override
    public void onReadError(Exception ex) {
        logger.error(String.format("Touch90JobEntity onReadError: %s", ex.getMessage()), ex);
    }


    private Touch90CareLogEntityAction touch90CareLogAction;

    public void setTouch90CareLogAction(Touch90CareLogEntityAction touch90CareLogAction) {
        this.touch90CareLogAction = touch90CareLogAction;
    }
}
