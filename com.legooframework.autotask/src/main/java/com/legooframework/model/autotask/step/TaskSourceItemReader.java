package com.legooframework.model.autotask.step;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.autotask.entity.TaskExecuteEntity;
import com.legooframework.model.autotask.entity.TaskExecuteEntityAction;
import com.legooframework.model.autotask.entity.TaskSourceEntity;
import com.legooframework.model.autotask.entity.TaskSourceEntityAction;
import com.legooframework.model.core.jdbc.sqlengine.SQLStatementFactory;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.AbstractCursorItemReader;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class TaskSourceItemReader extends AbsPsCursorItemReader<TaskSourceEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TaskSourceItemReader.class);
    private String sql;
    private RowMapper<TaskSourceEntity> rowMapper;

    public TaskSourceItemReader() {
        super();
        setName(ClassUtils.getShortName(TaskSourceItemReader.class));
        this.rowMapper = new TaskSourceEntityAction.RowMapperImpl();
        getParams().put("sql", "findUndoList");
    }

    public void setStepParams(String stepParams) {
//        params_step01.put("step01.switchesOn", Joiner.on('#').join(switches_on_str));
        Map<String, String> _map = Splitter.on('$').withKeyValueSeparator('=').split(stepParams);
        String switchesOn = MapUtils.getString(_map, "step01.switch");
        getParams().put("switches", Splitter.on('#').splitToList(switchesOn));
    }

    @Override
    public String getSql() {
        if (Strings.isNullOrEmpty(sql)) {
            this.sql = getSqlStatementFactory().getExecSql("TaskSourceEntity", "query4list", getParams());
            if (logger.isDebugEnabled()) logger.debug(this.sql);
        }
        return sql;
    }

    @Override
    RowMapper<TaskSourceEntity> getRowMapper() {
        return this.rowMapper;
    }
}
