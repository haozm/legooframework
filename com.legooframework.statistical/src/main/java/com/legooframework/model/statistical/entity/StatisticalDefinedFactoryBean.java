package com.legooframework.model.statistical.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.config.FileMonitorEvent;
import com.legooframework.model.core.config.MonitorFileSystem;
import com.legooframework.model.statistical.entity.rules.RulesModule;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationListener;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StatisticalDefinedFactoryBean extends AbstractFactoryBean<StatisticalDefinedFactory>
        implements ApplicationListener<FileMonitorEvent> {

    @Override
    public Class<StatisticalDefinedFactory> getObjectType() {
        return StatisticalDefinedFactory.class;
    }

    @Override
    protected StatisticalDefinedFactory createInstance() throws Exception {
        RulesModule rulesModule = new RulesModule();
        MonitorFileSystem monitorFileSystem = Objects.requireNonNull(getBeanFactory())
                .getBean(MonitorFileSystem.class);
        Preconditions.checkNotNull(monitorFileSystem);
        this.factory = new StatisticalDefinedFactory(this.patterns, rulesModule);
        Optional<Collection<File>> files = monitorFileSystem.findFiles(patterns);
        files.ifPresent(f -> f.forEach(x -> {
            this.factory.parseFile(x).ifPresent(c -> this.factory.addConfig(x, c));
        }));
        return factory;
    }

    private StatisticalDefinedFactory factory;
    private List<String> patterns;

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    @Override
    public void onApplicationEvent(FileMonitorEvent fileMonitorEvent) {
        factory.onFileMonitorEvent(fileMonitorEvent);
    }
}
