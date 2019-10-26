package com.legooframework.model.core.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.ResourceUtils;

import java.net.URL;

public class MonitorFileSystemFactory extends AbstractFactoryBean<MonitorFileSystem> implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(MonitorFileSystemFactory.class);

    private String root;
    private DefaultFileSystemManager fsm;
    private boolean monitor = true;
    private DefaultFileMonitor fm;
    private FileObject fs;

    @Override
    public Class<MonitorFileSystem> getObjectType() {
        return MonitorFileSystem.class;
    }

    @Override
    protected MonitorFileSystem createInstance() throws Exception {
        fsm = new DefaultFileSystemManager();
        fsm.addProvider("file", new DefaultLocalFileProvider());
        fsm.setCacheStrategy(CacheStrategy.ON_CALL);
        fsm.init();
        if (Strings.isNullOrEmpty(root)) {
            URL url = this.getClass().getClassLoader().getResource("META-INF/core/spring-model-cfg.xml");
            String file_path = url.toString();
            if (StringUtils.contains(file_path, "WEB-INF")) {
                file_path = StringUtils.substringBefore(file_path, "WEB-INF");
                this.root = String.format("%sresources", file_path);
            } else if (StringUtils.contains(file_path, "production")) {
                file_path = StringUtils.substringBefore(file_path, "production");
                this.root = file_path;
            }
        }
        Preconditions.checkState(!Strings.isNullOrEmpty(this.root), "配置文件加载路径不可以为空值...");
        if (logger.isDebugEnabled())
            logger.debug(ResourceUtils.getURL(this.root).toString());
        fs = fsm.resolveFile(ResourceUtils.getURL(this.root));
        if (monitor) {
            this.fm = new DefaultFileMonitor(new VfsFileListener(this.publisher));
            this.fm.setRecursive(true);
            this.fm.setDelay(1000L * 10);
            this.fm.addFile(this.fs);
            this.fm.start();
        }
        return new MonitorFileSystem(fs);
    }

    public void shutdown() throws Exception {
        if (monitor) this.fm.stop();
        fs.close();
        fsm.close();
        if (logger.isDebugEnabled())
            logger.debug(String.format("Close VFS:%s", this.root));
    }

    class VfsFileListener implements FileListener {

        private final ApplicationEventPublisher publisher;

        VfsFileListener(ApplicationEventPublisher publisher) {
            this.publisher = publisher;
        }

        @Override
        public void fileCreated(FileChangeEvent fileChangeEvent) throws Exception {
            publisher.publishEvent(FileMonitorEvent.fileCreatedEvent(fileChangeEvent));
            if (logger.isDebugEnabled())
                logger.debug(String.format("fileCreated:%s", fileChangeEvent));
        }

        @Override
        public void fileDeleted(FileChangeEvent fileChangeEvent) throws Exception {
            publisher.publishEvent(FileMonitorEvent.fileDeletedEvent(fileChangeEvent));
            if (logger.isDebugEnabled())
                logger.debug(String.format("fileDeleted:%s", fileChangeEvent));
        }

        @Override
        public void fileChanged(FileChangeEvent fileChangeEvent) throws Exception {
            publisher.publishEvent(FileMonitorEvent.fileChangedEvent(fileChangeEvent));
            if (logger.isDebugEnabled())
                logger.debug(String.format("fileChanged:%s", fileChangeEvent));
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    private ApplicationEventPublisher publisher;

    public void setMonitor(boolean monitor) {
        this.monitor = monitor;
    }

    public void setRoot(String root) {
        this.root = root;
    }
}
