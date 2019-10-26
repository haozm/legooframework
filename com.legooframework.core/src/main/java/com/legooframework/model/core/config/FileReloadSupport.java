package com.legooframework.model.core.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class FileReloadSupport<T> implements FileReload {

    private static final Logger logger = LoggerFactory.getLogger(FileReloadSupport.class);

    private final String[] patterns;
    private final Map<String, T> fileMaps;

    protected FileReloadSupport(List<String> patterns) {
        this.patterns = patterns.toArray(new String[0]);
        this.fileMaps = Maps.newConcurrentMap();
    }

    private boolean exitsByFile(File file) {
        Preconditions.checkState(file.exists());
        return fileMaps.containsKey(file.getAbsolutePath());
    }

    protected Optional<Collection<T>> getConfigs() {
        if (MapUtils.isEmpty(fileMaps)) return Optional.empty();
        return Optional.of(fileMaps.values());
    }

    protected void addConfig(File file, T config) {
        Preconditions.checkState(!exitsByFile(file), "已经存在%s对应的配置项,无法新增.", file.getAbsolutePath());
        Preconditions.checkNotNull(config, "配置项不可以为空.");
        fileMaps.put(file.getAbsolutePath(), config);
    }

    void removeConfig(File file) {
        fileMaps.remove(file.getAbsolutePath());
    }

    private void updateConfig(File file, T config) {
        Preconditions.checkNotNull(config, "配置项不可以为空.");
        fileMaps.remove(file.getAbsolutePath());
        fileMaps.put(file.getAbsolutePath(), config);
    }

    @Override
    public boolean isSupported(File file) {
        String file_path = file.getAbsolutePath();
        return PathMatcherUtil.match(patterns, file_path);
    }

    @Override
    public void onFileMonitorEvent(FileMonitorEvent fileMonitorEvent) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("Application Listener Event:%s", fileMonitorEvent));
        File listerFile = fileMonitorEvent.getFile();
        if (isSupported(listerFile)) {
            if (fileMonitorEvent.isFileCreatedEvent()) {
                try {
                    Optional<T> optional = parseFile(listerFile);
                    optional.ifPresent(x -> addConfig(listerFile, x));
                } catch (Exception e) {
                    logger.debug(String.format("Parse File:%s has error", listerFile.getAbsolutePath()), e);
                }
            } else if (fileMonitorEvent.isFileChangedEvent()) {
                try {
                    Optional<T> optional = parseFile(listerFile);
                    optional.ifPresent(x -> updateConfig(listerFile, x));
                } catch (Exception e) {
                    logger.debug(String.format("Parse File:%s has error", listerFile.getAbsolutePath()), e);
                }
            } else if (fileMonitorEvent.isFileDeletedEvent()) {
                removeConfig(listerFile);
            }
        }
    }

    protected abstract Optional<T> parseFile(File file);
}
