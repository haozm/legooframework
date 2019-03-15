package com.csosm.commons.jdbc.sqlcfg.rules;

import com.google.common.base.Preconditions;
import org.xml.sax.Attributes;

import java.util.Map;

public class MacrosParseRule extends AbstractSqlParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String id_val = attributes.getValue("id");
        getDigester().push("macroMap", id_val);
    }

    @Override
    public void body(String namespace, String name, String text) throws Exception {
        String id = getDigester().pop("macroMap");
        Map<String, String> macroMap = getDigester().peek("macroMap");
        Preconditions.checkState(!macroMap.containsKey(id), "存在重复的 macros Id=%s", id);
        macroMap.put(id, text);
    }


    @Override
    public String[] getPatterns() {
        return new String[]{"sqls/macros/macro"};
    }
}
