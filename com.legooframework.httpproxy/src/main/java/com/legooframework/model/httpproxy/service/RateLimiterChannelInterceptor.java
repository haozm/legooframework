package com.legooframework.model.httpproxy.service;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

public class RateLimiterChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterChannelInterceptor.class);
    @SuppressWarnings("UnstableApiUsage")
    private final RateLimiter rateLimiter;

    public RateLimiterChannelInterceptor(double permitsPerSecond) {
        Preconditions.checkArgument(permitsPerSecond > 0);
        this.permitsPerSecond = permitsPerSecond;
        //noinspection UnstableApiUsage
        this.rateLimiter = RateLimiter.create(permitsPerSecond);
        if (logger.isInfoEnabled())
            logger.info(String.format("当前网关的限流为 %f", permitsPerSecond));
    }

    @Override
    public Message<?> preSend(@SuppressWarnings("NullableProblems") Message<?> message, MessageChannel channel) {
        //noinspection UnstableApiUsage
        if (rateLimiter.tryAcquire()) return message;
        IllegalStateException exception = new IllegalStateException(String.format("当前请求超过最大阈值%f，需限流控制...",
                permitsPerSecond));
        logger.error("请求达上线，需限流控制...", exception);
        return MessageBuilder.withPayload(exception).copyHeaders(message.getHeaders()).build();
    }

    private final double permitsPerSecond;

}
