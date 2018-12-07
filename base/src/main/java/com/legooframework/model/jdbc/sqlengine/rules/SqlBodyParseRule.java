package com.legooframework.model.jdbc.sqlengine.rules;

import com.google.common.base.CharMatcher;
import com.legooframework.model.jdbc.sqlengine.ResultSetType;
import com.legooframework.model.jdbc.sqlengine.SQLStatementBuilder;
import com.legooframework.model.jdbc.sqlengine.SubStatementBuilder;
import com.legooframework.model.utils.AttributesUtil;
import org.xml.sax.Attributes;

import java.util.Optional;

class SqlBodyParseRule extends BaseParseRule {
    //<body resultKey="" resultType="">
    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        Optional<String> resultKey = AttributesUtil.getIfPresent(attributes, "resultKey");
        Optional<String> resultType = AttributesUtil.getIfPresent(attributes, "resultType");
        SubStatementBuilder builder = new SubStatementBuilder(resultKey.orElse(null),
                resultType.map(s -> Enum.valueOf(ResultSetType.class, s)).orElse(null));
        getDigester().push(builder);
    }

    @Override
    public void body(String namespace, String name, String text) {
        SubStatementBuilder subStatementBuilder = getDigester().pop();
        subStatementBuilder.setStatement(CharMatcher.whitespace().trimFrom(text));
        SQLStatementBuilder builder = getDigester().peek();
        builder.setStatement(subStatementBuilder.building());
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"sqls/model/sql/body"};
    }
}
