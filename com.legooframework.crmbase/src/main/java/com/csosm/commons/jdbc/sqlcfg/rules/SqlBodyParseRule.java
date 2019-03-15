package com.csosm.commons.jdbc.sqlcfg.rules;

import com.csosm.commons.jdbc.sqlcfg.SqlMetaEntityBuilder;
import com.google.common.base.CharMatcher;

/**
 * Created by Smart on 2017/3/31.
 *
 * @author hxj
 */
public class SqlBodyParseRule extends AbstractSqlParseRule {


  @Override
  public void body(String namespace, String name, String text) {
    SqlMetaEntityBuilder builder = getDigester().peek();
    builder.setSql(CharMatcher.whitespace().trimFrom(text));
  }

  @Override
  public String[] getPatterns() {
    return new String[] {"sqls/model/sql/body"};
  }
}
