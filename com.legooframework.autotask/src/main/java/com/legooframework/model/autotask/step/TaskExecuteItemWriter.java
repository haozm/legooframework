package com.legooframework.model.autotask.step;

import com.legooframework.model.autotask.entity.TaskExecuteEntity;
import com.legooframework.model.autotask.entity.TaskExecuteEntityAction;
import com.legooframework.model.core.utils.AppCtxSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class TaskExecuteItemWriter extends AppCtxSupport implements ItemWriter<TaskExecuteEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteItemReader.class);

    public TaskExecuteItemWriter() {
    }

    @Override
    public void write(List<? extends TaskExecuteEntity> items) throws Exception {
        getBean(TaskExecuteEntityAction.class).batchUpdateStatus(items);
        if (logger.isDebugEnabled())
            logger.debug(String.format("write size is %d", items.size()));
    }


}
