package com.csosm.commons.server;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CsosmExecutorServiceFactory extends AbstractFactoryBean<ListeningExecutorService> {

    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public Class<ListeningExecutorService> getObjectType() {
        return ListeningExecutorService.class;
    }

    @Override
    protected ListeningExecutorService createInstance() throws Exception {
        this.threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(64));
        return MoreExecutors.listeningDecorator(threadPoolExecutor);
    }

    private int corePoolSize = 4;
    private int maximumPoolSize = 64;

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public void shutdown() {
        threadPoolExecutor.shutdown();
    }

}
