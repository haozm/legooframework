package com.legooframework.model.autotask.step;

import com.google.common.base.Strings;
import com.legooframework.model.autotask.entity.TaskExecuteEntity;
import com.legooframework.model.autotask.entity.TaskExecuteEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.ClassUtils;

public class TaskExecuteItemReader extends AbsPsCursorItemReader<TaskExecuteEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteItemReader.class);
    private RowMapper<TaskExecuteEntity> rowMapper;
    private String sql;

    public TaskExecuteItemReader() {
        super();
        setName(ClassUtils.getShortName(TaskExecuteItemReader.class));
        this.rowMapper = new TaskExecuteEntityAction.RowMapperImpl();
        getParams().put("sql", "findTaskExecute4Todo");
    }

    @Override
    public String getSql() {
        if (Strings.isNullOrEmpty(sql)) {
            this.sql = getSqlStatementFactory().getExecSql("TaskExecuteEntity", "query4list", getParams());
            if (logger.isDebugEnabled()) logger.debug(this.sql);
        }
        return sql;
    }

    @Override
    RowMapper<TaskExecuteEntity> getRowMapper() {
        return rowMapper;
    }
}
