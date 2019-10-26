package com.csosm.module.webocx.entity;

import com.csosm.commons.vfs.MonitorFileSystem;
import com.csosm.module.webocx.entity.rules.RulesModule;
import com.google.common.base.Optional;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.File;
import java.util.List;

public class LegooWebOcxRepositoryFactory extends AbstractFactoryBean<LegooWebOcxRepository> {

    @Override
    public Class<LegooWebOcxRepository> getObjectType() {
        return LegooWebOcxRepository.class;
    }

    @Override
    protected LegooWebOcxRepository createInstance() throws Exception {
        RulesModule rulesModule = new RulesModule();
        MonitorFileSystem monitorFileSystem = getBeanFactory().getBean("crmMonitorFileSystem", MonitorFileSystem.class);
        Optional<List<File>> files_opt = monitorFileSystem.findFiles(patterns);
        LegooWebOcxRepository repository = new LegooWebOcxRepository(rulesModule, patterns, files_opt.isPresent() ? files_opt.get() : null);
        if (files_opt.isPresent()) repository.building(files_opt.get());
        return repository;
    }

    private String patterns;

    public void setPatterns(String patterns) {
        this.patterns = patterns;
    }
}
