package com.csosm.commons.server;

import java.io.File;
import java.util.Collection;

/**
 * 监听配置文件变化 并重新加载接口申明
 */
public interface FileModifiedReload {

    boolean isSupportFile(File file);

    void building(Collection<File> files);

}
