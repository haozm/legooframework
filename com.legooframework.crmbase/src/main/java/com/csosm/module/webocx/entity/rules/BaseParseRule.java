package com.csosm.module.webocx.entity.rules;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.webocx.entity.Operate;
import com.csosm.module.webocx.entity.WebOcxBuilder;
import com.google.common.base.Optional;
import org.apache.commons.digester3.Rule;
import org.xml.sax.Attributes;

abstract class BaseParseRule extends Rule {

    public abstract String[] getPatterns();

    static String STK_WEBOCXS = "webocxs";

    static String STK_WEBOCX = "webocx";

    Operate buildOpt(String name, Attributes attributes) {
        String _name = AttributesUtil.getValue(name, attributes, "name");
        String title = AttributesUtil.getValue(name, attributes, "title");
        String type = AttributesUtil.getValue(name, attributes, "type");
        Optional<String> keys = AttributesUtil.getIfPresent(attributes, "keys");
        Optional<String> url = AttributesUtil.getIfPresent(attributes, "url");
        return WebOcxBuilder.buildOperate(_name, title, type, url.orNull(), keys.orNull());
    }

}
