package com.legooframework.model.core.base.runtime;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public interface LoginContext extends UserDetails {
    // 用户ID
    Long getLoginId();

    Long getTenantId();

    boolean isAnonymous();

    boolean exitsRoles(String... roleNos);

    Map<String, Object> toParams();

    boolean isStoreManager();

    boolean isShoppingGuide();

    boolean isManager();

    boolean isRegediter();

    boolean isBoss();

    boolean isAreaManagerRole();

    String getLoginName();

    Integer getStoreId();

    Optional<Collection<Integer>> getStoreIds();
}
