package com.csosm.module.menu.entity;

import com.csosm.commons.vfs.MonitorFileSystem;
import com.google.common.base.Preconditions;
import com.csosm.module.menu.entity.rules.ResRulesModule;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class ResourcesFactoryBean extends AbstractFactoryBean<ResourcesFactory> {

    @Override
    public Class<ResourcesFactory> getObjectType() {
        return ResourcesFactory.class;
    }

    @Override
    protected ResourcesFactory createInstance() throws Exception {
        MonitorFileSystem monitorFileSystem = getBeanFactory().getBean("crmMonitorFileSystem", MonitorFileSystem.class);
        Preconditions.checkNotNull(monitorFileSystem);
        ResourcesFactory factory = new ResourcesFactory(new ResRulesModule(), monitorFileSystem, pattern);
        //  Optional<List<File>> files = monitorFileSystem.findFiles(pattern);
        factory.building(null);
        return factory;
    }

    private String pattern;

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
