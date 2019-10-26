package com.csosm.commons.server;

import com.csosm.commons.event.EventBusSubscribe;
import com.csosm.commons.event.FileModifiedEvent;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FileObjectMonitorServer extends AbstractBaseServer implements InitializingBean, EventBusSubscribe {

    private static final Logger logger = LoggerFactory.getLogger(FileObjectMonitorServer.class);

    private List<FileModifiedReload> reloads;

    public FileObjectMonitorServer() {
        this.reloads = Lists.newArrayList();
    }

    private Optional<FileModifiedReload> getFileModifiedReLoad(FileObject fileObject) throws IOException {
        for (FileModifiedReload $it : reloads) {
            File file = ResourceUtils.getFile(fileObject.getURL());
            if ($it.isSupportFile(file)) return Optional.of($it);
        }
        return Optional.absent();
    }

    @Subscribe
    public void subFileModifiedEvent(FileModifiedEvent event) throws Exception {
        if (logger.isDebugEnabled()) logger.debug(String.format("Subscribe Event %s", event));
        Optional<FileModifiedReload> optional = getFileModifiedReLoad(event.getFile());
        if (optional.isPresent()) {
            optional.get().building(null);
            if (logger.isDebugEnabled())
                logger.debug(String.format("%s reload", optional.get().getClass()));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, FileModifiedReload> beanMap = getAppCtx().getBeansOfType(FileModifiedReload.class);
        if (MapUtils.isNotEmpty(beanMap))
            this.reloads.addAll(beanMap.values());
    }
}
