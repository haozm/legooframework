package com.legooframework.model.core.config;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.event.LegooEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileChangeEvent;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.Optional;

public class FileMonitorEvent extends LegooEvent {

    public FileMonitorEvent(String eventName, File file) {
        super("core", eventName);
        super.putPayload("file", file);
    }

    static FileMonitorEvent fileCreatedEvent(FileChangeEvent changeEvent) throws Exception {
        return new FileMonitorEvent("fileCreatedEvent",
                ResourceUtils.getFile(changeEvent.getFile().getURL()));
    }

    static FileMonitorEvent fileDeletedEvent(FileChangeEvent changeEvent) throws Exception {
        return new FileMonitorEvent("fileDeletedEvent",
                ResourceUtils.getFile(changeEvent.getFile().getURL()));
    }

    static FileMonitorEvent fileChangedEvent(FileChangeEvent changeEvent) throws Exception {
        return new FileMonitorEvent("fileChangedEvent",
                ResourceUtils.getFile(changeEvent.getFile().getURL()));
    }

    public File getFile() {
        Optional<File> file = getValue("file", File.class);
        Preconditions.checkState(file.isPresent(), "系统异常:监听文件事件中，修改的文件不存在。");
        return file.get();
    }


    public boolean isFileCreatedEvent() {
        return StringUtils.equals("fileCreatedEvent", getEventName());
    }

    public boolean isFileDeletedEvent() {
        return StringUtils.equals("fileDeletedEvent", getEventName());
    }

    public boolean isFileChangedEvent() {
        return StringUtils.equals("fileChangedEvent", getEventName());
    }

}
