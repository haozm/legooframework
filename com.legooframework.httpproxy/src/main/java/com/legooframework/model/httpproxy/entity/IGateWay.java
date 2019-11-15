package com.legooframework.model.httpproxy.entity;

public interface IGateWay {

    boolean match();

    String getTatget();
}
