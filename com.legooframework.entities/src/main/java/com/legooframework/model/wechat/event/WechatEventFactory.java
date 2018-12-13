package com.legooframework.model.wechat.event;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.wechat.entity.WechatFriendEntity;
import org.apache.commons.codec.binary.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WechatEventFactory {

    private final static String EVENT_LOADWECHATWORKACCOUNTBYUSERNAME = "loadWechatWorkAccountByUserNameEvent";

    private final static String EVENT_MODIFYWECHATACCOUNT = "modifyWechatAccountEvent";

    private final static String EVENT_SYNCSINGLECONTACT = "syncSingleContactEvent";

    private final static String EVENT_INITCONTACT = "initContactEvent";

    private final static String EVENT_NOTICE_ADDEDWECHATACCOUNT = "noticeAddedWechatAccountEvent";

    private final static String EVENT_NOTICE_INITEDWECHATACCOUNT = "noticeInitedWechatAccountEvent";

    private final static String EVENT_FINDWECHATACCONTBYSTOREIDANDACCOUNTUSERNAME = "findWechatAccountByStoreIdAndAccountUserNameEvent";

    private final static String EVENT_LOADALLACCOUNTBYSTOREID = "loadAllWechatByStoreIdEvent";

    private final static String EVENT_FINDWECHATACCONTBYSTOREIDANDWEHATID = "findWechatAccountByStoreIdAndWechatIdEvent";

    private final static String EVENT_NOTICE_SYNCWECHATACCOUNTS = "noticeSyncWechatAccountsEvent";

    private WechatEventFactory() {
        throw new AssertionError();
    }

    public static WechatModuleEvent loadWechatWorkAccountByUserName(Bundle source, String userName) {
        Objects.requireNonNull(source);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userName), "入参 userName 不可以为空.");
        WechatModuleEvent event = new WechatModuleEvent(source.getName(), EVENT_LOADWECHATWORKACCOUNTBYUSERNAME);
        event.setUserName(userName);
        return event;
    }

    public static WechatModuleEvent findWechatAccountByStoreIdAndUserNameEvent(Bundle source, Long storeId, String userName) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(storeId);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userName), "入参 userName 不可以为空.");
        WechatModuleEvent event = new WechatModuleEvent(source.getName(), EVENT_FINDWECHATACCONTBYSTOREIDANDACCOUNTUSERNAME);
        event.setUserName(userName);
        event.setStoreId(storeId);
        return event;
    }

    public static WechatModuleEvent findWechatAccountByStoreIdAndWechatIdEvent(Bundle source, Long storeId, Long wechatId) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(storeId);
        Objects.requireNonNull(wechatId);
        WechatModuleEvent event = new WechatModuleEvent(source.getName(), EVENT_FINDWECHATACCONTBYSTOREIDANDWEHATID);
        event.setAccountId(wechatId);
        event.setStoreId(storeId);
        return event;
    }

    public static WechatModuleEvent syncSingleContactEvent(String operation, String deviceId,
                                                           String wechatId, String friendId) {
        checkArguments(operation, deviceId, wechatId, friendId);
        WechatModuleEvent event = new WechatModuleEvent(EVENT_SYNCSINGLECONTACT);
        event.setOperation(operation);
        event.setDeviceId(deviceId);
        event.setWechatId(wechatId);
        event.setFriendId(friendId);
        return event;
    }

    public static WechatModuleEvent initContactEvent(String operation, String deviceId,
                                                     String wechatId) {
        checkArguments(operation, deviceId, wechatId);
        WechatModuleEvent event = new WechatModuleEvent(EVENT_INITCONTACT);
        event.setOperation(operation);
        event.setDeviceId(deviceId);
        event.setWechatId(wechatId);
        return event;
    }

    public static WechatModuleEvent noticeAddedWechatAccount(String deviceId, WechatFriendEntity friend) {
        Objects.requireNonNull(friend);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "设备ID不能为空");
        WechatModuleEvent event = new WechatModuleEvent(EVENT_NOTICE_ADDEDWECHATACCOUNT);
        event.setDeviceId(deviceId);
        event.setWechatFriend(friend);
        return event;
    }

    public static WechatModuleEvent noticeSyncWechatAccounts(List<Map<String, Object>> wechatAccounts) {
        Preconditions.checkArgument(!wechatAccounts.isEmpty(), "同步微信信息不能为空");
        WechatModuleEvent event = new WechatModuleEvent(EVENT_NOTICE_SYNCWECHATACCOUNTS);
        event.setSyncWechatAccounts(wechatAccounts);
        return event;
    }

    private static void checkArguments(String... arguments) {
        Arrays.stream(arguments).forEach(x -> Preconditions.checkArgument(!Strings.isNullOrEmpty(x), "入参为空或nulll"));
    }

    public static WechatModuleEvent noticeModifyWechatAccount() {
        return new WechatModuleEvent(EVENT_MODIFYWECHATACCOUNT);
    }

    public static WechatModuleEvent noticeInitedWechatAccount() {
        return new WechatModuleEvent(EVENT_NOTICE_INITEDWECHATACCOUNT);
    }

    public static WechatModuleEvent loadAllAccountByStoreIdEvent() {
        return new WechatModuleEvent(EVENT_LOADALLACCOUNTBYSTOREID);
    }

    public static boolean isLoadAllAccountByStoreIdEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_LOADALLACCOUNTBYSTOREID);
    }

    public static boolean isNoticeAddedWechatAccountEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_NOTICE_ADDEDWECHATACCOUNT);
    }


    public static boolean isNoticeInitedWechatAccountEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_NOTICE_INITEDWECHATACCOUNT);
    }

    public static boolean isLoadWechatWorkAccountByUserNameEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_LOADWECHATWORKACCOUNTBYUSERNAME);
    }

    public static boolean isModifyWechatAccount(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_MODIFYWECHATACCOUNT);
    }

    public static boolean isSyncSingleContactEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_SYNCSINGLECONTACT);
    }

    public static boolean isInitContactEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_INITCONTACT);
    }

    public static boolean isFindWechatAccountByStoreIdAndAccountUserNameEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_FINDWECHATACCONTBYSTOREIDANDACCOUNTUSERNAME);
    }

    public static boolean isFindWechatAccountByStoreIdAndWechatIdEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_FINDWECHATACCONTBYSTOREIDANDWEHATID);
    }

    public static boolean isNoticeSyncWechatAccountsEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_NOTICE_SYNCWECHATACCOUNTS);
    }
}
