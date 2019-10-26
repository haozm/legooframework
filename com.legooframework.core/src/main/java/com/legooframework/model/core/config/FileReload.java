package com.legooframework.model.core.config;

import java.io.File;

public interface FileReload {
    /**
     * 是否支持该文件
     *
     * @param file File
     * @return OOXX
     */
    boolean isSupported(File file);

    /**
     * 监听Hold 事件
     *
     * @param fileMonitorEvent FileMonitorEvent
     */
    void onFileMonitorEvent(FileMonitorEvent fileMonitorEvent);
}
