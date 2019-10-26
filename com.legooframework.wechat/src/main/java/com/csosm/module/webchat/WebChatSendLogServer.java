package com.csosm.module.webchat;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.event.EventBusSubscribe;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.BaseModelServer;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.csosm.module.webchat.entity.*;
import com.csosm.module.webchat.event.BatchSendWetchatMsgEvent;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WebChatSendLogServer extends AbstractBaseServer implements EventBusSubscribe {

    private static final Logger logger = LoggerFactory.getLogger(WebChatSendLogServer.class);

    /**
     * 指定门店，返回指定一批会员ID匹配的微信数量，如果为null 则返回当前门店的全部微信数量
     *
     * @param storeId   门店ID
     * @param memberIds 待匹配的会员ID
     * @return int 匹配成功会员的微信数量
     */
    public int loadWechatWithMemberNums(Integer storeId, Collection<Integer> memberIds) {
        Preconditions.checkNotNull(storeId, "门店ID不可以为空...");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s对应的门店不存在...");

        Optional<List<WebChatUserEntity>> webChatUsers = getBean(WebChatUserAction.class).loadAllByStore(store.get(),
                null, true);
        if (!webChatUsers.isPresent())
            return 0;
        if (CollectionUtils.isEmpty(memberIds))
            return webChatUsers.get().size();

        Set<WebChatUserEntity> weixins = Sets.newHashSet();
        for (WebChatUserEntity $it : webChatUsers.get()) {
            if ($it.hasMember() && memberIds.contains($it.getBildMemberId())) {
                weixins.add($it);
            }
        }
        return CollectionUtils.isNotEmpty(weixins) ? weixins.size() : 0;
    }

    /**
     * 判断指定的门店是否绑定微信,用户CRM系统调用
     *
     * @param storeId 待咨询的门店ID
     * @return boolean 是否绑定
     */
    public boolean isBildWechat(Integer storeId) {
        Preconditions.checkNotNull(storeId, "门店ID不可以为空...");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s对应的门店不存在...");
        return store.get().hasDevice();
    }

    /**
     * 用于深研团队调用接口
     *
     * @param imgs     如果有则多张图片合计 用逗号分隔
     * @param sendInfo 发送的内容信息
     * @param storeId  所在门店ID
     * @param userId   当前操作用户，如果是批处理则填写忽略填写该项
     * @return boolean 是否发送成功
     */
    public boolean sendWebChatMsg(Map<Integer, String> sendInfo, Integer storeId, Long msgTempId, String imgs,
                                  Integer userId) {
        Preconditions.checkArgument(MapUtils.isNotEmpty(sendInfo), "会员列表ID不可以为空.");
        Preconditions.checkNotNull(storeId, "门店ID不可以为空...");
        LoginUserContext user = null;
        if (userId != null)
            user = getBean(BaseModelServer.class).loadContextByUserId(userId, null);

        Optional<StoreEntity> store = getBean(StoreEntityAction.class).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s对应的门店不存在...");

        Optional<List<DevicesEntity>> godVevOpt = getBean(DevicesEntityAction.class).findGodDeviceByStore(store.get());
        Preconditions.checkState(godVevOpt.isPresent(), "门店%s对应的大V手机无法获取...");

        Optional<List<WebChatUserEntity>> webChatUsers = getBean(WebChatUserAction.class).loadAllByStore(store.get(),
                null, true);
        if (!webChatUsers.isPresent()) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("门店 %s 暂无绑定微信会员....,忽略本次发送.", store.get().getName()));
            return true;
        }

        Set<Integer> memberIds = sendInfo.keySet();
        Set<WebChatUserEntity> weixins = Sets.newHashSet();
        for (WebChatUserEntity $it : webChatUsers.get()) {
            if ($it.hasMember() && memberIds.contains($it.getBildMemberId())) {
                weixins.add($it);
            }
        }

        if (CollectionUtils.isEmpty(weixins)) {
            if (logger.isDebugEnabled())
                logger.debug("传入的会员信息尚未绑定微信....,忽略本次发送.");
            return true;
        }

        ListMultimap<String, WebChatUserEntity> multimap = ArrayListMultimap.create();

        for (WebChatUserEntity weixin : weixins)
            multimap.put(weixin.getOwnerUserName(), weixin);

        Preconditions.checkState(CollectionUtils.isNotEmpty(weixins), "指定的微信Id集合不存在当前门店.");
        String batchNo;
        String[] img_args = Strings.isNullOrEmpty(imgs) ? null : StringUtils.split(imgs, ',');

        if (user == null) {
            batchNo = getBean(SendMsgEntityAction.class).insert("会员批量关怀，详细信息需查看明细", msgTempId, img_args, weixins,
                    store.get());
        } else {
            batchNo = getBean(SendMsgEntityAction.class).insert("会员批量关怀，详细信息需查看明细", msgTempId, img_args, weixins,
                    store.get(), user);
        }

        List<SendMsgDetailEntity> sendMsgs = Lists.newArrayList();
        for (String weixinId : multimap.asMap().keySet()) {
            Optional<DevicesEntity> deviceOpt = getBean(DevicesEntityAction.class).findByWeixinId(weixinId);
            if (!deviceOpt.isPresent()) {
                if (logger.isWarnEnabled())
                    logger.warn(String.format("主微信号[%s] 无可用设备", weixinId));
                continue;
            }
            for (WebChatUserEntity $it : multimap.get(weixinId)) {
                Integer meberId = $it.getBildMemberId();
                String content = MapUtils.getString(sendInfo, meberId == null ? -1 : meberId);
                String groupId = UUID.randomUUID().toString();
                int order = 1;
                if (ArrayUtils.isNotEmpty(img_args)) {
                    for (String img : img_args) {
                        SendMsgDetailEntity item = new SendMsgDetailEntity(img, $it.getUserName(), batchNo,
                                deviceOpt.get().getId(), 3, store.get(), userId == null ? -99 : userId, groupId, order);
                        sendMsgs.add(item);
                        order++;
                    }
                }
                SendMsgDetailEntity item = new SendMsgDetailEntity(content, $it.getUserName(), batchNo,
                        deviceOpt.get().getId(), 1, store.get(), userId == null ? -99 : userId, groupId, order + 1);
                sendMsgs.add(item);
            }
        }
        try {
            getBean(SendMsgEntityAction.class).batchToSend(sendMsgs);
        } catch (Exception e) {
            logger.error("发送微信信息失败", e);
            return false;
        }
        return true;
    }

    /**
     * @param sendInfo 发送的内容信息
     * @param storeId  所在门店ID
     * @param userId   当前操作用户，如果是批处理则填写忽略填写该项
     * @return boolean 是否发送成功
     * @desc 原系统调用
     */
    public boolean sendWebChatMsg(Map<Integer, String> sendInfo, Integer storeId, Integer userId) {
        Preconditions.checkArgument(MapUtils.isNotEmpty(sendInfo), "会员列表ID不可以为空.");
        Preconditions.checkNotNull(storeId, "门店ID不可以为空...");
        LoginUserContext user = null;
        if (userId != null)
            user = getBean(BaseModelServer.class).loadContextByUserId(userId, null);

        Optional<StoreEntity> store = getBean(StoreEntityAction.class).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s对应的门店不存在...");

        Optional<List<DevicesEntity>> devicesOpt = getBean(DevicesEntityAction.class).findGodDeviceByStore(store.get());
        Preconditions.checkState(devicesOpt.isPresent(), "门店%s对应的大V手机无法获取...");

        Optional<List<WebChatUserEntity>> webChatUsers = getBean(WebChatUserAction.class).loadAllByStore(store.get(),
                null, true);
        if (!webChatUsers.isPresent()) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("门店 %s 暂无绑定微信会员....,忽略本次发送.", store.get().getName()));
            return false;
        }

        Set<Integer> memberIds = sendInfo.keySet();
        Set<WebChatUserEntity> weixins = Sets.newHashSet();
        for (WebChatUserEntity $it : webChatUsers.get()) {
            if ($it.hasMember() && memberIds.contains($it.getBildMemberId())) {
                weixins.add($it);
            }
        }

        if (CollectionUtils.isEmpty(weixins)) {
            if (logger.isDebugEnabled())
                logger.debug("传入的会员信息尚未绑定微信....,忽略本次发送.");
            return false;
        }

        Preconditions.checkState(CollectionUtils.isNotEmpty(weixins), "指定的微信Id集合不存在当前门店.");

        ListMultimap<String, WebChatUserEntity> multimap = ArrayListMultimap.create();

        for (WebChatUserEntity weixin : weixins)
            multimap.put(weixin.getOwnerUserName(), weixin);

        String batchNo;

        if (user == null) {
            batchNo = getBean(SendMsgEntityAction.class).insert("会员批量关怀，详细信息需查看明细", null, null, weixins, store.get());
        } else {
            batchNo = getBean(SendMsgEntityAction.class).insert("会员批量关怀，详细信息需查看明细", null, null, weixins, store.get(),
                    user);
        }

        List<SendMsgDetailEntity> sendMsgs = Lists.newArrayList();
        for (String weixinId : multimap.asMap().keySet()) {
            Optional<DevicesEntity> deviceOpt = getBean(DevicesEntityAction.class).findByWeixinId(weixinId);
            if (!deviceOpt.isPresent()) {
                if (logger.isWarnEnabled())
                    logger.warn(String.format("主微信号[%s] 无可用设备", weixinId));
                continue;
            }
            for (WebChatUserEntity $it : weixins) {
                Integer meberId = $it.getBildMemberId();
                String content = MapUtils.getString(sendInfo, meberId == null ? -1 : meberId);
                String groupId = UUID.randomUUID().toString();
                SendMsgDetailEntity item = new SendMsgDetailEntity(content, $it.getUserName(), batchNo,
                        deviceOpt.get().getId(), 1, store.get(), userId == null ? -99 : userId, groupId, 1);
                sendMsgs.add(item);
            }
        }
        getBean(SendMsgEntityAction.class).batchToSend(sendMsgs);
        return true;
    }

    void sendWebChatMsg(String msgTxt, String[] imageInfo, Collection<String> weixinIds, Integer storeId,
                        Long msgTempId, LoginUserContext userContext) {
        Preconditions.checkNotNull(userContext);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(weixinIds), "微信ID集合不可以为空.");
        Preconditions.checkNotNull(storeId, "入参 Integer storeId 不可以为空值...");
        Preconditions.checkArgument(userContext.getCompany().isPresent(), "当前用户未绑定公司，无法执行该操作.");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s对应的门店不存在...");
        Optional<List<WebChatUserEntity>> webChatUsers = getBean(WebChatUserAction.class).loadAllByStore(store.get(),
                null);
        Preconditions.checkState(webChatUsers.isPresent(), "当前门店无微信好友记录.");
        Set<WebChatUserEntity> weixins = Sets.newHashSet();
        for (String $it : weixinIds) {
            for (WebChatUserEntity $w : webChatUsers.get()) {
                if (StringUtils.equals($it, $w.getUserName()))
                    weixins.add($w);
            }
        }
        sendWebChatMsg(msgTxt, imageInfo, weixins, store.get(), msgTempId, userContext);
    }

    void sendWebChatMsg(String msgTxt, String[] imageInfo, Set<WebChatUserEntity> weixins, StoreEntity store,
                        Long msgTempId, LoginUserContext userContext) {
        Preconditions.checkState(CollectionUtils.isNotEmpty(weixins), "指定的微信Id集合不存在当前门店.");
        String batchId = getBean(SendMsgEntityAction.class).insert(msgTxt, msgTempId, imageInfo, weixins, store,
                userContext);
        Optional<SendMsgEntity> optional = getBean(SendMsgEntityAction.class).findById(batchId);
        Preconditions.checkState(optional.isPresent(), "持久化微信群发消息记录失败,batchId=%s.", batchId);
        // 发布事件
        if (optional.get().canReplace()) {
            ListMultimap<String, WebChatUserEntity> multimap = ArrayListMultimap.create();
            for (WebChatUserEntity weixin : weixins) multimap.put(weixin.getOwnerUserName(), weixin);
            List<SendMsgDetailEntity> msgDetails = Lists.newArrayList();
            for (String weixinId : multimap.asMap().keySet()) {
                Optional<DevicesEntity> deviceOpt = getBean(DevicesEntityAction.class).findByWeixinId(weixinId);
                if (!deviceOpt.isPresent()) {
                    if (logger.isErrorEnabled())
                        logger.error(String.format("群发信息，主微信号[%s]无设备", weixinId));
                    continue;
                }
                msgDetails.addAll(optional.get().toMessageDetail(deviceOpt.get(), userContext, multimap.get(weixinId)));
            }
            getAsyncEventBus().post(new BatchSendWetchatMsgEvent(msgDetails));
        } else {
            Optional<List<WebChatUserEntity>> weixinsOpt = getBean(WebChatUserAction.class).loadAllByIds(store,
                    optional.get().getWeixinIds());
            if (!weixinsOpt.isPresent()) {
                if (logger.isErrorEnabled())
                    logger.error("群发短信发生异常。。。。。 主微信 %s 对应的实体不存在...", optional.get());
                return;
            }
            ListMultimap<String, WebChatUserEntity> multimap = ArrayListMultimap.create();
            for (WebChatUserEntity weixin : weixinsOpt.get()) multimap.put(weixin.getOwnerUserName(), weixin);
            List<SendMsgDetailEntity> details = Lists.newArrayList();
            for (String weixinId : multimap.asMap().keySet()) {
                Optional<DevicesEntity> deviceOpt = getBean(DevicesEntityAction.class).findByWeixinId(weixinId);
                Preconditions.checkState(deviceOpt.isPresent(), "微信 %s 对应的设备不存在...", weixinId);
                details.addAll(optional.get().toMessageDetail(deviceOpt.get(), userContext, multimap.get(weixinId)));
            }
            getAsyncEventBus().post(new BatchSendWetchatMsgEvent(details));
        }
    }

    @Subscribe
    public void subBatchSendWetchatMsgEvent(BatchSendWetchatMsgEvent event) {
        if (CollectionUtils.isEmpty(event.getSendMsgDetails())) {
            StoreEntity store = event.getStore();
            SendMsgEntity sendMsg = event.getSendMsgEntity();
            Optional<List<WebChatUserEntity>> weixinsOpt = getBean(WebChatUserAction.class).loadAllByIds(store,
                    sendMsg.getWeixinIds());
            if (!weixinsOpt.isPresent()) {
                if (logger.isErrorEnabled())
                    logger.error("群发短信发生异常。。。。。 主微信 %s 对应的实体不存在...", event.getSendMsgEntity());
                return;
            }

            ListMultimap<String, WebChatUserEntity> multimap = ArrayListMultimap.create();
            for (WebChatUserEntity weixin : weixinsOpt.get()) multimap.put(weixin.getOwnerUserName(), weixin);
            List<SendMsgDetailEntity> details = Lists.newArrayList();
            for (String weixinId : multimap.asMap().keySet()) {
                Optional<DevicesEntity> deviceOpt = getBean(DevicesEntityAction.class).findByWeixinId(weixinId);
                if (!deviceOpt.isPresent()) {
                    if (logger.isErrorEnabled())
                        logger.error(String.format("群发信息，主微信号[%s]无设备", weixinId));
                    continue;
                }
                details.addAll(sendMsg.toMessageDetail(deviceOpt.get(), event.getUserContext(), multimap.get(weixinId)));
            }
            getBean(SendMsgEntityAction.class).batchToSend(details);
        } else {
            getBean(SendMsgEntityAction.class).batchToSend(event.getSendMsgDetails());
        }
    }

}
