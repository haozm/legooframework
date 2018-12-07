package com.legooframework.model.integration;

import com.google.common.base.Preconditions;
import com.legooframework.model.base.runtime.LoginContext;
import com.legooframework.model.base.runtime.LoginContextHolder;
import com.legooframework.model.event.LegooEvent;
import com.legooframework.model.event.MessageGateWay;
import com.legooframework.model.event.MessageHelper;
import com.legooframework.model.osgi.Bundle;
import com.legooframework.model.osgi.BundleRuntimeFactory;
import com.legooframework.model.utils.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Optional;

public class LegooMessageGateWay implements MessageGateWay, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(LegooMessageGateWay.class);

    private MessagingTemplate template;

    public LegooMessageGateWay(MessagingTemplate template) {
        this.template = template;
    }

    @Override
    public void postEvent(LegooEvent event) {
        LoginContext loginContext = LoginContextHolder.get();
        if (logger.isDebugEnabled())
            logger.debug(String.format("postEvent(%s,%s)", SYNC_EVENTBUS, event));
        template.send(SYNC_EVENTBUS, event.toMessage(loginContext));
    }

    @Override
    public void send(LegooEvent event) throws Exception {
        LoginContext loginContext = LoginContextHolder.get();
        try {
            Message<LegooEvent> request = event.toMessage(loginContext);
            // 熔断机制
            Collection<Bundle> bundles = getBundleFactory().getBundles();
            Optional<String> channel = Optional.empty();
            for (Bundle $it : bundles) {
                channel = $it.getChannelByEvent(event);
                if (channel.isPresent()) break;
            }
            Preconditions.checkState(channel.isPresent(), "目前无Bundle支持该事件 %s", event);
            if (logger.isTraceEnabled())
                logger.trace(String.format("send(%s,%s)", channel.get(), event));
            template.send(channel.get(), request);
        } catch (Exception e) {
            throw ExceptionUtil.handleException(e, String.format("send(%s,...) has error", event), logger);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> sendAndReceive(LegooEvent event, Class<T> clazz) throws Exception {
        LoginContext loginContext = LoginContextHolder.get();
        try {
            Message<LegooEvent> request = event.toMessage(loginContext);
            // 熔断机制
            Collection<Bundle> bundles = getBundleFactory().getBundles();
            Optional<String> channel = Optional.empty();
            for (Bundle $it : bundles) {
                channel = $it.getChannelByEvent(event);
                if (channel.isPresent()) break;
            }
            Preconditions.checkState(channel.isPresent(), "目前无Bundle支持该事件 %s", event);
            Message<?> receive = template.sendAndReceive(channel.get(), request);
            Preconditions.checkNotNull(receive, "sendAndReceive(....)  无返回或者返回超时...");
            // 如果出现错误 则直接还原异常
            if (MessageHelper.hasException(receive)) throw (Exception) receive.getPayload();
            Object payload = receive.getPayload();
            Preconditions.checkNotNull(payload, "消息返回不可以为空.");

            if (payload instanceof Optional) {
                Optional<Object> opt = (Optional<Object>) payload;
                if (!opt.isPresent()) return Optional.empty();
                Assert.isInstanceOf(clazz, opt.get(), String.format("返回值类型%s与期望值%s不匹配.", opt.get(), clazz));
                return (Optional<T>) payload;
            } else {
                Assert.isInstanceOf(clazz, payload, String.format("返回值类型%s与期望值%s不匹配.", payload, clazz));
                return Optional.of((T) payload);
            }
        } catch (Exception e) {
            throw ExceptionUtil.handleException(e, String.format("sendAndReceive(%s,...) has error", event), logger);
        }
    }

    BundleRuntimeFactory getBundleFactory() {
        return this.appCtx.getBean(BundleRuntimeFactory.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }

    private final String ASYNC_EVENTBUS = "asyncEventBus";
    private final String SYNC_EVENTBUS = "syncEventBus";

    private ApplicationContext appCtx;

}
