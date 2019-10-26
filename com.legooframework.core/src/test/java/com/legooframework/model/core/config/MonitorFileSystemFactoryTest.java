package com.legooframework.model.core.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.junit.Test;

import java.net.URL;

public class MonitorFileSystemFactoryTest {
    // StandardFileSystemManager
    @Test
    public void createInstance() throws Exception {
//        StandardFileSystemManager fileSystemManager = (StandardFileSystemManager) VFS.getManager();
//        DefaultLocalFileProvider fileProvider = new DefaultLocalFileProvider();
//        fileSystemManager.setDefaultProvider(fileProvider);
//        FileObject fileObject = fileSystemManager.resolveFile("file://" + "C:\\workspace\\service\\apps\\resources\\");
//        System.out.println(fileObject.isFolder());
//        fileObject.close();
        URL url = this.getClass().getClassLoader().getResource("META-INF/core/spring-model-cfg.xml");
        String url_str = url.toString();
        String sub_url = StringUtils.substringBefore(url_str, "META-INF");
        System.out.println(sub_url);
//        DefaultFileSystemManager fileSystemManager = new DefaultFileSystemManager();
//        DefaultLocalFileProvider fileProvider = new DefaultLocalFileProvider();
//        fileSystemManager.setDefaultProvider(fileProvider);
//        fileSystemManager.init();
//        FileObject fileObject = fileSystemManager.resolveFile("file://"+"C:\\workspace\\service\\apps\\resources\\");
//        System.out.println(fileObject.isFolder());
//
//        fileObject.close();


    }
}