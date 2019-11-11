package com.legooframework.model.smsprovider.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class SyncSmsDto {

    private final String response;

    SyncSmsDto(String response) {
        this.response = response;
    }

    public boolean hasContext() {
        return !Strings.isNullOrEmpty(response);
    }

    public boolean isError() {
        return !Strings.isNullOrEmpty(response) && StringUtils.startsWith(response, "error:");
    }

    public Optional<String> getResponse() {
        if (!Strings.isNullOrEmpty(response) && StringUtils.equals("no record", response) || StringUtils.startsWith(response, "error:"))
            return Optional.empty();
        return Optional.of(response);
    }
}
