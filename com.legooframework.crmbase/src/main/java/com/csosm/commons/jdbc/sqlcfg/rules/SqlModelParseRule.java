package com.csosm.commons.jdbc.sqlcfg.rules;

import org.xml.sax.Attributes;

/**
 * Created by Smart on 2017/3/31.
 *
 * @author hxj
 */
public class SqlModelParseRule extends AbstractSqlParseRule {

  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    String id_val = attributes.getValue("id");
    getDigester().push(KEY_MODEL_NAME, id_val);
  }

  @Override
  public void end(String namespace, String name) throws Exception {
    getDigester().pop(KEY_MODEL_NAME);
  }

  @Override
  public String[] getPatterns() {
    return new String[] {"sqls/model"};
  }
}
