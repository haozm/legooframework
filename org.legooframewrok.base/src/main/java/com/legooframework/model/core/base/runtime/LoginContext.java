package com.legooframework.model.core.base.runtime;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public interface LoginContext extends UserDetails {
    // 用户ID
    Long getLoginId();

    // 账号ID
    Long getAccountId();

    String getAccountNo();

    String getDeviceNo();

    Long getTenantId();

    boolean isAnonymous();

    boolean exitsRoles(String... roleNos);

    Optional<List<LegooRole>> getRoles();

    Optional<String> getToken();

    Date getLoginTime();

    Optional<List<Long>> getStoreIds();

    Optional<Long> getOnlyOneStoreId();

    Optional<List<LegooOrg>> getStores();

    Map<String, Object> toParams();

    boolean equalsUser(LoginContext o);

    Optional<Collection<String>> getDeviceId();

}
