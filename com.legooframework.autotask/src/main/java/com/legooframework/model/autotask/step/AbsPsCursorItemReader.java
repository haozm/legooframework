package com.legooframework.model.autotask.step;

import com.google.common.collect.Maps;
import com.legooframework.model.core.jdbc.sqlengine.SQLStatementFactory;
import org.springframework.batch.item.database.AbstractCursorItemReader;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public abstract class AbsPsCursorItemReader<T> extends AbstractCursorItemReader<T> {

    private PreparedStatement preparedStatement;
    private SQLStatementFactory sqlStatementFactory;
    private Map<String, Object> params;

    AbsPsCursorItemReader() {
        super();
        this.params = Maps.newHashMap();
    }

    public void setSqlStatementFactory(SQLStatementFactory sqlStatementFactory) {
        this.sqlStatementFactory = sqlStatementFactory;
    }

    Map<String, Object> getParams() {
        return params;
    }

    @Override
    public abstract String getSql();

    abstract RowMapper<T> getRowMapper();

    SQLStatementFactory getSqlStatementFactory() {
        return sqlStatementFactory;
    }

    /**
     * Close the cursor and database connection.
     */
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
    protected T readCursor(ResultSet rs, int currentRow) throws SQLException {
        return getRowMapper().mapRow(rs, currentRow);
    }
}
