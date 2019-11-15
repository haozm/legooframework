package com.legooframework.model.httpproxy.entity;

import org.springframework.web.util.UriComponents;

public interface IGateWay {

    boolean match(UriComponents originalUri);

    String getTatget(UriComponents originalUri);
}
