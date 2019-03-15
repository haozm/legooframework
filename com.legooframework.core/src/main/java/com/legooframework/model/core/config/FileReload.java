package com.legooframework.model.core.config;

import java.io.File;

public interface FileReload {

    boolean isSupported(File file);

    void handleileMonitorEvent(FileMonitorEvent monitorEvent);
}
