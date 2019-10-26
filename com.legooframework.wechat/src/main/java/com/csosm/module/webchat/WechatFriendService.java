package com.csosm.module.webchat;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.event.EventBusSubscribe;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.BaseModelServer;
import com.csosm.module.base.entity.*;
import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.member.entity.MemberEntityAction;
import com.csosm.module.webchat.entity.*;
import com.csosm.module.webchat.event.AddFriends4PushingEvent;
import com.csosm.module.webchat.event.MemberWithWeixinEvent;
import com.csosm.module.webchat.group.WeixinGroupAction;
import com.csosm.module.webchat.group.WeixinGroupEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WechatFriendService extends AbstractBaseServer implements EventBusSubscribe {

    private static final Logger logger = LoggerFactory.getLogger(WebChatSendLogServer.class);

    public Map<String, Object> loadConfigInformation(Collection<Integer> storeIds, LoginUserContext user) {
        Preconditions.checkState(user.getCompany().isPresent(), "登录用户无公司信息");
        OrganizationEntity company = user.getCompany().get();
        WechatAddFriendConfigEntity config = new WechatAddFriendConfigEntity();
        Map<String, Object> result = config.toVO();
        int enableStoresNum = 0, disableStoresNum = 0;
        if (!CollectionUtils.isEmpty(storeIds)) {
            Optional<List<StoreEntity>> optional = getBean(StoreEntityAction.class).findByIds(storeIds);
            if (optional.isPresent() && !optional.get().isEmpty()) {
                enableStoresNum = getBean(WechatAddFriendConfigEntityAction.class).countConfigs(company, optional.get());
                disableStoresNum = optional.get().size() - enableStoresNum;
            }
        }
        result.put("enableStoresNum", enableStoresNum);
        result.put("disableStoresNum", disableStoresNum);
        return result;
    }

    public void createNewPushList() {
        logger.error("--------------------定时任务开始新建自动加粉名单----------------------------");
        Optional<List<StoreEntity>> storesOpt = getBean(StoreEntityAction.class).loadByHasDevice();
        if (!storesOpt.isPresent())
            return;
        List<WechatAddFriendConfigEntity> configs = getBean(WechatAddFriendConfigEntityAction.class).findByStores(storesOpt.get());
        if (CollectionUtils.isEmpty(configs)) return;
        for (StoreEntity store : storesOpt.get()) {
            Optional<WechatAddFriendConfigEntity> configOpt = getConfig(configs, store);
            if (!configOpt.isPresent()) continue;
            if (configOpt.get().isEnable())
                getBean(WechatAddFriendListAction.class).createPushListNew(store, configOpt.get());
        }
    }

    private Optional<WechatAddFriendConfigEntity> getConfig(List<WechatAddFriendConfigEntity> configs, StoreEntity store) {
        for (WechatAddFriendConfigEntity config : configs) {
            if (config.getStoreId().intValue() == store.getId().intValue()) return Optional.of(config);
        }
        return Optional.absent();
    }

    /**
     * 通过微信ID查找认领的导购
     *
     * @param loginUser
     * @param weixinId
     * @return
     */
    public Optional<EmployeeEntity> findBeSignedEmployee(LoginUserContext loginUser, String weixinId) {
        Objects.requireNonNull(loginUser);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), "入参微信ID不存在");
        Optional<WebChatUserEntity> weixinOpt = getBean(WebChatUserAction.class).findById(loginUser.getExitsStore(), weixinId);
        Optional<WechatSignEntity> signOpt = getBean(WechatSignAction.class).findByWeixin(weixinOpt.get());
        if (!signOpt.isPresent()) return Optional.absent();
        return getBean(EmployeeEntityAction.class).findById(signOpt.get().getEmployeeId());
    }

