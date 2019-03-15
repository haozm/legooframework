package com.legooframework.model.core.jdbc.sqlengine.rules;

import com.google.common.collect.Lists;
import org.apache.commons.digester3.binder.AbstractRulesModule;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Smart on 2015/11/16.
 *
 * @author HXJ
 */
public class RulesModule extends AbstractRulesModule {

    @Override
    protected void configure() {
        List<BaseParseRule> rules = Lists.newArrayListWithCapacity(8);
        rules.add(new ModelParseRule());
        rules.add(new MacroParseRule());
        rules.add(new SqlParseRule());
        rules.add(new RoleSupportParseRule());
        rules.add(new ColumnParseRule());
        rules.add(new SqlBodyParseRule());
        rules.add(new ParamParseRule());
        rules.forEach(r -> Stream.of(r.getPatterns()).forEach(c -> forPattern(c).addRule(r)));
    }

}
