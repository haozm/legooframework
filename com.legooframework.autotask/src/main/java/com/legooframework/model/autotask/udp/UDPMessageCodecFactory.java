package com.legooframework.model.autotask.udp;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class UDPMessageCodecFactory implements ProtocolCodecFactory {

    private final UDPMessageEncoder encoder;

    private final UDPMessageDecoder decoder;

    UDPMessageCodecFactory() {
        encoder = new UDPMessageEncoder();
        decoder = new UDPMessageDecoder();
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }
}
