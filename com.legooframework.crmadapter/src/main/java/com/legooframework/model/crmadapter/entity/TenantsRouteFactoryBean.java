package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.config.MonitorFileSystem;
import com.legooframework.model.crmadapter.entity.rules.RulesModule;
import org.springframework.beans.factory.config.AbstractFactoryBean;

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
        TenantsRouteFactory factory = new TenantsRouteFactory(rulesModule, patterns);
        Optional<Collection<File>> files = monitorFileSystem.findFiles(patterns);
        files.ifPresent(f -> f.forEach(factory::build));
        return factory;
    }

    private List<String> patterns;

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }
}
