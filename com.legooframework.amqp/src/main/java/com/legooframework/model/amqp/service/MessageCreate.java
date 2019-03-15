package com.legooframework.model.amqp.service;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;


public class MessageCreate {

    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendDataToQueue(String queueKey, Object object) {
        amqpTemplate.convertAndSend(queueKey, object);
    }
}
