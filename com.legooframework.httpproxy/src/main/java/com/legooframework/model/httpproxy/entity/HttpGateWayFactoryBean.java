package com.legooframework.model.httpproxy.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.config.FileMonitorEvent;
import com.legooframework.model.core.config.MonitorFileSystem;
import com.legooframework.model.httpproxy.entity.rules.RulesModule;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationListener;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class HttpGateWayFactoryBean extends AbstractFactoryBean<HttpGateWayFactory>
        implements ApplicationListener<FileMonitorEvent> {

    private HttpGateWayFactory httpGateWayFactory;

    @Override
    public Class<HttpGateWayFactory> getObjectType() {
        return HttpGateWayFactory.class;
    }

    @Override
    protected HttpGateWayFactory createInstance() throws Exception {
        org.apache.commons.digester3.binder.RulesModule rulesModule = new RulesModule();
        MonitorFileSystem monitorFileSystem = Objects.requireNonNull(getBeanFactory())
                .getBean(MonitorFileSystem.class);
        Preconditions.checkNotNull(monitorFileSystem);
        this.httpGateWayFactory = new HttpGateWayFactory(patterns, rulesModule);
        Optional<Collection<File>> files = monitorFileSystem.findFiles(patterns);
        files.ifPresent(f -> f.forEach(x -> {
            this.httpGateWayFactory.parseFile(x).ifPresent(c -> this.httpGateWayFactory.addConfig(x, c));
        }));
        return this.httpGateWayFactory;
    }

    private List<String> patterns;

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    @Override
    public void onApplicationEvent(FileMonitorEvent fileMonitorEvent) {
        httpGateWayFactory.onFileMonitorEvent(fileMonitorEvent);
    }
}
