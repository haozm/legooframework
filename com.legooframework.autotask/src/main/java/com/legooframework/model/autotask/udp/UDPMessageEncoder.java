package com.legooframework.model.autotask.udp;

import com.google.common.base.Charsets;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPMessageEncoder extends ProtocolEncoderAdapter {

    private static final Logger logger = LoggerFactory.getLogger(UDPMessageEncoder.class);

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput output) throws Exception {
        UDPMessage udpMessage = (UDPMessage) message;
        String json_msg = WebUtils.toJson(message);
        IoBuffer buf = IoBuffer.allocate(json_msg.length()).setAutoExpand(true);
        buf.putString(json_msg, Charsets.UTF_8.newEncoder());
        buf.flip();
        output.write(buf);
    }
}
