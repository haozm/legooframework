package com.legooframework.model.autotask.step;

import com.google.common.collect.Lists;
import com.legooframework.model.autotask.entity.*;
import com.legooframework.model.core.utils.AppCtxSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.stream.Collectors;

public class TaskSourceItemWriter extends AppCtxSupport implements ItemWriter<TaskProcessAgg> {

    private static final Logger logger = LoggerFactory.getLogger(TaskSourceItemWriter.class);

    public TaskSourceItemWriter() {
    }

    @Override
    public void write(List<? extends TaskProcessAgg> items) throws Exception {
        List<TaskSourceEntity> taskSources = items.stream().map(TaskProcessAgg::getTaskSource).collect(Collectors.toList());
        getTaskSourceAction().updateStatus(taskSources);
        List<TaskExecuteEntity> taskExecutes = Lists.newArrayList();
        items.forEach(item -> item.getExecTasks().ifPresent(taskExecutes::addAll));
        getTaskExecuteEntityAction().batchInsert(taskExecutes);
        if (logger.isDebugEnabled())
            logger.debug(String.format("TaskSourceItemWriter(...) write taskSources'size %s and taskExecutes'size is %s",
                    taskSources.size(), taskExecutes.size()));
    }

    // ----------------- setter ------------------------------
    private TaskSourceEntityAction getTaskSourceAction() {
        return this.getBean(TaskSourceEntityAction.class);
    }

    private TaskExecuteEntityAction getTaskExecuteEntityAction() {
        return this.getBean(TaskExecuteEntityAction.class);
    }

}
