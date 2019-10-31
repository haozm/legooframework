package com.legooframework.model.salesrecords.entity;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.jdbc.sqlengine.SQLStatementFactory;
import org.apache.commons.collections4.MapUtils;
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

public class SaleRecord4EmployeeItemReader extends AbstractCursorItemReader<SaleRecord4EmployeeEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SaleRecord4EmployeeItemReader.class);

    public SaleRecord4EmployeeItemReader() {
        super();
        setName(ClassUtils.getShortName(SaleRecord4EmployeeItemReader.class));
        this.rowMapper = new SaleRecord4EmployeeEntityAction.RowMapperImpl();
    }

    private RowMapper<SaleRecord4EmployeeEntity> rowMapper;
    private PreparedStatement preparedStatement;
    private String sql;
    private SQLStatementFactory sqlStatementFactory;
    private Map<String, Object> paramMap = Maps.newHashMap();
    private static Splitter.MapSplitter SPLITTER = Splitter.on('$').withKeyValueSeparator('=');

    public void setParams(String params) {
        // companyId=%s startDate=???
        Map<String, String> _map = SPLITTER.split(params);
        paramMap.put("companyId", MapUtils.getIntValue(_map, "companyId"));
        paramMap.put("startDate", MapUtils.getString(_map, "startDate"));
        paramMap.put("sql", "findUndoByCompany");
    }

    public void setSqlStatementFactory(SQLStatementFactory sqlStatementFactory) {
        this.sqlStatementFactory = sqlStatementFactory;
    }

    @Override
    public String getSql() {
        if (Strings.isNullOrEmpty(sql)) {
            this.sql = sqlStatementFactory.getExecSql("SaleRecord4EmployeeEntity", "quer4list", paramMap);
            if (logger.isDebugEnabled())
                logger.debug(String.format("sql=%s", this.sql));
        }
        return sql;
    }

    @Override
    protected void openCursor(Connection con) {
        try {
            if (isUseSharedExtendedConnection()) {
                preparedStatement = con.prepareStatement(getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
                        ResultSet.HOLD_CURSORS_OVER_COMMIT);
            } else {
                preparedStatement = con.prepareStatement(getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            }
            applyStatementSettings(preparedStatement);
            this.rs = preparedStatement.executeQuery();
            handleWarnings(preparedStatement);
        } catch (SQLException se) {
            close();
            throw getExceptionTranslator().translate("Executing query", getSql(), se);
        }

    }

    @Nullable
    @Override
    protected SaleRecord4EmployeeEntity readCursor(ResultSet rs, int currentRow) throws SQLException {
        return rowMapper.mapRow(rs, currentRow);
    }

    /**
     * Close the cursor and database connection.
     */
    @Override
    protected void cleanupOnClose() throws Exception {
        JdbcUtils.closeStatement(this.preparedStatement);
    }

}
