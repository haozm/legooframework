package com.legooframework.model.amqp.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class MemageListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        System.out.println(new String(message.getBody()));
    }
}
