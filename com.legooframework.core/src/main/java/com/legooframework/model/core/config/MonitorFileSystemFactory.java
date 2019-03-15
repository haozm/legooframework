package com.legooframework.model.core.config;

import com.legooframework.model.core.event.MessageGateWay;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.ResourceUtils;

public class MonitorFileSystemFactory extends AbstractFactoryBean<MonitorFileSystem> {

    private static final Logger logger = LoggerFactory.getLogger(MonitorFileSystemFactory.class);

    private String root;
    private long delay = 5000L;
    private StandardFileSystemManager fileSystemManager;
    private DefaultFileMonitor fileMonitor;
    private FileObject fileObject;

    @Override
    public Class<MonitorFileSystem> getObjectType() {
        return MonitorFileSystem.class;
    }

    @Override
    protected MonitorFileSystem createInstance() throws Exception {
        this.fileSystemManager = (StandardFileSystemManager) VFS.getManager();
        if (logger.isInfoEnabled())
            logger.info(String.format("VFS-Root:%s", ResourceUtils.getFile(this.root).getAbsolutePath()));
        fileObject = this.fileSystemManager.resolveFile(String.format("file:%s", ResourceUtils.getFile(this.root)
                .getAbsolutePath()));
        // MessageGateWay messageGateWay = Objects.requireNonNull(getBeanFactory()).getBean(MessageGateWay.class);
//        this.fileMonitor = new DefaultFileMonitor(new VfsFileListener(null));
//        this.fileMonitor.setRecursive(true);
//        this.fileMonitor.setDelay(1000L * 60);
        // 增加文件修改监听
        //this.fileMonitor.addFile(fileObject);
        //this.fileMonitor.start();
        return new MonitorFileSystem(fileObject);
    }

    class VfsFileListener implements FileListener {

        MessageGateWay messageGateWay;

        VfsFileListener(MessageGateWay messageGateWay) {
            this.messageGateWay = messageGateWay;
        }

        @Override
        public void fileCreated(FileChangeEvent fileChangeEvent) throws Exception {
//            messageGateWay.send(FileMonitorEvent.fileCreatedEvent(fileChangeEvent),
//                    LoginContextHolder.getAnonymousCtx());
        }

        @Override
        public void fileDeleted(FileChangeEvent fileChangeEvent) throws Exception {
//            messageGateWay.send(FileMonitorEvent.fileDeletedEvent(fileChangeEvent),
//                    LoginContextHolder.getAnonymousCtx());
        }

        @Override
        public void fileChanged(FileChangeEvent fileChangeEvent) throws Exception {
//            messageGateWay.send(FileMonitorEvent.fileChangedEvent(fileChangeEvent),
//                    LoginContextHolder.getAnonymousCtx());
        }
    }

    public void shutdown() throws Exception {
        fileObject.close();
        fileSystemManager.close();
        //this.fileMonitor.stop();
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setRoot(String root) {
        this.root = root;
    }
}
