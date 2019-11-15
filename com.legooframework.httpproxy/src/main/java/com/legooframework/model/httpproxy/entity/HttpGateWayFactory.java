package com.legooframework.model.httpproxy.entity;

import com.google.common.collect.Lists;
import com.legooframework.model.core.config.FileReloadSupport;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.digester3.binder.RulesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class HttpGateWayFactory extends FileReloadSupport<File> implements IGateWay {

    private static final Logger logger = LoggerFactory.getLogger(HttpGateWayFactory.class);

    private final RulesModule rulesModule;
    private final List<HttpGateWayEntity> gateWays;

    HttpGateWayFactory(List<String> patterns, RulesModule rulesModule) {
        super(patterns);
        this.rulesModule = rulesModule;
        this.gateWays = Lists.newArrayList();
    }

    @Override
    public boolean match() {
        return false;
    }

    @Override
    public String getTatget() {
        return null;
    }

    @Override
    public void addConfig(File file, File config) {
        super.addConfig(file, config);
    }

    @Override
    protected Optional<File> parseFile(File file) {
        if (!isSupported(file)) return Optional.empty();
        Digester digester = DigesterLoader.newLoader(this.rulesModule).newDigester();
        List<HttpGateWayEntity> items = Lists.newArrayList();
        try {
            digester.push(items);
            digester.parse(file);
            if (logger.isDebugEnabled())
                logger.debug(String.format("finish parse getway-rule: %s", items));
            return Optional.of(file);
        } catch (Exception e) {
            logger.error(String.format("parse file=%s has error", file), e);
        } finally {
            digester.clear();
        }
        return Optional.empty();
    }
}
