package com.csosm.commons.jdbc.sqlcfg.rules;

import com.csosm.commons.jdbc.sqlcfg.ColumnMeta;
import com.csosm.commons.util.AttributesUtil;
import com.csosm.commons.util.XmlUtil;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.util.Map;

/**
 * Created by Smart on 2017/3/31.
 *
 * @author hxj
 */
public class SqlSubMetaParseRule extends AbstractSqlParseRule {

    private static final Logger logger = LoggerFactory.getLogger(SqlSubMetaParseRule.class);

    @Override
    public void begin(String namespace, String name, Attributes attributes) {
        Map<String, String> params = Maps.newHashMap();
        int len = attributes.getLength();
        for (int i = 0; i < len; i++) {
            String qName = attributes.getQName(i);
            if (ArrayUtils.contains(KEYS, qName)) continue;
            String value = attributes.getValue(qName);
            params.put(qName, value);
        }

        ColumnMeta columnMeta = new ColumnMeta(
                XmlUtil.getValue(attributes, "id"),
                XmlUtil.getValue(attributes, "name"),
                XmlUtil.getOptValue(attributes, "desc").orNull(),
                XmlUtil.getValue(attributes, "type"),
                AttributesUtil.getBooleanValue(attributes, "fixed", false),
                AttributesUtil.getIfPresent(attributes, "showType").orNull(),
                AttributesUtil.getBooleanValue(attributes, "sort", false),
                AttributesUtil.getBooleanValue(attributes, "category", false),
                AttributesUtil.getBooleanValue(attributes, "legend", false),
                AttributesUtil.getBooleanValue(attributes, "colfixed", false),
                AttributesUtil.getBooleanValue(attributes, "sum", false),
                AttributesUtil.getIfPresent(attributes, "sublist").orNull(),
                MapUtils.isEmpty(params) ? null : params);
        if (logger.isTraceEnabled()) logger.trace(columnMeta.toString());
        ColumnMeta builder = getDigester().peek();
        builder.addColumnMeta(columnMeta);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"sqls/model/sql/meta/col/col"};
    }

    private static String[] KEYS = new String[]{"id", "name", "desc", "type", "fixed", "showType", "sort", "category",
            "legend", "freeze", "sum", "sublist"};
}
