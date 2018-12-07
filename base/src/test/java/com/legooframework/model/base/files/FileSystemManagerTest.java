package com.legooframework.model.base.files;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.Arrays;

public class FileSystemManagerTest {

    @Test
    public void testFile() throws  Exception{
        File file = ResourceUtils.getFile(ResourceUtils.FILE_URL_PREFIX+"D:\\test\\tomcat-users.xml");
        System.out.println(file.lastModified());
    }

    public static void main(String[] args) throws Exception{
        FileSystemManager fsManager = VFS.getManager();
        FileObject listendir  = fsManager.resolveFile( "D:\\test" );
        System.out.println(Arrays.toString(fsManager.getSchemes()));
        DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener(){
            @Override
            public void fileCreated(FileChangeEvent fileChangeEvent) throws Exception {
                System.out.println(fileChangeEvent.getFile().getName());
            }

            @Override
            public void fileDeleted(FileChangeEvent fileChangeEvent) throws Exception {
                System.out.println(fileChangeEvent.getFile().getName());
            }

            @Override
            public void fileChanged(FileChangeEvent fileChangeEvent) throws Exception {
                System.out.println(fileChangeEvent.getFile().getName());
            }
        });

        fm.setRecursive(true);
        fm.addFile(listendir);
        fm.start();
        for(;;){
            try {
                Thread.currentThread().sleep(10000);
                System.out.println(System.currentTimeMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
