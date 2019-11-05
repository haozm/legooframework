package com.legooframework.model.jwtoken.service;

import org.springframework.security.core.AuthenticationException;

public class TokenNotExitsException extends AuthenticationException {

    public TokenNotExitsException(String msg) {
        super(msg);
    }
}
