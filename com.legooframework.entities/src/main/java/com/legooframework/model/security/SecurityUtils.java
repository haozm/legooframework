package com.legooframework.model.security;

import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.security.entity.RoleEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Optional;

public abstract class SecurityUtils {

    public final static String ROLE_STOREMANAGER = "STOREMANAGER";
    public final static String ROLE_ADMINISTRATOR = "ADMINISTRATOR";
    public final static String ROLE_SHOPPINGGUIDE = "SHOPPINGGUIDE";

    private static boolean hasRole(Collection<RoleEntity> roles, String roleNo) {
        if (CollectionUtils.isEmpty(roles)) return false;
        Optional<RoleEntity> exits = roles.stream()
                .filter(x -> StringUtils.equals(roleNo, x.getRoleNo())).findFirst();
        return exits.isPresent();
    }

    private static boolean hasRoleNo(Collection<String> roles, String roleNo) {
        if (CollectionUtils.isEmpty(roles)) return false;
        return roles.contains(roleNo);
    }

    public static boolean hasStoreManager(LoginContext loginUser) {
        if (loginUser.getRoles().isPresent()) {
            return loginUser.getRoles().get().stream().anyMatch(x -> StringUtils.equals(x.getRoleNo(), ROLE_STOREMANAGER));
        }
        return false;
    }

    public static boolean hasShoppingGuide(LoginContext loginUser) {
        if (loginUser.getRoles().isPresent()) {
            return loginUser.getRoles().get().stream().anyMatch(x -> StringUtils.equals(x.getRoleNo(), ROLE_SHOPPINGGUIDE));
        }
        return false;
    }

    public static boolean hasAdministrator(LoginContext loginUser) {
        if (loginUser.getRoles().isPresent()) {
            return loginUser.getRoles().get().stream().anyMatch(x -> StringUtils.equals(x.getRoleNo(), ROLE_ADMINISTRATOR));
        }
        return false;
    }

    public static boolean hasStoreManager(Collection<RoleEntity> roles) {
        return hasRole(roles, ROLE_STOREMANAGER);
    }

    public static boolean hasStoreManagerNo(Collection<String> roles) {
        return hasRoleNo(roles, ROLE_STOREMANAGER);
    }

    public static boolean hasAdministrator(Collection<RoleEntity> roles) {
        return hasRole(roles, ROLE_ADMINISTRATOR);
    }

    public static boolean hasAdministratorNo(Collection<String> roles) {
        return hasRoleNo(roles, ROLE_ADMINISTRATOR);
    }

    public static boolean hasShoppingGuide(Collection<RoleEntity> roles) {
        return hasRole(roles, ROLE_SHOPPINGGUIDE);
    }

    public static boolean hasShoppingGuideNo(Collection<String> roles) {
        return hasRoleNo(roles, ROLE_SHOPPINGGUIDE);
    }

    public static boolean isAdministrator(RoleEntity role) {
        return (null != role) && StringUtils.equals(ROLE_ADMINISTRATOR, role.getRoleNo());
    }

}
