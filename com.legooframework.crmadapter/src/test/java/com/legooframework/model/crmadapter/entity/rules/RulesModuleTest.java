package com.legooframework.model.crmadapter.entity.rules;

import com.google.common.collect.Lists;
import com.legooframework.model.crmadapter.entity.TenantsRouteFactory;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class RulesModuleTest {

    @Test
    public void configure() throws Exception {
        RulesModule rulesModule = new RulesModule();
        List<TenantsRouteFactory.UrlItem> lis = Lists.newArrayList();
        Digester digester = DigesterLoader.newLoader(rulesModule).newDigester();
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/tenants-route-cfg.xml");
        digester.push(lis);
        digester.parse(file);
        System.out.println(lis.size());
    }
}