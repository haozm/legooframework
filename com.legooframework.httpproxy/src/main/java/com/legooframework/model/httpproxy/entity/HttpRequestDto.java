package com.legooframework.model.httpproxy.entity;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.MimeType;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.UUID;

public class HttpRequestDto {

    private final MimeType contentType;
    private final UUID msgId;
    private final String requestMethod, uri;
    private final UriComponents uriComponents;
    private final Object body;

    public HttpRequestDto(Message<?> http_message) {
        MessageHeaderAccessor headerAccessor = MessageHeaderAccessor.getMutableAccessor(http_message);
        this.msgId = headerAccessor.getId();
        this.contentType = headerAccessor.getContentType();
        this.uri = String.valueOf(headerAccessor.getHeader("http_requestUrl"));
        this.requestMethod = String.valueOf(headerAccessor.getHeader("http_requestMethod"));
        this.uriComponents = UriComponentsBuilder.fromHttpUrl(this.uri).build();
        this.body = http_message.getPayload();
    }

    String getUri() {
        return uri;
    }

    public boolean isPost() {
        return StringUtils.equals(requestMethod, "POST");
    }

    UriComponents getUriComponents() {
        return uriComponents;
    }

    public Optional<Object> getBody() {
        return Optional.ofNullable(body);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("msgId", msgId)
                .add("contentType", contentType)
                .add("requestMethod", requestMethod)
                .add("uri", uri)
                .toString();
    }
}
