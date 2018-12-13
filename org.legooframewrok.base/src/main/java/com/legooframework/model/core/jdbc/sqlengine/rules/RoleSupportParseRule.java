package com.legooframework.model.core.jdbc.sqlengine.rules;

import com.legooframework.model.core.jdbc.sqlengine.SQLStatementBuilder;
import com.legooframework.model.core.utils.AttributesUtil;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

import java.util.Optional;

/**
 * Created by Smart on 2017/3/31.
 *
 * @author hxj
 */
public class RoleSupportParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) {
        Optional<String> include = AttributesUtil.getIfPresent(attributes, "include");
        Optional<String> exclude = AttributesUtil.getIfPresent(attributes, "exclude");
        String[] args_in = include.isPresent() ? StringUtils.split(include.get(), ',') :
                null;
        String[] args_ex = include.isPresent() ? StringUtils.split(exclude.get(), ',') :
                null;
        SQLStatementBuilder builder = getDigester().peek();
        builder.setIncluds(args_in);
        builder.setExcluds(args_ex);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"sqls/model/sql/roles"};
    }
}
