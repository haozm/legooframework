package com.csosm.module.webchat;

import com.csosm.commons.adapter.LoginUserContext;


import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.entity.*;
import com.csosm.module.member.entity.Member4MatchWebChatDto;
import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.member.entity.MemberEntityAction;
import com.csosm.module.webchat.concurrent.*;
import com.csosm.module.webchat.entity.Distance;
import com.csosm.module.webchat.entity.MatchWebChatComparator;
import com.csosm.module.webchat.entity.WebChatUserAction;
import com.csosm.module.webchat.entity.WebChatUserEntity;
import com.csosm.module.webchat.event.MemberWithWeixinEvent;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// 会员绑定服务
public class WebChatBindMemberServer extends AbstractBaseServer {

    private static final Logger logger = LoggerFactory.getLogger(WebChatBindMemberServer.class);

    private static Splitter.MapSplitter mapSplitter = Splitter.on(',').withKeyValueSeparator(':');

    //批量绑定
    public void batchBildMembers(String mapping, StoreEntity store, LoginUserContext userContext) {
        Preconditions.checkNotNull(userContext);
        Preconditions.checkNotNull(store, "无门店信息.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mapping));
        Map<String, String> map = mapSplitter.split(mapping);
        Collection<String> weixin_ids = map.keySet();
        Collection<String> member_ids = map.values();
        Set<Integer> member_int_ids = Sets.newHashSet();
        for (String $it : member_ids) member_int_ids.add(NumberUtils.createInteger($it));
        Optional<List<WebChatUserEntity>> webchatusers = getBean(WebChatUserAction.class)
                .loadAllByIds(store, weixin_ids);
        Preconditions.checkState(webchatusers.isPresent(), "当前门店%s不存在指定的微信好友.", store.getName());
        java.util.Optional<List<MemberEntity>> members = getBean(MemberEntityAction.class)
                .findMembersByIds(store, member_int_ids, false, false);
        Preconditions.checkState(members.isPresent(), "当前门店%s不存在指定的会员.", store.getName());
        List<WebChatUserEntity> bilds = Lists.newArrayList();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Optional<WebChatUserEntity> wx = webChatUserById(webchatusers.get(), entry.getKey());
            Optional<MemberEntity> mm = memberById(members.get(), entry.getValue());
            if (wx.isPresent() && mm.isPresent()) {
                Optional<WebChatUserEntity> opt = wx.get().bildMember(mm.get());
                if (opt.isPresent()) bilds.add(opt.get());
            }
        }
        if (!CollectionUtils.isEmpty(bilds)) {
            getBean(WebChatUserAction.class).batchBildMember(bilds, store, userContext);
            getAsyncEventBus().post(new MemberWithWeixinEvent(bilds, store, userContext));
        }
    }

