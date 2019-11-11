package com.legooframework.model.smsprovider.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.smsprovider.entity.SMSSubAccountEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class SendedSmsDto {
    private final SMSSubAccountEntity account;
    private final String response;

    SendedSmsDto(SMSSubAccountEntity account, String response) {
        this.account = account;
        this.response = response;
    }

    public String getAccount() {
        return this.account.getUsername();
    }

    public String getSmsSendId() {
        Preconditions.checkState(isSuccess());
        return response.substring(8);
    }

    public boolean isSuccess() {
        return !Strings.isNullOrEmpty(response) && StringUtils.startsWith(response, "success:");
    }

    public boolean isError() {
        return !Strings.isNullOrEmpty(response) && StringUtils.startsWith(response, "error:");
    }


    public String getExitsRespons() {
        Preconditions.checkState(!Strings.isNullOrEmpty(response), "网关返回报文为空...账户信息...%s", account);
        return this.response;
    }


    public Optional<String> getResponse() {
        return Optional.ofNullable(response);
    }
}
