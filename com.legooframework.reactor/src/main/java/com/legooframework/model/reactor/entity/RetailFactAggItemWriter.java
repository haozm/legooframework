package com.legooframework.model.reactor.entity;

import com.legooframework.model.covariant.entity.SendSmsEntity;
import com.legooframework.model.covariant.entity.SendSmsEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.stream.Collectors;

public class RetailFactAggItemWriter implements ItemWriter<RetailFactAgg> {

    private static final Logger logger = LoggerFactory.getLogger(RetailFactAggItemWriter.class);

    public RetailFactAggItemWriter(ReactorLogEntityAction reactorLogAction, SendSmsEntityAction sendSmsAction) {
        this.reactorLogAction = reactorLogAction;
        this.sendSmsAction = sendSmsAction;
    }

    @Override
    public void write(List<? extends RetailFactAgg> sources) throws Exception {
        String batchNo = String.format("JB-%s", LocalDateTime.now().toString("yyyyMMddHHmmss"));
        List<ReactorLogEntity> reactorLogs = sources.stream().map(RetailFactAgg::createReactorLog).collect(Collectors.toList());
        this.reactorLogAction.batchInsert(reactorLogs);
        List<RetailFactAgg> todoList = sources.stream().filter(RetailFactAgg::hasSms).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(todoList)) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("%s has hasSms item size is %s", batchNo, todoList.size()));
            List<SendSmsEntity> smses = todoList.stream().map(RetailFactAgg::getSendSms).collect(Collectors.toList());
            smses.forEach(x -> x.setBatchNo(batchNo));
            this.sendSmsAction.batchAdd4Send(smses);
        }
    }

    private ReactorLogEntityAction reactorLogAction;
    private SendSmsEntityAction sendSmsAction;
}
