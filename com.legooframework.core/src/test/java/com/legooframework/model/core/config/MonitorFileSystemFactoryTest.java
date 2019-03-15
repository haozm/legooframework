package com.legooframework.model.core.config;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.FileProvider;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

public class MonitorFileSystemFactoryTest {

    @Test
    public void createInstance() throws Exception {
        StandardFileSystemManager fileSystemManager = (StandardFileSystemManager) VFS.getManager();
//        FileProvider fileProvider = new UrlFileProvider();
//        fileSystemManager.addProvider("file:",fileProvider);
//        fileSystemManager.setBaseFile(ResourceUtils.getFile("file:C:\\workspace\\service\\apache-tomcat-9.0.13\\shared\\resources"));
        // fileSystemManager.init();
        FileObject fileObject = fileSystemManager.resolveFile("file:C:\\workspace\\service\\apache-tomcat-9.0.13\\shared\\resources\\META-INF\\");
        System.out.println(fileObject);
    }
}