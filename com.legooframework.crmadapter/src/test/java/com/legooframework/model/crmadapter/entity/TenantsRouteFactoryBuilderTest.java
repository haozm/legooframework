package com.legooframework.model.crmadapter.entity;

import com.legooframework.model.crmadapter.entity.rules.RulesModule;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;

public class TenantsRouteFactoryBuilderTest {

    @Test
    public void addUrlItem() throws Exception {
        RulesModule rulesModule = new RulesModule();
        Digester digester = DigesterLoader.newLoader(rulesModule).newDigester();
        TenantsRouteFactoryBuilder builder = new TenantsRouteFactoryBuilder();
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/tenants-route-cfg.xml");
        digester.push(builder);
        digester.parse(file);
        System.out.println(builder);
    }
}