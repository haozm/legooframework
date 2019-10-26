package com.legooframework.model.autotask.udp;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPKeepAliveMessageFactory implements KeepAliveMessageFactory {

    private static final Logger logger = LoggerFactory.getLogger(UDPKeepAliveMessageFactory.class);

    @Override
    public boolean isRequest(IoSession session, Object message) {
        if (message instanceof UDPMessage) {
            UDPMessage udpMessage = (UDPMessage) message;
            return udpMessage.isHeartReqMsg();
        }
        return false;
    }

    @Override
    public boolean isResponse(IoSession session, Object message) {
        if (message instanceof UDPMessage) {
            boolean heart = ((UDPMessage) message).isHeartAckMsg();
            if (logger.isDebugEnabled())
                logger.debug(String.format("Header-message=%s", heart));
            return heart;
        }
        return false;
    }

    @Override
    public Object getRequest(IoSession session) {
        UDPMessage message = UDPMessageHelper.heart().toMessage();
        if (logger.isDebugEnabled())
            logger.debug(String.format("Header-message=%s", message));
        return message;
    }

    @Override
    public Object getResponse(IoSession session, Object message) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("Header-message=%s", message));
        return null;
    }
}