    // 解除绑定
    public void unBildMembers(String weixinId, StoreEntity store, LoginUserContext userContext) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), "入参 String weixinId 不可以为空...");
        Set<String> weixin_ids = Sets.newHashSet(weixinId);
        Optional<List<WebChatUserEntity>> webchatusers = getBean(WebChatUserAction.class)
                .loadAllByIds(store, weixin_ids);
        if (!webchatusers.isPresent()) return;
        WebChatUserEntity entity = webchatusers.get().get(0);
        if (!entity.hasMember()) return;
        getBean(WebChatUserAction.class).unBildMember(weixin_ids, Lists.newArrayList(entity.getBildMemberId()),store, userContext);
        getAsyncEventBus().post(new MemberWithWeixinEvent(entity, store, userContext));
    }

    private Optional<WebChatUserEntity> webChatUserById(List<WebChatUserEntity> webchatusers, String id) {
        Optional<WebChatUserEntity> optional = Optional.absent();
        for (WebChatUserEntity $it : webchatusers) {
            if (StringUtils.equalsIgnoreCase($it.getId(), id)) {
                optional = Optional.of($it);
                break;
            }
        }
        return optional;
    }

    /**
     * 通过 会员 智能 匹配微信
     *
     * @param store
     * @param memberId
     * @return 返回前五个匹配项
     */
    public Optional<List<WechatMatchMemberDto>> memberMatchWechats(StoreEntity store, Integer memberId) {
        Preconditions.checkNotNull(store, "所属门店不可以为空值...");
        Preconditions.checkNotNull(memberId, "Integer memberId 不可以为空值...");
        Set<Integer> ids = Sets.newHashSet(memberId);
        java.util.Optional<List<MemberEntity>> wechatUsers = getBean(MemberEntityAction.class).findMembersByIds(store, ids, false, false);
        Preconditions.checkState(wechatUsers.isPresent(), "memberId = %s 无法匹配对应的会员信息...", memberId);
        MemberEntity member = wechatUsers.get().get(0);
        Preconditions.checkState(!member.hasWeixin(), "当前会员已绑定微信...");
        Optional<List<WebChatUserEntity>> unbind_weixins_opt = getBean(WebChatUserAction.class)
                .loadAllByStore(store, null, false);
        Preconditions.checkState(unbind_weixins_opt.isPresent(), "当前门店尚未适合的微信，用于智能匹配...");
        List<WebChatUserEntity> unbind_weixins = unbind_weixins_opt.get();
        List<WechatMatchMemberDto> unbind_weixins_dto = Lists.newArrayList();
        for (WebChatUserEntity $it : unbind_weixins) {
            unbind_weixins_dto.add(new WechatMatchMemberDto($it));
        }
        String batch = UUID.randomUUID().toString();
        List<List<WechatMatchMemberDto>> list_lists = Lists.partition(unbind_weixins_dto, 50);
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]本次微信共计 %s 个，拆分为%s 小组进行匹配.", batch, unbind_weixins_dto.size(),
                    list_lists.size()));

        List<ListenableFuture<List<WechatMatchMemberDto>>> listenableFutures =
                Lists.newArrayListWithCapacity(list_lists.size());
        for (List<WechatMatchMemberDto> $it : list_lists) {
            ListenableFuture<List<WechatMatchMemberDto>> listenableFuture = getExecutorService()
                    .submit(new MatchWechatFuture(member, $it));
            listenableFutures.add(listenableFuture);
        }
        ListenableFuture<List<List<WechatMatchMemberDto>>> successfulAsList = Futures.successfulAsList(listenableFutures);
        try {
            List<List<WechatMatchMemberDto>> matchResultSet = successfulAsList.get();
            if (logger.isDebugEnabled())
                logger.debug(String.format("[%s]完成会员微信匹配.", batch));
            List<WechatMatchMemberDto> list = Lists.newArrayList();
            for (List<WechatMatchMemberDto> $it : matchResultSet) {
                list.addAll($it);
            }
            Ordering<Distance> ordering = MatchWebChatComparator.orderDistance();
            Collections.sort(list, ordering);
            list = list.size() > 5 ? list.subList(0, 4) : list;
            return Optional.of(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("批量匹配会员执行异常...", e);
        }
    }

    // 批量匹配会员
    public List<MatchResultSet> batchMatchMembersByStore(StoreEntity store, List<String> weixinIds,
                                                         LoginUserContext userContext) {
        Preconditions.checkNotNull(userContext);
        Preconditions.checkNotNull(store);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(weixinIds));
        Preconditions.checkArgument(userContext.getMaxPowerRole().isPresent(), "登陆用户缺少角色信息。");

        Optional<List<WebChatUserEntity>> wexin_users = getBean(WebChatUserAction.class).loadAllByStore(store, null, false);
        Preconditions.checkState(wexin_users.isPresent(), "待匹配的会员不允许为空...");
        List<WebChatUserEntity> un_weixin_usrs = Lists.newArrayList();
        for (String id : weixinIds) {
            for (WebChatUserEntity $it : wexin_users.get()) {
                if (StringUtils.equals(id, $it.getUserName())) {
                    un_weixin_usrs.add($it);
                    break;
                }
            }
        }

        java.util.Optional<List<Member4MatchWebChatDto>> members = getBean(MemberEntityAction.class).find4MatchWebChatByStore(store);
        Preconditions.checkState(members.isPresent(), "指定门店%s对应的尚未绑定的会员不存在.", store.getName());
        List<List<Member4MatchWebChatDto>> member_sub_list = Lists.partition(members.get(), 1024);
        String batch = UUID.randomUUID().toString();
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s] weixin [%s] member: [%s] splite %s groups...", batch, un_weixin_usrs.size(),
                    members.get().size(), member_sub_list.size()));
        List<ListenableFuture<Multimap<WebChatUserEntity, Member4MatchWebChatDto>>> listenableFutures =
                Lists.newArrayListWithCapacity(member_sub_list.size());

        for (List<Member4MatchWebChatDto> $it : member_sub_list) {
            ListenableFuture<Multimap<WebChatUserEntity, Member4MatchWebChatDto>> listenableFuture =
                    getExecutorService().submit(new MatchMemberFuture(un_weixin_usrs, $it, true));
            listenableFutures.add(listenableFuture);
        }

        ListenableFuture<List<Multimap<WebChatUserEntity, Member4MatchWebChatDto>>> successfulAsList =
                Futures.successfulAsList(listenableFutures);
        try {
            List<Multimap<WebChatUserEntity, Member4MatchWebChatDto>> matchResultSet = successfulAsList.get();
            if (logger.isDebugEnabled())
                logger.debug(String.format("[%s]完成会员好友匹配.", batch));

            Multimap<WebChatUserEntity, Member4MatchWebChatDto> multimap = ArrayListMultimap.create();
            for (Multimap<WebChatUserEntity, Member4MatchWebChatDto> $it : matchResultSet) multimap.putAll($it);

            Ordering<Distance> ordering = MatchWebChatComparator.orderDistance();
            List<MatchResultSet> mt_list = Lists.newArrayList();

            for (WebChatUserEntity $key : multimap.keySet()) {
                MatchResultSet cur = new MatchResultSet($key);
                List<Member4MatchWebChatDto> sorts_members = ordering.sortedCopy(multimap.get($key));
                sorts_members = sorts_members.size() > 10 ? sorts_members.subList(0, 9) : sorts_members;
                cur.setMembers(sorts_members);
                mt_list.add(cur);
            }
            return mt_list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("批量匹配会员执行异常...", e);
        }
    }

    /**
     * @param storeId
     * @param weixinId
     * @return
     */
    public java.util.Optional<MemberEntity> loadMemberByWeixinId(Integer storeId, String weixinId) {
        Preconditions.checkNotNull(storeId, "入参  Integer storeId 不可以未空值...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), "入参  String weixinId 不可以未空值...");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class).findById(storeId);
        Preconditions.checkState(store.isPresent(), "所属门店不存在.....");
        Optional<WebChatUserEntity> webchat = getBean(WebChatUserAction.class).findById(store.get(), weixinId);
        Preconditions.checkState(webchat.isPresent(), "微信号[%s]不存在...", weixinId);
        if (!webchat.get().hasMember()) return java.util.Optional.empty();
        Integer memberId = webchat.get().getBildMemberId();
        return getBean(MemberEntityAction.class).findMemberById(store.get(), memberId);
    }

    private Optional<MemberEntity> memberById(List<MemberEntity> members, String id) {
        Optional<MemberEntity> optional = Optional.absent();
        for (MemberEntity $it : members) {
            if (Objects.equals($it.getId(), NumberUtils.createInteger(id))) {
                optional = Optional.of($it);
                break;
            }
        }
        return optional;
    }

    private ListeningExecutorService getExecutorService() {
        return getBean(ListeningExecutorService.class);
    }

}
