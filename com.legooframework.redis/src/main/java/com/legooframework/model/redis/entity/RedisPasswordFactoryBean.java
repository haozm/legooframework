package com.legooframework.model.redis.entity;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.data.redis.connection.RedisPassword;

public class RedisPasswordFactoryBean extends AbstractFactoryBean<RedisPassword> {

    public RedisPasswordFactoryBean(String password) {
        this.password = password;
    }

    @Override
    public Class<RedisPassword> getObjectType() {
        return RedisPassword.class;
    }

    @Override
    protected RedisPassword createInstance() throws Exception {
        return RedisPassword.of(password);
    }

    private final String password;
}
