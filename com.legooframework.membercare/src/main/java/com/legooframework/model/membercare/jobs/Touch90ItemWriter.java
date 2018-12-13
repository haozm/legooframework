package com.legooframework.model.membercare.jobs;

import com.google.common.collect.Lists;
import com.legooframework.model.membercare.entity.UpcomingTaskDetailEntity;
import com.legooframework.model.membercare.entity.UpcomingTaskDetailEntityAction;
import com.legooframework.model.membercare.entity.UpcomingTaskEntity;
import com.legooframework.model.membercare.entity.UpcomingTaskEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class Touch90ItemWriter implements ItemWriter<List<UpcomingTaskEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(Touch90ItemWriter.class);

    @Override
    public void write(List<? extends List<UpcomingTaskEntity>> items) throws Exception {
        List<UpcomingTaskEntity> upcoming_insert_tasks = Lists.newArrayList();
        final List<UpcomingTaskEntity> upcoming_update_tasks = Lists.newArrayList();
        List<UpcomingTaskDetailEntity> upcoming_detail_insert_tasks = Lists.newArrayList();
        final List<UpcomingTaskDetailEntity> upcoming_detail_update_tasks = Lists.newArrayList();
        for (List<UpcomingTaskEntity> item : items) {
            for (UpcomingTaskEntity task : item) {
                if (task.isCRUD4Insert()) {
                    upcoming_insert_tasks.add(task);
                    upcoming_detail_insert_tasks.addAll(task.getTaskDetails());
                } else if (task.isCRUD4Reader() || task.isCRUD4Update()) {
                    if (task.isCRUD4Update()) {
                        upcoming_update_tasks.add(task);
                    }
                    task.getTaskDetails().forEach(x -> {
                        if (x.isCRUD4Update()) upcoming_detail_update_tasks.add(x);
                    });
                }
            }
        }
        if (CollectionUtils.isNotEmpty(upcoming_insert_tasks)) {
            upcomingTaskAction.batchInsert(upcoming_insert_tasks);
            upcomingTaskDetailAction.batchInsert(upcoming_detail_insert_tasks);
        }
        upcomingTaskAction.batchUpdate(upcoming_update_tasks);
        upcomingTaskDetailAction.batchUpdate(upcoming_detail_update_tasks);
    }

    private UpcomingTaskEntityAction upcomingTaskAction;
    private UpcomingTaskDetailEntityAction upcomingTaskDetailAction;

    public void setUpcomingTaskAction(UpcomingTaskEntityAction upcomingTaskAction) {
        this.upcomingTaskAction = upcomingTaskAction;
    }

    public void setUpcomingTaskDetailAction(UpcomingTaskDetailEntityAction upcomingTaskDetailAction) {
        this.upcomingTaskDetailAction = upcomingTaskDetailAction;
    }
}
