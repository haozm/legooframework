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
        MonitorFileSystem monitorFileSystem = getBeanFactory().getBean(MonitorFileSystem.class);
        Optional<List<File>> files_opt = monitorFileSystem.findFiles(patterns);
        if (files_opt.isPresent()) {
            LegooWebOcxRepository repository = new LegooWebOcxRepository(rulesModule, files_opt.get());
            repository.init();
            return repository;
        }
        return null;
    }

    private String patterns;

    public void setPatterns(String patterns) {
        this.patterns = patterns;
    }
}
