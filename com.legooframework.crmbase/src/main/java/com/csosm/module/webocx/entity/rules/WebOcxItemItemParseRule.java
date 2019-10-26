package com.csosm.module.webocx.entity.rules;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.webocx.entity.OcxItem;
import com.csosm.module.webocx.entity.PageDefinedBuilder;
import com.csosm.module.webocx.entity.WebOcxBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

import java.util.List;
import java.util.Map;

class WebOcxItemItemParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String type = AttributesUtil.getValue(name, attributes, "type");
        String field = AttributesUtil.getValue(name, attributes, "field");
        String placeholder = AttributesUtil.getIfPresent(attributes, "placeholder").orNull();
        boolean required = AttributesUtil.getBooleanValue(attributes, "required", false);
        String defvalue = AttributesUtil.getIfPresent(attributes, "defvalue").orNull();
        String dataType = AttributesUtil.getIfPresent(attributes, "dataType").or("string");
        List<Map<String, String>> maps = Lists.newArrayList();
        String datas = AttributesUtil.getValue(name, attributes, "datas");
        String[] strs = StringUtils.split(datas, ',');
        for (String val : strs) {
            String[] args = StringUtils.split(val, ':');
            Map<String, String> map = Maps.newHashMap();
            map.put("label", args[0]);
            map.put("value", args[1]);
            maps.add(map);
        }

        WebOcxBuilder item_builder = WebOcxBuilder.createOcxSubItemBuilder(type, field,  placeholder, defvalue,
                required, dataType, maps);
        OcxItem item = item_builder.buildOcxSubItem();
        WebOcxBuilder builder = getDigester().peek();
        builder.setOcxSubItem(item);
    }


    @Override
    public String[] getPatterns() {
        return new String[]{"webocxs/webocx/group/queries/item/item"};
    }

}
