package com.legooframework.model.crmadapter.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import com.legooframework.model.crmadapter.entity.TenantsDomainEntity;
import com.legooframework.model.crmadapter.entity.TenantsRouteFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostDomainParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String url = AttributesUtil.getValue(name, attributes, "url");
        String defualted = AttributesUtil.getIfPresent(attributes, "defualt").orElse("false");
        String _companyIds = AttributesUtil.getValue(name, attributes, "companyIds");
        List<Integer> companyIds = Stream.of(StringUtils.split(_companyIds, ','))
                .map(Integer::valueOf).collect(Collectors.toList());
        TenantsDomainEntity domainEntity = new TenantsDomainEntity(url, StringUtils.equals("true", defualted), companyIds);
        getDigester().push(domainEntity);
    }

    @Override
    public void end(String namespace, String name) throws Exception {
        TenantsDomainEntity pop = getDigester().pop();
        TenantsRouteFactoryBuilder builder = getDigester().peek();
        builder.addDomainEntity(pop);
    }

    public String[] getPatterns() {
        return new String[]{"domains/domain"};
    }
}
