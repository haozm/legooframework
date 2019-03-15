package com.csosm.commons.vfs;

import com.google.common.eventbus.AsyncEventBus;
import org.apache.commons.vfs2.FileMonitor;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.ResourceUtils;

public class MonitorFileSystemFactory extends AbstractFactoryBean<MonitorFileSystem> {

    private static final Logger logger = LoggerFactory.getLogger(MonitorFileSystemFactory.class);

    private FileSystemManager fsManager;
    private FileMonitor fileMonitor;
    private String foldPath;
    private long delay = 5000L;

    @Override
    public Class<MonitorFileSystem> getObjectType() {
        return MonitorFileSystem.class;
    }

    @Override
    protected MonitorFileSystem createInstance() throws Exception {
        this.fsManager = VFS.getManager();
        if (logger.isInfoEnabled())
            logger.info(String.format("fileMonitor listener path is :%s", ResourceUtils.getFile(this.foldPath)
                    .getAbsolutePath()));
        FileObject configFile = this.fsManager.resolveFile(ResourceUtils.getFile(this.foldPath).getAbsolutePath());
        this.fileMonitor = new DefaultFileMonitor(new ConfigFileListener(getBeanFactory()
                .getBean("csosmAsyncEventBus", AsyncEventBus.class)));
        ((DefaultFileMonitor) this.fileMonitor).setRecursive(true);
        ((DefaultFileMonitor) this.fileMonitor).setDelay(10000L);
        this.fileMonitor.addFile(configFile);
        ((DefaultFileMonitor) this.fileMonitor).start();
        return new MonitorFileSystem(configFile);
    }

    public void shutdown() {
        ((DefaultFileMonitor) fileMonitor).stop();
        ((DefaultFileSystemManager) fsManager).close();
    }

    public void setDelay(long delay) {
        this.delay = (delay <= 1000) ? 1000L : delay;
    }

    public void setFoldPath(String foldPath) {
        this.foldPath = foldPath;
    }
}
