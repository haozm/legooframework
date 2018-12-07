package com.legooframework.model.jdbc.sqlengine.rules;

import com.google.common.base.Enums;
import com.legooframework.model.jdbc.sqlengine.ColumnMeta;
import com.legooframework.model.jdbc.sqlengine.ColumnType;
import com.legooframework.model.jdbc.sqlengine.SQLStatementBuilder;
import com.legooframework.model.utils.AttributesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.util.Optional;

class ColumnParseRule extends BaseParseRule {

    private static final Logger logger = LoggerFactory.getLogger(ColumnParseRule.class);

    @Override
    public void begin(String namespace, String name, Attributes attributes) {
        String id = AttributesUtil.getValue(name, attributes, "id");
        String name_val = AttributesUtil.getValue(name, attributes, "name");
        Optional<String> desc = AttributesUtil.getIfPresent(attributes, "desc");
        Optional<String> type = AttributesUtil.getIfPresent(attributes, "type");
        boolean fixed = AttributesUtil.getBooleanValue(attributes, "fixed", false);
        Optional<String> showType = AttributesUtil.getIfPresent(attributes, "showType");
        ColumnType columnType = type.isPresent() ? Enums.getIfPresent(ColumnType.class, type.get()).or(ColumnType.STRING)
                : ColumnType.STRING;
        ColumnMeta columnMeta = new ColumnMeta(id, name_val, desc.orElse(null), columnType, fixed, showType.orElse(null));
        if (logger.isTraceEnabled()) logger.trace(columnMeta.toString());
        SQLStatementBuilder builder = getDigester().peek();
        builder.addColumn(columnMeta);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"sqls/model/sql/meta/col"};
    }
}
