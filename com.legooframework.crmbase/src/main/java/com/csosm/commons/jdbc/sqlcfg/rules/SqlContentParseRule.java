package com.csosm.commons.jdbc.sqlcfg.rules;

import com.csosm.commons.jdbc.sqlcfg.SqlMetaEntity;
import com.csosm.commons.jdbc.sqlcfg.SqlMetaEntityBuilder;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

/**
 * Created by Smart on 2017/3/31.
 *
 * @author hxj
 */
public class SqlContentParseRule extends AbstractSqlParseRule {

    private static final Logger logger = LoggerFactory.getLogger(SqlContentParseRule.class);

    @Override
    public void begin(String namespace, String name, Attributes attributes) {
        String id_val = attributes.getValue("id");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id_val));
        String dynamic_val = attributes.getValue("dynamic");
        String desc_val = attributes.getValue("desc");
        String macros = attributes.getValue("macros");
        SqlMetaEntityBuilder builder =
                new SqlMetaEntityBuilder(
                        getModelName(), id_val, StringUtils.equals("true", dynamic_val), desc_val, macros);
        getDigester().push(builder);
    }

    @Override
    public void end(String namespace, String name) {
        SqlMetaEntityBuilder builder = getDigester().pop();
        SqlMetaEntity metaEntity = builder.build();
        Table<String, String, SqlMetaEntity> sqlMetaEntityTable = getDigester().peek();
        if (logger.isTraceEnabled()) logger.trace(metaEntity.toString());
        sqlMetaEntityTable.put(metaEntity.getModelName(), metaEntity.getStmtId(), metaEntity);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"sqls/model/sql"};
    }
}
