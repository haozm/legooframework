package com.legooframework.model.httpproxy.entity.rules;

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
        List<BaseParseRule> rules = Lists.newArrayListWithCapacity(4);
        rules.add(new ModuleParseRule());
        rules.add(new ModuleItemParseRule());
        rules.forEach(r -> Stream.of(r.getPatterns()).forEach(c -> forPattern(c).addRule(r)));
    }

}
