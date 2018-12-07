package com.legooframework.model.jdbc.sqlengine.rules;

import com.google.common.base.Enums;
import com.legooframework.model.jdbc.sqlengine.ColumnType;
import com.legooframework.model.jdbc.sqlengine.ParamMeta;
import com.legooframework.model.jdbc.sqlengine.SQLStatementBuilder;
import com.legooframework.model.utils.AttributesUtil;
import org.xml.sax.Attributes;

import java.util.Optional;

/**
 * Created by Smart on 2017/3/31.
 *
 * @author hxj
 */
public class ParamParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) {
        Optional<String> type = AttributesUtil.getIfPresent(attributes, "type");
        String param_name = AttributesUtil.getValue(name, attributes, "name");
        boolean required = AttributesUtil.getBooleanValue(attributes, "required", false);
        Optional<String> def_value = AttributesUtil.getIfPresent(attributes, "default");
        Optional<String> format = AttributesUtil.getIfPresent(attributes, "format");

        ColumnType columnType = type.isPresent() ? Enums.getIfPresent(ColumnType.class, type.get())
                .or(ColumnType.STRING) : ColumnType.STRING;

        ParamMeta param = new ParamMeta(param_name, columnType, required, def_value.orElse(null),
                format.orElse(null));
        SQLStatementBuilder builder = getDigester().peek();
        builder.addParam(param);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"sqls/model/sql/params/p"};
    }
}
