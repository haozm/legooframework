package com.csosm.module.webocx.entity.rules;

import java.util.Map;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.webocx.entity.WebOcx;
import com.csosm.module.webocx.entity.WebOcxBuilder;
import org.xml.sax.Attributes;

class WebOcxParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String webocx_id = AttributesUtil.getValue(name, attributes, "id");
        String url = AttributesUtil.getValue(name, attributes, "url");
        String stmtId = AttributesUtil.getIfPresent(attributes, "stmtId").orNull();
        String title = AttributesUtil.getIfPresent(attributes, "title").orNull();
        boolean paged = AttributesUtil.getBooleanValue(attributes, "paged", false);
        boolean showCount = AttributesUtil.getBooleanValue(attributes, "showCount", false);
        boolean reject = AttributesUtil.getBooleanValue(attributes, "reject", true);
        String index = AttributesUtil.getIfPresent(attributes, "index").or("0");
        String group = AttributesUtil.getIfPresent(attributes, "group").orNull();
        String desc = AttributesUtil.getIfPresent(attributes, "desc").orNull();
        WebOcxBuilder builder = WebOcxBuilder.createWebocxBuilder(webocx_id, url, stmtId, paged, showCount, title,
                Integer.valueOf(index), group, reject, desc);
        getDigester().push(builder);
    }

    @Override
    public void end(String namespace, String name) throws Exception {
        WebOcxBuilder webocx_builder = getDigester().pop();
        WebOcx webOcx = webocx_builder.buildWebOcx();
        Map<String, WebOcx> webOcxMap = getDigester().peek();
        webOcxMap.put(webOcx.getId(), webOcx);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"webocxs/webocx"};
    }
}
