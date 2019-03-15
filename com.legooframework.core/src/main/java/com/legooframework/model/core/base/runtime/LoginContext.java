package com.legooframework.model.core.base.runtime;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LoginContext extends UserDetails {
    // 用户ID
    Long getLoginId();

    Long getTenantId();

    boolean isAnonymous();

    boolean exitsRoles(String... roleNos);

    Map<String, Object> toParams();

    boolean isStoreManager();

    String getLoginName();

    Integer getStoreId();

}
