package com.csosm.commons.jdbc.sqlcfg.rules;

import com.google.common.collect.Lists;
import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Smart on 2015/11/16.
 *
 * @author HXJ
 */
public class SqlConfigRulesModule extends AbstractRulesModule {

    private static final Logger logger = LoggerFactory.getLogger(SqlConfigRulesModule.class);

    @Override
    protected void configure() {
        List<AbstractSqlParseRule> rules = Lists.newArrayListWithCapacity(8);
        rules.add(new SqlModelParseRule());
        rules.add(new MacrosParseRule());
        rules.add(new SqlContentParseRule());
        rules.add(new SqlBodyParseRule());
        rules.add(new SqlMetaParseRule());
        rules.add(new SqlParamsParseRule());
        rules.add(new SqlSubMetaParseRule());
        for (AbstractSqlParseRule rule : rules)
            for (String $cursor : rule.getPatterns()) {
                forPattern($cursor).addRule(rule);
                if (logger.isTraceEnabled()) logger.trace(String.format(LOG_TEMPLATE, $cursor, rule));
            }
    }

    private static final String LOG_TEMPLATE = "Add SqlParseRule: %s: %s";
}
