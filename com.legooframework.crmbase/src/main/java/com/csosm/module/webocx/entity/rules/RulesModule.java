package com.csosm.module.webocx.entity.rules;

import com.google.common.collect.Lists;
import org.apache.commons.digester3.binder.AbstractRulesModule;

import java.util.List;

/**
 * Created by Smart on 2015/11/16.
 *
 * @author HXJ
 */
public class RulesModule extends AbstractRulesModule {

    @Override
    protected void configure() {
        List<BaseParseRule> rules = Lists.newArrayListWithCapacity(8);
        rules.add(new WebOcxItemDataItemParseRule());
        rules.add(new WebOcxItemDataParseRule());
        rules.add(new WebOcxItemParseRule());
        rules.add(new WebOcxItemItemParseRule());
        rules.add(new WebOcxCdnItemParseRule());
        rules.add(new WebOcxMetaParseRule());
        rules.add(new WebOcxSubPageParseRule());
        rules.add(new WebOcxSubPageItemParseRule());
        rules.add(new WebOcxOperateParseRule());
        rules.add(new WebOcxButtonsParseRule());
        rules.add(new WebOcxGroupParseRule());
        rules.add(new WebOcxParseRule());
        for (BaseParseRule $it : rules) {
            for (String $p : $it.getPatterns()) forPattern($p).addRule($it);
        }
    }

}
