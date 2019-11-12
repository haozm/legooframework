package com.legooframework.model.core.jdbc.sqlengine;

import com.google.common.base.Preconditions;
import org.springframework.batch.item.database.AbstractCursorItemReader;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class ColumnMapStreamItemReader extends AbstractCursorItemReader<Map<String, Object>> {

    private String sql;
    private PreparedStatement preparedStatement;
    private RowMapper<Map<String, Object>> rowMapper = new ColumnMapRowMapper();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Preconditions.checkNotNull(this.sql, "待执行的SQL 不可以为控制....");
        super.doOpen();
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    protected void cleanupOnClose() throws Exception {
        JdbcUtils.closeStatement(this.preparedStatement);
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

    @Override
    protected Map<String, Object> readCursor(ResultSet rs, int currentRow) throws SQLException {
        return rowMapper.mapRow(rs, currentRow);
    }
}
