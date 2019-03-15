package com.csosm.commons.vfs;

import com.csosm.commons.event.FileModifiedEvent;
import com.google.common.eventbus.AsyncEventBus;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;


class ConfigFileListener implements FileListener {

    private final AsyncEventBus asyncEventBus;

    public ConfigFileListener(AsyncEventBus asyncEventBus) {
        this.asyncEventBus = asyncEventBus;
    }

    @Override
    public void fileCreated(FileChangeEvent changeEvent) throws Exception {
        asyncEventBus.post(FileModifiedEvent.addFileEvent(changeEvent.getFile()));
    }

    @Override
    public void fileDeleted(FileChangeEvent changeEvent) throws Exception {
        asyncEventBus.post(FileModifiedEvent.addFileEvent(changeEvent.getFile()));
    }

    @Override
    public void fileChanged(FileChangeEvent changeEvent) throws Exception {
        asyncEventBus.post(FileModifiedEvent.addFileEvent(changeEvent.getFile()));
    }


}
