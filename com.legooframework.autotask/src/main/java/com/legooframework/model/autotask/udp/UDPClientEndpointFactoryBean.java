package com.legooframework.model.autotask.udp;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.net.InetSocketAddress;

public class UDPClientEndpointFactoryBean extends AbstractFactoryBean<UDPClientEndpoint> {

    private int keepAliveRequestInterval = 120, port = 62280;
    private String hostname = "etl.csosm.com";

    public void setKeepAliveRequestInterval(int keepAliveRequestInterval) {
        this.keepAliveRequestInterval = keepAliveRequestInterval;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public Class<UDPClientEndpoint> getObjectType() {
        return UDPClientEndpoint.class;
    }

    @Override
    protected UDPClientEndpoint createInstance() throws Exception {
        IoConnector connector = new NioDatagramConnector();
        InetSocketAddress socketAddress = new InetSocketAddress(this.hostname, this.port);
        UDPClientEndpoint endpoint = new UDPClientEndpoint(connector, socketAddress, keepAliveRequestInterval);
        connector.setHandler(endpoint);
        endpoint.init();
        return endpoint;
    }

    public static void main(String[] args) {
        IoConnector connector = new NioDatagramConnector();
        UDPClientEndpoint endpoint = new UDPClientEndpoint(connector, new InetSocketAddress("etl.csosm.com", 62280), 30);
        connector.setHandler(endpoint);
        endpoint.init();
    }
}
