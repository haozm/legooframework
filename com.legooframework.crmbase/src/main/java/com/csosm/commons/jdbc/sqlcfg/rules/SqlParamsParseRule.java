package com.csosm.commons.jdbc.sqlcfg.rules;

import com.csosm.commons.jdbc.sqlcfg.QueryParam;
import com.csosm.commons.jdbc.sqlcfg.SqlMetaEntityBuilder;
import com.csosm.commons.util.XmlUtil;
import com.google.common.base.Strings;
import org.xml.sax.Attributes;

/**
 * Created by Smart on 2017/3/31.
 *
 * @author hxj
 */
public class SqlParamsParseRule extends AbstractSqlParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) {
        String type = XmlUtil.getValue(attributes, "type");
        String param_name = XmlUtil.getValue(attributes, "name");
        String fmt = XmlUtil.getOptValue(attributes, "fmt").orNull();
        String required = XmlUtil.getOptValue(attributes, "use").or("optional");
        String value = Strings.emptyToNull(XmlUtil.getOptValue(attributes, "default").or(""));
        QueryParam param = null;
        switch (type) {
            case "str":
                param = QueryParam.createStrParam(param_name, "required".equals(required), value, fmt);
                break;
            case "strs":
                param = QueryParam.createStrsParam(param_name, "required".equals(required), value, fmt);
                break;
            case "int":
                param = QueryParam.createIntParam(param_name, "required".equals(required), value, fmt);
                break;
            case "ints":
                param = QueryParam.createIntsParam(param_name, "required".equals(required), value, fmt);
                break;
            case "bln":
                param = QueryParam.createBooleanParam(param_name, "required".equals(required), value, fmt);
                break;
            case "long":
                param = QueryParam.createLongParam(param_name, "required".equals(required), value, fmt);
                break;
            case "longs":
                param = QueryParam.createLongsParam(param_name, "required".equals(required), value, fmt);
                break;
            case "btn":
                param = QueryParam.createBetweenParam(param_name, "required".equals(required), value, fmt);
                break;
            case "birthdayRange":
                param = QueryParam.createBirthdayRangeParam(param_name, "required".equals(required), value, fmt);
                break;
            default:
                throw new IllegalArgumentException(String.format("type=%s 为非法的类型定义。", type));
        }
        SqlMetaEntityBuilder builder = getDigester().peek();
        builder.addQueryParam(param);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"sqls/model/sql/params/p"};
    }
}
