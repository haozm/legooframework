package com.legooframework.model.vfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class FileReloadSupport<T> implements FileReload {

    private final String[] patterns;
    private final Map<String, T> fileMaps;

    protected FileReloadSupport(List<String> patterns) {
        this.patterns = patterns.toArray(new String[0]);
        this.fileMaps = Maps.newConcurrentMap();
    }

    protected Optional<T> getByFile(File file) {
        Preconditions.checkState(file.exists());
        return Optional.ofNullable(fileMaps.get(file.getAbsolutePath()));
    }

    protected boolean exitsByFile(File file) {
        Preconditions.checkState(file.exists());
        return fileMaps.containsKey(file.getAbsolutePath());
    }

    protected Optional<Collection<T>> getConfigs() {
        if (MapUtils.isEmpty(fileMaps)) return Optional.empty();
        return Optional.of(fileMaps.values());
    }

    protected void addConfig(File file, T config) {
        Preconditions.checkState(!exitsByFile(file),
                "已经存在%s对应的配置项,无法新增.", file.getAbsolutePath());
        Preconditions.checkNotNull(config, "配置项不可以为空.");
        fileMaps.put(file.getAbsolutePath(), config);
    }

    protected void removeConfig(File file) {
        fileMaps.remove(file.getAbsolutePath());
    }

    protected void updateConfig(File file, T config) {
        Preconditions.checkNotNull(config, "配置项不可以为空.");
        fileMaps.remove(file.getAbsolutePath());
        fileMaps.put(file.getAbsolutePath(), config);
    }

    @Override
    public boolean isSupported(File file) {
        String file_path = file.getAbsolutePath();
        return PathMatcherUtil.match(patterns, file_path);
    }

}
