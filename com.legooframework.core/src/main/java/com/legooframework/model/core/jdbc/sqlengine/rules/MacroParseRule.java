package com.legooframework.model.core.jdbc.sqlengine.rules;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.utils.AttributesUtil;
import org.xml.sax.Attributes;

import java.util.Map;

class MacroParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String macro_id = AttributesUtil.getValue(name, attributes, "id");
        getDigester().push(STK_MACROS, macro_id);
    }

    @Override
    public void body(String namespace, String name, String text) throws Exception {
        String macro_id = getDigester().pop(STK_MACROS);
        Map<String, String> macros = getDigester().peek(STK_MACROS);
        Preconditions.checkState(!macros.containsKey(macro_id), "存在重复的 macros Id=%s", macro_id);
        macros.put(macro_id, text);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"sqls/macros/macro"};
    }
}
