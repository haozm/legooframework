package com.legooframework.model.crmadapter.entity.rules;

import com.google.common.collect.Lists;
import com.legooframework.model.core.jdbc.sqlengine.rules.*;
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
        rules.add(new TenantParseRule());
        rules.add(new PostUrlParseRule());
        rules.forEach(r -> Stream.of(r.getPatterns()).forEach(c -> forPattern(c).addRule(r)));
    }

}
