package com.legooframework.model.statistical.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import com.legooframework.model.statistical.entity.EchartMetaEntity;
import com.legooframework.model.statistical.entity.SummaryMetaEntity;
import com.legooframework.model.statistical.entity.TableMetaEntity;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.util.Optional;

class TableMetaParseRule extends BaseParseRule {

    private static final Logger logger = LoggerFactory.getLogger(TableMetaParseRule.class);

    TableMetaParseRule() {
    }

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String title = AttributesUtil.getValue(name, attributes, "title");
        if (StringUtils.equals("summary", name) || StringUtils.equals("subsummary", name)) {
            String idval = AttributesUtil.getValue(name, attributes, "id");
            String sql = AttributesUtil.getValue(name, attributes, "sql");
            Optional<String> linkUrl = AttributesUtil.getIfPresent(attributes, "linkUrl");
            Optional<String> subpage = AttributesUtil.getIfPresent(attributes, "subpage");
            TableMetaEntityBuilder builder = new TableMetaEntityBuilder(idval, title, sql, linkUrl.orElse(null),
                    TableMetaEntityBuilder.SUMMARY_TYPE, subpage.orElse(null));
            getDigester().push(builder);
        } else if (StringUtils.equals("table", name)) {
            String idval = AttributesUtil.getValue(name, attributes, "id");
            String sql = AttributesUtil.getValue(name, attributes, "sql");
            Optional<String> linkUrl = AttributesUtil.getIfPresent(attributes, "linkUrl");
            TableMetaEntityBuilder builder = new TableMetaEntityBuilder(idval, title, sql, linkUrl.orElse(null),
                    TableMetaEntityBuilder.TABLE_TYPE, null);
            getDigester().push(builder);
        } else if (StringUtils.equals("echart", name)) {
            String id = AttributesUtil.getValue(name, attributes, "id");
            String type = AttributesUtil.getValue(name, attributes, "type");
            String sql = AttributesUtil.getValue(name, attributes, "sql");
            Optional<String> axisY1Title = AttributesUtil.getIfPresent(attributes, "axisY1Title");
            Optional<String> axisY2Title = AttributesUtil.getIfPresent(attributes, "axisY2Title");
            EchartMetaEntityBuilder builder = new EchartMetaEntityBuilder(id, type, title,
                    axisY1Title.orElse(null), axisY2Title.orElse(null), sql);
            getDigester().push(builder);
        }
    }

    @Override
    public void end(String namespace, String name) throws Exception {
        if (StringUtils.equals("summary", name)) {
            TableMetaEntityBuilder _builder = getDigester().pop();
            StatisticalEntityBuilder builder = getDigester().peek();
            SummaryMetaEntity meta = _builder.buildSummary();
            builder.setSummaryMeta(meta);
            if (logger.isDebugEnabled())
                logger.debug(String.format("Parse SummaryMetaEntity %s is OK", meta));
        } else if (StringUtils.equals("table", name)) {
            TableMetaEntityBuilder _builder = getDigester().pop();
            StatisticalEntityBuilder builder = getDigester().peek();
            TableMetaEntity meta = _builder.buildTable();
            builder.setTableMeta(meta);
            if (logger.isDebugEnabled())
                logger.debug(String.format("Parse TableMetaEntity %s is OK", meta));
        } else if (StringUtils.equals("echart", name)) {
            EchartMetaEntityBuilder _builder = getDigester().pop();
            StatisticalEntityBuilder builder = getDigester().peek();
            EchartMetaEntity meta = _builder.building();
            builder.addEchartMeta(meta);
            if (logger.isDebugEnabled())
                logger.debug(String.format("Parse EchartMetaEntity %s is OK", meta));
        } else if (StringUtils.equals("subsummary", name)) {
            TableMetaEntityBuilder _builder = getDigester().pop();
            StatisticalEntityBuilder builder = getDigester().peek();
            SummaryMetaEntity meta = _builder.buildSummary();
            builder.setSubSummaryMeta(meta);
            if (logger.isDebugEnabled())
                logger.debug(String.format("Parse subSummaryMetaEntity %s is OK", meta));
        }
    }

    @Override
    String[] getPatterns() {
        return new String[]{TABLE_PATH, SUMMARY_PATH, SUBSUMMARY_PATH, ECHART_PATH};
    }
}
