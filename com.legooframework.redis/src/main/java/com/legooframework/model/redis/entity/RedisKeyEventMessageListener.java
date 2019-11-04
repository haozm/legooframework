package com.legooframework.model.redis.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;

public class RedisKeyEventMessageListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RedisKeyEventMessageListener.class);

    public RedisKeyEventMessageListener() {
        this.serializer = RedisSerializer.string();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        logger.debug(String.format("pattern:%s, channle:%s, body:%s", serializer.deserialize(pattern),
                serializer.deserialize(message.getChannel()), serializer.deserialize(message.getBody())));
    }

    private RedisSerializer<String> serializer;
}
