package com.legooframework.model.membercare.jobs;

import com.google.common.collect.Lists;
import com.legooframework.model.membercare.entity.Touch90TaskDto;
import com.legooframework.model.membercare.entity.UpcomingTaskEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class Touch90ItemWriter implements ItemWriter<List<Touch90TaskDto>> {

    private static final Logger logger = LoggerFactory.getLogger(Touch90ItemWriter.class);

    @Override
    public void write(List<? extends List<Touch90TaskDto>> items) throws Exception {
        List<Touch90TaskDto> list = Lists.newArrayList();
        items.forEach(list::addAll);
        if (logger.isDebugEnabled())
            logger.debug(String.format("Run Touch90 Job has Detail sizei is %s", list.size()));
        if (CollectionUtils.isNotEmpty(list)) {
            upcomingTaskAction.saveOrUpdateTouch90Task(list);
        }
    }

    private UpcomingTaskEntityAction upcomingTaskAction;

    public void setUpcomingTaskAction(UpcomingTaskEntityAction upcomingTaskAction) {
        this.upcomingTaskAction = upcomingTaskAction;
    }

}
