package com.legooframework.model.autotask.udp;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DefaultDatagramSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class UDPClientEndpoint extends IoHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(UDPClientEndpoint.class);

    private final InetSocketAddress socketAddress;
    private final IoConnector connector;
    private final int keepAliveRequestInterval;


    UDPClientEndpoint(IoConnector connector, InetSocketAddress socketAddress, int keepAliveRequestInterval) {
        this.connector = connector;
        this.socketAddress = socketAddress;
        this.keepAliveRequestInterval = keepAliveRequestInterval;
    }

    void init() {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[Wakaka---------------]Connector to %s by UDP ", socketAddress));
        if (!connector.getFilterChain().contains("ProtocolCodecFilter"))
            connector.getFilterChain().addLast("ProtocolCodecFilter", new ProtocolCodecFilter(new UDPMessageCodecFactory()));
        if (!connector.getFilterChain().contains("KeepAliveFilter"))
            connector.getFilterChain().addLast("KeepAliveFilter", new KeepAliveFilter(new UDPKeepAliveMessageFactory(),
                    IdleStatus.BOTH_IDLE, KeepAliveRequestTimeoutHandler.LOG, keepAliveRequestInterval, 30));
        if (!connector.getFilterChain().contains("LoggingFilter"))
            connector.getFilterChain().addLast("LoggingFilter", new LoggingFilter());
        DefaultDatagramSessionConfig sessionConfig = (DefaultDatagramSessionConfig) connector.getSessionConfig();

        sessionConfig.setCloseOnPortUnreachable(true);
        sessionConfig.setReuseAddress(true);
        sessionConfig.setSendBufferSize(1024 * 64);
        sessionConfig.setReceiveBufferSize(1024 * 64 * 2);
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("[Wakaka---------------]UDP setCloseOnPortUnreachable = %s", true));
            logger.debug(String.format("[Wakaka---------------]UDP setReuseAddress = %s", true));
            logger.debug(String.format("[Wakaka---------------]UDP setSendBufferSize = %d", 1024 * 64));
            logger.debug(String.format("[Wakaka---------------]UDP setReceiveBufferSize = %d", 1024 * 64 * 2));
            logger.debug("[Wakaka---------------]connFuture.awaitUninterruptibly() ....");
        }
        ConnectFuture connFuture = connector.connect(socketAddress);
        connFuture.awaitUninterruptibly();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("messageReceived(%s)", message));
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("sessionCreated(%s)", session.getId()));
        UDPMessage message = UDPMessageHelper.login("svr02").toMessage();
        if (logger.isDebugEnabled())
            logger.debug(String.format("Send Login-Msg %s", message));
        session.write(message);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("sessionOpened which id is %s", session.getId()));
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        if (logger.isErrorEnabled())
            logger.error(String.format("exceptionCaught(%s)", session.getId()), cause);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("sessionClosed(%s)", session.getId()));
    }
}
