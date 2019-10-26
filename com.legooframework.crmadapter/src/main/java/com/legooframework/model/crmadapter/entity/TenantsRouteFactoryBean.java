package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.config.MonitorFileSystem;
import com.legooframework.model.crmadapter.entity.rules.RulesModule;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TenantsRouteFactoryBean extends AbstractFactoryBean<TenantsRouteFactory> {

    @Override
    public Class<TenantsRouteFactory> getObjectType() {
        return TenantsRouteFactory.class;
    }

    @Override
    protected TenantsRouteFactory createInstance() throws Exception {
        org.apache.commons.digester3.binder.RulesModule rulesModule = new RulesModule();
        MonitorFileSystem monitorFileSystem = Objects.requireNonNull(getBeanFactory())
                .getBean(MonitorFileSystem.class);
        Preconditions.checkNotNull(monitorFileSystem);
        final TenantsRouteFactory factory = new TenantsRouteFactory(rulesModule, restTemplate, patterns);
        Optional<Collection<File>> files = monitorFileSystem.findFiles(patterns);
        files.ifPresent(f -> {
            File _temp = f.iterator().next();
            factory.parseFile(_temp).ifPresent(x -> factory.addConfig(_temp, x));
        });
        return factory;
    }

    private RestTemplate restTemplate;
    private List<String> patterns;

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }
}
