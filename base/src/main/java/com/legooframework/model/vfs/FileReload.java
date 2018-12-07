package com.legooframework.model.vfs;

import java.io.File;

public interface FileReload {

    boolean isSupported(File file);

    void handleileMonitorEvent(FileMonitorEvent monitorEvent);
}
