package com.legooframework.model.httpproxy.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.MimeType;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
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

    @SuppressWarnings("unchecked")
    public Optional<Integer> getUserId() {
        String userId = this.uriComponents.getQueryParams().getFirst("userId");
        if (Strings.isNullOrEmpty(userId) && this.body instanceof Map) {
            userId = MapUtils.getString((Map<? super Object, ?>) this.body, "userId");
        }
        if (!Strings.isNullOrEmpty(userId))
            Preconditions.checkState(NumberUtils.isDigits(userId), "非法登录用户ID...");
        return Strings.isNullOrEmpty(userId) ? Optional.empty() : Optional.of(Integer.parseInt(userId));
    }

    public boolean isPost() {
        return StringUtils.equals(requestMethod, "POST");
    }

    public boolean isGet() {
        return StringUtils.equals(requestMethod, "GET");
    }

    UriComponents getUriComponents() {
        return uriComponents;
    }

    public Optional<Object> getBody() {
        return Optional.ofNullable(body);
    }

    private boolean hasQueryParam(String param) {
        String value = this.uriComponents.getQueryParams().getFirst(param);
        return !Strings.isNullOrEmpty(Strings.emptyToNull(value));
    }

    public boolean hasQueryMod() {
        return hasQueryParam("mod");
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
