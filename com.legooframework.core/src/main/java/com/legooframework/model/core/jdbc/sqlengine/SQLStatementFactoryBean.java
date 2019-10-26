package com.legooframework.model.core.jdbc.sqlengine;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.config.FileMonitorEvent;
import com.legooframework.model.core.config.MonitorFileSystem;
import com.legooframework.model.core.jdbc.sqlengine.rules.RulesModule;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationListener;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SQLStatementFactoryBean extends AbstractFactoryBean<SQLStatementFactory> implements ApplicationListener<FileMonitorEvent> {

    @Override
    public Class<SQLStatementFactory> getObjectType() {
        return SQLStatementFactory.class;
    }

    @Override
    protected SQLStatementFactory createInstance() throws Exception {
        org.apache.commons.digester3.binder.RulesModule rulesModule = new RulesModule();
        Configuration configuration = getConfiguration();
        MonitorFileSystem monitorFileSystem = Objects.requireNonNull(getBeanFactory())
                .getBean(MonitorFileSystem.class);
        Preconditions.checkNotNull(monitorFileSystem);
        statementFactory = new SQLStatementFactory(configuration, rulesModule, patterns);
        Optional<Collection<File>> files = monitorFileSystem.findFiles(patterns);
        files.ifPresent(f -> f.forEach(x -> {
            statementFactory.parseFile(x).ifPresent(c -> statementFactory.addConfig(x, c));
        }));
        return statementFactory;
    }

    private SQLStatementFactory statementFactory;

    private Configuration getConfiguration() {
        Configuration _configuration = new Configuration(Configuration.VERSION_2_3_22);
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        _configuration.setTemplateLoader(stringTemplateLoader);
        _configuration.setNumberFormat("#");
        _configuration.setClassicCompatible(true);
        return _configuration;
    }

    private List<String> patterns;

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    @Override
    public void onApplicationEvent(FileMonitorEvent fileMonitorEvent) {
        statementFactory.onFileMonitorEvent(fileMonitorEvent);
    }
}
