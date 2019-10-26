package com.legooframework.model.crmadapter.service;

import org.springframework.security.core.AuthenticationException;

public class TokenNotExitsException extends AuthenticationException {

    public TokenNotExitsException(String msg) {
        super(msg);
    }
}
