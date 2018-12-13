package com.legooframework.model.security.event;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.osgi.Bundle;
import org.apache.commons.lang3.StringUtils;

public final class SecEventFactory {

    private static final String EVENT_FINDACCOUNTBYNOEVENT = "findAccountByNoEvent";

    private static final String EVENT_FINDACCOUNTBYIDEVENT = "findAccountByIdEvent";

    private static final String EVENT_LOADACCOUNTAGGBYNOEVENT = "loadAccountAggByNoEvent";

    private static final String EVENT_CREATE_ACCOUNT_EVENT = "createAccountEvent";

    // 新增导购
    private static final String EVENT_CREATE_SHOPPINGGUIDE_EVENT = "createShoppingGuidEvent";

    // 新增店长
    private static final String EVENT_CREATE_STOREMANAGER_EVENT = "createStoreManagerEvent";

    // 指定账户ID 授权角色
    private static final String EVENT_AUTHORIZEDROLESEVENT_EVENT = "authorizedRolesEvent";

    // 加载当前登录用户可使用的权限
    private static final String EVENT_LOADLOGINUSERENABLEDROLES = "loadLoginUserEnabledRolesEvent";

    // 根据角色ID加载角色信息
    private static final String EVENT_LOADROLESBYIDS = "loadRolesByIdsEvent";

    // 获取所有角色列表
    private static final String EVENT_LOADALLROLES = "loadAllRolesEvent";

    public static boolean isAuthorizedRolesEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_AUTHORIZEDROLESEVENT_EVENT, event.getEventName());
    }

    public static LegooEvent authorizedRolesEvent(Bundle source, Long accountId, String... roleNos) {
        Preconditions.checkNotNull(accountId, "账户 accountId 不可以为空.");
        SecModuleEvent event = new SecModuleEvent(source.getName(), EVENT_AUTHORIZEDROLESEVENT_EVENT);
        event.setAccountId(accountId);
        event.setRoleNos(roleNos);
        return event;
    }

    public static boolean isCreateShoppingGuidEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_CREATE_SHOPPINGGUIDE_EVENT, event.getEventName());
    }

    public static boolean isLoadLoginUserEnabledRolesEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_LOADLOGINUSERENABLEDROLES, event.getEventName());
    }

    public static boolean isLoadRolesByIds(LegooEvent event) {
        return StringUtils.equals(EVENT_LOADROLESBYIDS, event.getEventName());
    }

    public static boolean isLoadAllRolesEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_LOADALLROLES, event.getEventName());
    }

    public static LegooEvent loadAllRolesEvent(Bundle source) {
        return new SecModuleEvent(source.getName(), EVENT_LOADALLROLES);
    }

    public static LegooEvent createShoppingGuidEvent(Bundle source, String accountNo, String accountName,
                                                     String password) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountNo), "账户 accountNo 不可以为空.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountName), "账户 accountName不可以为空.");
        SecModuleEvent event = new SecModuleEvent(source.getName(), EVENT_CREATE_SHOPPINGGUIDE_EVENT);
        event.setAccountNo(accountNo);
        event.setAccountName(accountName);
        event.setPassword(password);
        return event;
    }

    public static boolean isCreateStoreManagerEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_CREATE_STOREMANAGER_EVENT, event.getEventName());
    }

    public static LegooEvent createStoreManagerEvent(Bundle source, String accountNo, String accountName,
                                                     String password) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountNo), "账户 accountNo 不可以为空.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountName), "账户 accountName不可以为空.");
        SecModuleEvent event = new SecModuleEvent(source.getName(), EVENT_CREATE_STOREMANAGER_EVENT);
        event.setAccountNo(accountNo);
        event.setAccountName(accountName);
        event.setPassword(password);
        return event;
    }

    public static boolean isLoadAccountAggByNoEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_LOADACCOUNTAGGBYNOEVENT, event.getEventName());
    }

    public static LegooEvent loadAccountAggByNoEvent(Bundle source, String accountNo) {
        Preconditions.checkNotNull(accountNo, "待检索的用户登录账号不可以为空.");
        SecModuleEvent event = new SecModuleEvent(source.getName(), EVENT_LOADACCOUNTAGGBYNOEVENT);
        event.setAccountNo(accountNo);
        return event;
    }

    public static LegooEvent loadLoginUserEnabledRolesEvent(Bundle source) {
        return new SecModuleEvent(source.getName(), EVENT_LOADLOGINUSERENABLEDROLES);
    }

    public static LegooEvent loadRolesByIdsEvent(Bundle source, String... roleNos) {
        SecModuleEvent event = new SecModuleEvent(source.getName(), EVENT_LOADROLESBYIDS);
        event.setRoleNos(roleNos);
        return event;
    }

    public static boolean isCreateAccountEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_CREATE_ACCOUNT_EVENT, event.getEventName());
    }

    public static SecModuleEvent createAccountEvent(Bundle source, String accountNo, String accountName,
                                                    String password) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountNo), "账户 accountNo 不可以为空.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountName), "账户 accountName不可以为空.");
        SecModuleEvent event = new SecModuleEvent(source.getName(), EVENT_CREATE_ACCOUNT_EVENT);
        event.setAccountNo(accountNo);
        event.setAccountName(accountName);
        event.setPassword(password);
        return event;
    }

    public static SecModuleEvent findAccountByIdEvent(Bundle source, Long idVal) {
        Preconditions.checkNotNull(idVal, "待检索的实体唯一标识不可以为空.");
        SecModuleEvent event = new SecModuleEvent(source.getName(), EVENT_FINDACCOUNTBYIDEVENT);
        event.setAccountId(idVal);
        return event;
    }

    public static boolean isFindAccountByIdEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_FINDACCOUNTBYIDEVENT, event.getEventName());
    }

    public static SecModuleEvent findAccountByNoEvent(Bundle source, String accountNo) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountNo), "入参 accountNo 不可以为空.");
        SecModuleEvent event = new SecModuleEvent(source.getName(), EVENT_FINDACCOUNTBYNOEVENT);
        event.setAccountNo(accountNo);
        return event;
    }

    public static boolean isFindAccountByNoEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_FINDACCOUNTBYNOEVENT, event.getEventName());
    }
}
