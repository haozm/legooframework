package com.legooframework.model.smsprovider.service;

import com.google.common.base.Strings;
import com.legooframework.model.smsprovider.entity.SMSSubAccountEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class SyncSmsDto {

    private final SMSSubAccountEntity account;

    private final String response;

    SyncSmsDto(SMSSubAccountEntity account, String response) {
        this.response = response;
        this.account = account;
    }

    public String getAccount() {
        return this.account.getUsername();
    }

    public boolean isEmpty() {
        return Strings.isNullOrEmpty(response);
    }

    public Optional<String> getResponse() {
        if (!Strings.isNullOrEmpty(response) && StringUtils.equals("no record", response) || StringUtils.startsWith(response, "error:"))
            return Optional.empty();
        return Optional.of(response);
    }
}
