package com.csosm.module.menu.entity.rules;

import com.google.common.collect.Lists;
import org.apache.commons.digester3.binder.AbstractRulesModule;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Smart on 2015/11/16.
 *
 * @author HXJ
 */
public class ResRulesModule extends AbstractRulesModule {

    @Override
    protected void configure() {
        List<BaseParseRule> rules = Lists.newArrayListWithCapacity(3);
        rules.add(new MenusParseRule());
        rules.add(new MenuParseRule());
        rules.add(new WebPageParseRule());
        rules.forEach(r -> Stream.of(r.getPatterns()).forEach(c -> forPattern(c).addRule(r)));
    }

}
