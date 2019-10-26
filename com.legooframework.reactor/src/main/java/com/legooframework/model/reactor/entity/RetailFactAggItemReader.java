package com.legooframework.model.reactor.entity;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
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

public class RetailFactAggItemReader extends AbstractCursorItemReader<RetailFactEntity> {

    private static final Logger logger = LoggerFactory.getLogger(RetailFactAggItemReader.class);

    public RetailFactAggItemReader() {
        super();
        setName(ClassUtils.getShortName(RetailFactAggItemReader.class));
        this.rowMapper = new RetailFactRowMapper();
    }

    private RowMapper<RetailFactEntity> rowMapper;
    private PreparedStatement preparedStatement;
    private String sql;
    private SQLStatementFactory sqlStatementFactory;
    private Map<String, Object> paramMap = Maps.newHashMap();

    public void setParams(String params) {
        //stmtId=%s,companyIds=%s,companyShortName=%d,maxId=%s
        Map<String, String> _map = Splitter.on('$').withKeyValueSeparator('=').split(params);
        paramMap.putAll(_map);
    }

    public void setSqlStatementFactory(SQLStatementFactory sqlStatementFactory) {
        this.sqlStatementFactory = sqlStatementFactory;
    }

    @Override
    public String getSql() {
        if (Strings.isNullOrEmpty(sql)) {
            String stmtId = MapUtils.getString(this.paramMap, "stmtId");
            String[] args = StringUtils.split(stmtId, '.');
            this.sql = sqlStatementFactory.getExecSql(args[0], args[1], paramMap);
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
//            if (this.preparedStatementSetter != null) {
//                preparedStatementSetter.setValues(preparedStatement);
//            }
            this.rs = preparedStatement.executeQuery();
            handleWarnings(preparedStatement);
        } catch (SQLException se) {
            close();
            throw getExceptionTranslator().translate("Executing query", getSql(), se);
        }

    }

    @Nullable
    @Override
    protected RetailFactEntity readCursor(ResultSet rs, int currentRow) throws SQLException {
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
