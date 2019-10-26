package com.csosm.commons.vfs;

import com.google.common.eventbus.AsyncEventBus;
import org.apache.commons.vfs2.FileMonitor;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.ResourceUtils;

public class MonitorFileSystemFactory extends AbstractFactoryBean<MonitorFileSystem> {

    private static final Logger logger = LoggerFactory.getLogger(MonitorFileSystemFactory.class);

    private DefaultFileSystemManager fileSystemManager;
    private FileMonitor fileMonitor;
    private String foldPath;
    private FileObject fileObject;
    private long delay = 5000L;

    @Override
    public Class<MonitorFileSystem> getObjectType() {
        return MonitorFileSystem.class;
    }

    @Override
    protected MonitorFileSystem createInstance() throws Exception {
        fileSystemManager = new DefaultFileSystemManager();
        fileSystemManager.setDefaultProvider(new DefaultLocalFileProvider());
        fileSystemManager.init();
        String file_boot = String.format("file://%s", ResourceUtils.getFile(this.foldPath).getAbsolutePath());
        if (logger.isInfoEnabled()) logger.info(file_boot);
        fileObject = fileSystemManager.resolveFile(file_boot);
        this.fileMonitor = new DefaultFileMonitor(new ConfigFileListener(getBeanFactory()
                .getBean("csosmAsyncEventBus", AsyncEventBus.class)));
        ((DefaultFileMonitor) this.fileMonitor).setRecursive(true);
        ((DefaultFileMonitor) this.fileMonitor).setDelay(10000L);
        this.fileMonitor.addFile(fileObject);
        ((DefaultFileMonitor) this.fileMonitor).start();
        return new MonitorFileSystem(fileObject);
    }

    public void shutdown() throws Exception {
        fileObject.close();
        ((DefaultFileMonitor) fileMonitor).stop();
        fileSystemManager.close();
    }

    public void setDelay(long delay) {
        this.delay = (delay <= 1000) ? 5000L : delay;
    }

    public void setFoldPath(String foldPath) {
        this.foldPath = foldPath;
    }
}