//    private List<WechatAddFriendListEntity> autoAddMemberCmds(List<WechatAddFriendListEntity> sendList) {
//        List<WechatAddFriendListEntity> resultList = Lists.newArrayList();
//        if (CollectionUtils.isEmpty(sendList)) return resultList;
//        List<RemoteCmdEntity> cmds = Lists.newArrayListWithCapacity(16);
//        for (WechatAddFriendListEntity entity : sendList) {
//            Optional<StoreEntity> storeOpt = getBean(StoreEntityAction.class).findById(entity.getStoreId());
//            if (!storeOpt.isPresent()) continue;
//            if (!storeOpt.get().hasDevice()) continue;
//            Optional<WechatAddFriendConfigEntity> configOpt = getBean(WechatAddFriendConfigEntityAction.class).findByStore(storeOpt.get());
//            if (!configOpt.isPresent()) continue;
//            resultList.add(entity);
//            String deviceId = null;
//            if (storeOpt.get().getDeviceIds().get().size() == 1) {
//                deviceId = storeOpt.get().getDeviceIds().get().iterator().next();
//            } else {
//                Map<String, Integer> countMap = getBean(WebChatUserAction.class).countWeixinByStore(storeOpt.get());
//                int min = Integer.MAX_VALUE;
//                for (Entry<String, Integer> entry : countMap.entrySet()) {
//                    if (min > entry.getValue()) {
//                        deviceId = entry.getKey();
//                        min = entry.getValue();
//                    }
//                }
//            }
//
//            Map<String, Object> command = Maps.newHashMap();
//            command.put("tag", configOpt.get().getContent());
//            command.put("ids", entity.getPhoneNo());
//            cmds.add(new RemoteCmdEntity("add_member", "FFFFFFFF", deviceId, gson.toJson(command), entity.getId().toString(), null));
//        }
//        getBean(RemoteCmdAction.class).batchSave(cmds);
//        return resultList;
//    }

    public void sendAndUpdateStatus() {
        logger.error("--------------------定时任务发送更新自动加粉会员指令----------------------------");
        Optional<List<WechatAddFriendListEntity>> sendPreList = getBean(WechatAddFriendListAction.class)
                .loadByOneForBatch();
        if (!sendPreList.isPresent())
            return;
        // List<WechatAddFriendListEntity> sendList = autoAddMemberCmds(sendPreList.get());
        // getBean(WechatAddFriendListAction.class).updateStatus(sendList);
    }

    public void createPushList() {
        Date today = DateTime.now().toDate();
        Optional<List<WechatAddFriendPushListEntity>> pushlist = getBean(WechatAddFriendListAction.class)
                .createPushList(today);
        if (!pushlist.isPresent())
            return;
        getBean(WechatAddFriendPushListAction.class).batchInsert(pushlist.get());
        getBean(WechatAddFriendListAction.class).updateStatus4Pushing(pushlist.get());
        getAsyncEventBus().post(new AddFriends4PushingEvent(pushlist.get()));
    }

    @Subscribe
    public void handleAddFriends4PushingEvent(AddFriends4PushingEvent event) {
        if (logger.isDebugEnabled()) logger.debug(String.format("Subscribe Event %s", event));
//        List<Integer> storeIds = Lists.newArrayList();
//        List<WechatAddFriendPushListEntity> pushLists = event.getPushLists();
//        for (WechatAddFriendPushListEntity $it : pushLists) storeIds.add($it.getStoreId());
//        Optional<List<StoreEntity>> stores = getBean(StoreEntityAction.class).findByIds(storeIds);
//        Preconditions.checkState(stores.isPresent(), "门店数据异常，无法获取ID=%s对应的门店实体....", storeIds);
//        List<RemoteCmdEntity> list = Lists.newArrayList();
//        StoreEntity store = null;
//        for (WechatAddFriendPushListEntity $it : pushLists) {
//            Map<String, Object> map = Maps.newHashMap();
//            for (StoreEntity $s : stores.get()) {
//                store = null;
//                if ($it.getStoreId().equals($s.getId())) {
//                    store = $s;
//                    break;
//                }
//            }
//            if (store == null) continue;
//            RemoteCmdEntity cmd = RemoteCmdEntity.addPhoneContact($it, store);
//            list.add(cmd);
//        }
//        getBean(RemoteCmdAction.class).batchSave(list);
//        getBean(WechatAddFriendPushListAction.class).batchMarkPushed(pushLists);
    }


    @Subscribe
    public void handleMemberWithWeixinEvent(MemberWithWeixinEvent event) {
        if (logger.isDebugEnabled()) logger.debug(String.format("Subscribe Event %s", event));
        if (event.isBildEvent()) {
            List<WebChatUserEntity> weixin_list = event.getWebChatUsers();
            if (CollectionUtils.isEmpty(weixin_list)) return;
            List<Integer> memberIds = Lists.newArrayList();
            for (WebChatUserEntity $it : weixin_list) memberIds.add($it.getBildMemberId());
            java.util.Optional<List<MemberEntity>> members = getBean(MemberEntityAction.class).findMembersByIds(event.getStore(), memberIds, false, false);
            if (members.isPresent())
                getBean(WechatAddFriendListAction.class).removeByMembers(members.get(), event.getStore());
        }
    }

    public java.util.Optional<List<MemberEntity>> loadTodayByStore(Integer storeId) {
        Preconditions.checkNotNull(storeId, "入参 门店ID 不可以为空...");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class).findById(storeId);
        Preconditions.checkState(store.isPresent(), "ID=%s对应的门店实体不存在...");
        Optional<List<WechatAddFriendPushListEntity>> pusdlist = getBean(WechatAddFriendPushListAction.class)
                .loadByStoreAndDate(store.get(), new Date());
        if (!pusdlist.isPresent()) return java.util.Optional.empty();
        List<Integer> memberIds = Lists.newArrayList();
        for (WechatAddFriendPushListEntity $it : pusdlist.get()) memberIds.addAll($it.getMemberIds());
        return getBean(MemberEntityAction.class).findMembersByIds(store.get(), memberIds, true, true);
    }

    public void updatePushedStatus() {
        Optional<List<WechatAddFriendPushListEntity>> pusdlist = getBean(WechatAddFriendPushListAction.class)
                .loadAllByAckList();
        if (!pusdlist.isPresent()) return;
        getBean(WechatAddFriendListAction.class).updateStatus4Pushed(pusdlist.get());
        getBean(WechatAddFriendPushListAction.class).batchMarkPushedMarked(pusdlist.get());

    }

    /**
     * 改变状态
     *
     * @param companyId
     * @param weixinId
     * @param phone
     * @param status
     */
    public void modifyStatus(Integer companyId, String deviceId, String weixinId, String phone, Integer status) {
        Objects.requireNonNull(companyId);
        Objects.requireNonNull(status);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(phone));
        Optional<OrganizationEntity> companyOpt = getBean(OrganizationEntityAction.class).findById(companyId);
        Preconditions.checkState(companyOpt.isPresent(), String.format("公司[%s]不存在，不执行更新状态操作", companyId));
        Optional<StoreEntity> storeOpt = getBean(StoreEntityAction.class).loadStoreByDeviceId(deviceId);
        Preconditions.checkState(storeOpt.isPresent(), String.format("设备[%s]对应的门店不存在，不执行更新状态操作", deviceId));
        java.util.Optional<MemberEntity> memberOpt = getBean(MemberEntityAction.class).findByStoreWithMobile(storeOpt.get(), phone);
        Preconditions.checkState(memberOpt.isPresent(), String.format("会员[%s]不存在,不执行更新状态操作", phone));
        getBean(WechatAddFriendListAction.class).updateStatus(companyOpt.get(), storeOpt.get(), memberOpt.get(), weixinId, phone, PushStatus.valueOf(status));
    }

    private List<StoreEntity> loadSuitStores(OrganizationEntity company, Collection<Integer> storeIds, WechatAddFriendConfigEntity.SuitType suitType) {
        List<StoreEntity> result = Lists.newArrayList();
        Optional<List<StoreEntity>> storesOpt = getBean(StoreEntityAction.class).findByIds(storeIds);
        if (!storesOpt.isPresent()) return result;
        List<StoreEntity> stores = storesOpt.get();
        List<WechatAddFriendConfigEntity> configs = getBean(WechatAddFriendConfigEntityAction.class).findByStores(stores, company);
        List<Integer> configStoreIds = Lists.newArrayList();
        for (WechatAddFriendConfigEntity config : configs) configStoreIds.add(config.getStoreId());
        switch (suitType) {
            case DISABLE_STORE:
                for (StoreEntity store : stores) {
                    if (!configStoreIds.contains(store.getId())) result.add(store);
                }
                break;
            case ALL_STORE:
                result = stores;
                break;
            default:
                break;
        }
        return result;

    }

    /**
     * enable 0 代表未启用门店，1代表已启用门店，2代表所有门店
     * open 0 代表不开启，1代表关闭
     *
     * @param storeIds
     * @param enable
     */
    public void createPushConfigs(Collection<Integer> storeIds, int suitType, int enable, String content, LoginUserContext user) {
        Preconditions.checkState(user.getCompany().isPresent(), "登录用户无公司信息");
        OrganizationEntity company = user.getCompany().get();
        List<StoreEntity> stores = loadSuitStores(company, storeIds, WechatAddFriendConfigEntity.SuitType.valueOf(suitType));
        switch (enable) {
            case 0:
                getBean(WechatAddFriendConfigEntityAction.class).clearPushConfigs(company, stores);
                break;
            case 1:
                getBean(WechatAddFriendConfigEntityAction.class).clearPushConfigs(company, stores);
                getBean(WechatAddFriendConfigEntityAction.class).saveAndEnablePushConfigs(company, stores, content);
                break;
            default:
                break;
        }
    }

    /**
     * 更新加粉操作成功状态
     */
    public void updateAddMemberSuccess() {
        Optional<List<WechatAddFriendListEntity>> successMemberOpt = getBean(WechatAddFriendListAction.class).loadByStatus(PushStatus.ADD_MEMBER_SUCCESS);
        if (!successMemberOpt.isPresent()) return;
        if (CollectionUtils.isEmpty(successMemberOpt.get())) return;
        List<WechatAddFriendListEntity> memberList = successMemberOpt.get();
        List<WechatAddFriendListEntity> bindSuccessMembers = Lists.newArrayList();
        for (WechatAddFriendListEntity member : memberList) {
            if (bindMember(member)) bindSuccessMembers.add(member);
        }
        getBean(WechatAddFriendListAction.class).updateStatus(bindSuccessMembers, PushStatus.ADD_MEMBER_REAL_SUCCESS);
        if (!CollectionUtils.isEmpty(memberList))
            getBean(BaseModelServer.class).cleanCache("adapterCache");
    }

    private boolean bindMember(WechatAddFriendListEntity member) {
        StoreEntity store = getBean(StoreEntityAction.class).loadById(member.getStoreId());
        java.util.Optional<MemberEntity> memberOpt = getBean(MemberEntityAction.class).findMemberById(store, member.getMemberId());
        if (!memberOpt.isPresent()) return false;
        Optional<StoreEntity> storeOpt = getBean(StoreEntityAction.class).findById(member.getStoreId());
        if (!storeOpt.isPresent()) return false;
        try {
            getBean(WebChatUserAction.class).buildBySingle(member.getWeixinId(), memberOpt.get(), storeOpt.get());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
