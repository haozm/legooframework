package com.csosm.module.menu.entity.rules;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.menu.entity.ResEntity;
import com.csosm.module.menu.entity.ResMenuEntity;
import org.xml.sax.Attributes;

import java.util.Map;

class MenusParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String tenantId = AttributesUtil.getValue(name, attributes, "tenantId");
        getDigester().push(STK_TENANT, Long.valueOf(tenantId));
        ResMenuEntity entity = ResMenuEntity.createRoot(null, Long.valueOf(tenantId));
        getDigester().push(entity);
    }

    @Override
    public void end(String namespace, String name) throws Exception {
        getDigester().pop(STK_TENANT);
        ResMenuEntity menu_root = getDigester().pop();
        Map<Long, ResEntity> menuMap = getDigester().peek();
        menuMap.put(menu_root.getTenantId(), menu_root);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"resources/menus"};
    }

}
