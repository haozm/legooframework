package com.legooframework.model.httpproxy.entity;

import com.legooframework.model.core.base.exception.BaseException;

public class FusingTimeOutException extends BaseException {

    public FusingTimeOutException(HttpGateWayParams gateWayParams) {
        super("4004", String.format("Post Url %s Timeout %d", gateWayParams.getTarget(), gateWayParams.getTimeout()));
    }
}
