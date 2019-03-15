package com.legooframework.model.crmadapter.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import com.legooframework.model.crmadapter.entity.TenantsRouteBuilder;
import com.legooframework.model.crmadapter.entity.TenantsRouteEntity;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TenantParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String _ids = AttributesUtil.getValue(name, attributes, "ids");
        String _domain = AttributesUtil.getValue(name, attributes, "domain");
        Set<Integer> companyIds = Stream.of(StringUtils.split(_ids, ',')).mapToInt(Integer::valueOf).boxed()
                .collect(Collectors.toSet());
        TenantsRouteBuilder builder = new TenantsRouteBuilder(companyIds, _domain);
        getDigester().push(builder);
    }

    @Override
    public void end(String namespace, String name) throws Exception {
        TenantsRouteBuilder builder = getDigester().pop();
        List<TenantsRouteEntity> list = getDigester().peek();
        list.add(builder.building());
    }

    public String[] getPatterns() {
        return new String[]{"tenants/tenant"};
    }

}
