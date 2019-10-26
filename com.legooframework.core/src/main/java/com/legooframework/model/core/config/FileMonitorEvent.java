package com.legooframework.model.core.config;

import com.google.common.base.MoreObjects;
import org.apache.commons.vfs2.FileChangeEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.Objects;

public class FileMonitorEvent extends ApplicationEvent {

    private File file;
    private final long timetamp;

    private FileMonitorEvent(String eventName, File file) {
        super(eventName);
        this.file = file;
        this.timetamp = System.currentTimeMillis();
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
        return file;
    }

    public boolean isFileCreatedEvent() {
        return Objects.equals("fileCreatedEvent", getSource());
    }

    public boolean isFileDeletedEvent() {
        return Objects.equals("fileDeletedEvent", getSource());
    }

    public boolean isFileChangedEvent() {
        return Objects.equals("fileChangedEvent", getSource());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("source", source)
                .add("timetamp", timetamp)
                .add("file", file.getAbsolutePath())
                .toString();
    }
}
