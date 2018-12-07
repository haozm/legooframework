package com.legooframework.model.jdbc.sqlengine.rules;

import com.google.common.collect.Table;
import com.legooframework.model.jdbc.sqlengine.SQLStatement;
import com.legooframework.model.jdbc.sqlengine.SQLStatementBuilder;
import com.legooframework.model.utils.AttributesUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.util.Optional;

class SqlParseRule extends BaseParseRule {

    private static final Logger logger = LoggerFactory.getLogger(SqlParseRule.class);

    @Override
    public void begin(String namespace, String name, Attributes attributes) {
        String stmtId = AttributesUtil.getValue(name, attributes, "id");
        boolean dynamic = AttributesUtil.getBooleanValue(attributes, "dynamic", false);
        Optional<String> descOpt = AttributesUtil.getIfPresent(attributes, "desc");
        Optional<String> macrosOpt = AttributesUtil.getIfPresent(attributes, "macros");
        String[] macros = macrosOpt.map(s -> StringUtils.split(s, ',')).orElse(null);
        SQLStatementBuilder builder = new SQLStatementBuilder(getModelName(), stmtId, macros,
                descOpt.orElse(null), dynamic);
        getDigester().push(builder);
    }

    @Override
    public void end(String namespace, String name) {
        SQLStatementBuilder builder = getDigester().pop();
        SQLStatement statement = builder.build();
        Table<String, String, SQLStatement> sqlMetaEntityTable = getDigester().peek("statements");
        if (logger.isTraceEnabled()) logger.trace(statement.toString());
        sqlMetaEntityTable.put(statement.getModel(), statement.getStmtId(), statement);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"sqls/model/sql"};
    }
}
