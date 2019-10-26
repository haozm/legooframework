package com.csosm.module.labels;

import com.csosm.commons.adapter.LoginUserContext;

import com.csosm.commons.event.EventBusSubscribe;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.entity.*;
import com.csosm.module.labels.entity.*;
import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.member.entity.MemberEntityAction;
import com.csosm.module.webchat.entity.WebChatUserAction;
import com.csosm.module.webchat.entity.WebChatUserEntity;
import com.csosm.module.webchat.entity.WechatSignAction;
import com.csosm.module.webchat.event.MemberWithWeixinEvent;
import com.csosm.module.webchat.group.WeixinGroupAction;
import com.csosm.module.webchat.group.WeixinGroupEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class LabelMarkedService extends AbstractBaseServer implements EventBusSubscribe {

    private static final Logger logger = LoggerFactory.getLogger(LabelMarkedService.class);

    public void markLables(Long labelId, Set<Integer> memberIds, Set<String> weixinIds, LoginUserContext loginUser) {
        Preconditions.checkNotNull(labelId, "Long labelId NOT NULL...");
        Preconditions.checkNotNull(loginUser, "LoginUserContext loginUser NOT NULL...");
        Preconditions.checkState(loginUser.getStore().isPresent(), "当前登陆用户无门店信息....无法执行后续操作...");
        Optional<LabelNodeEntity> label = getBean(LabelNodeAction.class).findByMixId(labelId, loginUser.getStore().orNull(),
                loginUser.getCompany().orNull());
        Preconditions.checkState(label.isPresent(), "id=%s 对应的标签定义不存在...");
        List<MemberEntity> members = null;
        if (CollectionUtils.isNotEmpty(memberIds)) {
            java.util.Optional<List<MemberEntity>> _members =  getBean(MemberEntityAction.class)
                    .findMembersByIds(loginUser.getStore().get(), memberIds, false, false);
            members = _members.isPresent() ? _members.get() : null;
        }
        List<WebChatUserEntity> weixins = null;
        if (CollectionUtils.isNotEmpty(weixinIds)) {
            Optional<List<WebChatUserEntity>> _members = getBean(WebChatUserAction.class)
                    .loadAllByIds(loginUser.getStore().get(), weixinIds);
            weixins = _members.isPresent() ? _members.get() : null;
        }
        getBean(LabelMarkedAction.class).markLables(label.get(), members, weixins, loginUser.getStore().get(), loginUser);
    }

    public void loadLabel(Integer memberId, String weixinId, LoginUserContext loginUser) {
        Preconditions.checkState(loginUser.getStore().isPresent(), "当前登陆用户无门店信息....无法执行后续操作...");
        StoreEntity store = loginUser.getStore().get();
        MemberEntity member = null;
        WebChatUserEntity weixin = null;
        if (memberId != null) {
            Set<Integer> ids = Sets.newHashSet(memberId);
            java.util.Optional<List<MemberEntity>> mbers = getBean(MemberEntityAction.class)
                    .findMembersByIds(store, ids, false, true);
            Preconditions.checkState(mbers.isPresent() && mbers.get().size() == 1, "id=%s 对应的会员不存在...", memberId);
            member = mbers.get().get(0);
            Optional<WebChatUserEntity> webChatUser = getBean(WebChatUserAction.class).findByMember(store, member);
            weixin = webChatUser.isPresent() ? webChatUser.get() : null;
        } else {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), " String weixinId 不可以为空值...");
            Optional<WebChatUserEntity> webChatUser = getBean(WebChatUserAction.class)
                    .findById(loginUser.getStore().get(), weixinId);
            Preconditions.checkState(webChatUser.isPresent(), "id=%s 对应的微信不存在.", weixinId);
            weixin = webChatUser.get();
        }
        Preconditions.checkState(member != null || weixin != null, "无法获取会员或微信账户信息....");

    }

    public void removeLabel(Long labelId, Integer memberId, String weixinId, LoginUserContext loginUser) {
        Preconditions.checkNotNull(labelId, "Long labelId NOT NULL...");
        Preconditions.checkNotNull(loginUser, "LoginUserContext loginUser NOT NULL...");
        Preconditions.checkState(loginUser.getStore().isPresent(), "当前登陆用户无门店信息....无法执行后续操作...");
        MemberEntity member = null;
        WebChatUserEntity weixin = null;
        int storeId = -1;
        if (null != memberId) {
            Set<Integer> ids = Sets.newHashSet(memberId);
            java.util.Optional<List<MemberEntity>> mbers =  getBean(MemberEntityAction.class)
                    .findMembersByIds(loginUser.getStore().get(), ids, false, true);
            Preconditions.checkState(mbers.isPresent() && mbers.get().size() == 1,
                    "id=%s 对应的会员不存在...", memberId);
            member = mbers.get().get(0);
            Preconditions.checkState(member.getStoreId().isPresent(), "数据异常，会员%s无门店绑定信息....", member.getName());
            storeId = member.getStoreId().get();
        } else {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), " String weixinId 不可以为空值...");
            Optional<WebChatUserEntity> webChatUser = getBean(WebChatUserAction.class)
                    .findById(loginUser.getStore().get(), weixinId);
            Preconditions.checkState(webChatUser.isPresent(), "id=%s 对应的微信不存在.", weixinId);
            weixin = webChatUser.get();
            storeId = weixin.getStoreId();
        }
        Optional<StoreEntity> store = getBean(StoreEntityAction.class).findById(storeId);
        Preconditions.checkState(store.isPresent(), "数据异常，id=%d对应的门店不存在...", storeId);
        Preconditions.checkState(store.get().getCompanyId().isPresent(), "门店无公司信息....");
        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class).findCompanyById(store.get().getCompanyId().get());
        Preconditions.checkState(company.isPresent(), "数据异常，id=%d对应的公司不存在...", store.get().getCompanyId());

        Optional<LabelNodeEntity> label = getBean(LabelNodeAction.class).findByMixId(labelId, store.get(), company.get());
        Preconditions.checkState(label.isPresent(), "门店%s 中id=%s 对应的标签定义不存在...", store.get().getName(), labelId);
        getBean(LabelMarkedAction.class).removeLabel(label.get(), member, weixin, loginUser.getStore().get());
    }

    /**
     * 添加备注
     *
     * @param userContext
     * @param memberId
     * @param weixinId
     * @param remarks
     * @return
     */
    public UserRemarksEntity addRemarks(LoginUserContext userContext, Integer memberId, String weixinId, String remarks) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addRemarks(user,memberId:%s,weixinId:%s,remarks:%s)", memberId, weixinId, remarks));
        Preconditions.checkNotNull(userContext);
        Preconditions.checkState(userContext.getStore().isPresent());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(remarks), "用户备注不可以为空....");
        MemberEntity member = null;
        WebChatUserEntity webChatUser = null;
        if (memberId != null) {
            java.util.Optional<MemberEntity> member_opt = getBean(MemberEntityAction.class).findMemberById(userContext.getExitsStore(), memberId);
            Preconditions.checkState(member_opt.isPresent(), "Id=%s 对应的会员不存在...");
            member = member_opt.get();
            Optional<WebChatUserEntity> weinxin_opt = getBean(WebChatUserAction.class)
                    .findByMember(userContext.getStore().get(), member_opt.get());
            if (weinxin_opt.isPresent()) {
                webChatUser = weinxin_opt.get();
                if (!Strings.isNullOrEmpty(weixinId)) {
                    Preconditions.checkState(StringUtils.equals(weixinId, webChatUser.getUserName()),
                            "传入的 memberId 与 weixinId 尚未绑定...");
                }
            }
        }

        if (!Strings.isNullOrEmpty(weixinId)) {
            Preconditions.checkState(userContext.getStore().isPresent(), "当前登陆用户无门店信息...");
            Optional<WebChatUserEntity> weinxin_opt = getBean(WebChatUserAction.class).findById(userContext.getStore().get(),
                    weixinId);
            Preconditions.checkState(weinxin_opt.isPresent(), "账户%s 对应的微信用户不存在...", weixinId);
            webChatUser = weinxin_opt.get();
        }
        return getBean(UserRemarksAction.class).addRemarks(userContext, member, webChatUser, remarks);
    }
    
    /**
     * 导购给微信好友打标签与认领
     *
     * @param employeeId
     * @param weixinId
     * @param remark
     */
    public void remarkAndSign(LoginUserContext loginUser, Integer employeeId, String weixinId, String remark) {
        Objects.requireNonNull(loginUser);
        Objects.requireNonNull(employeeId);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), "入参微信ID不能为空");
        Preconditions.checkArgument(loginUser.getStore().isPresent(),"当前登录用户无门店信息");
        StoreEntity store = loginUser.getExitsStore();
        Optional<EmployeeEntity> employeeOpt = getBean(EmployeeEntityAction.class).findEmployee(loginUser.getStore().get(), employeeId);
        Preconditions.checkState(employeeOpt.isPresent(), String.format("导购[%s]不存在", employeeId));
        EmployeeEntity employee = employeeOpt.get();
        Optional<WebChatUserEntity> weixinOpt = getBean(WebChatUserAction.class).findById(loginUser.getExitsStore(), weixinId);
        Preconditions.checkState(weixinOpt.isPresent(), String.format("门店[%s]不存在微信[%s]", loginUser.getExitsStore().getId(), weixinId));
        WebChatUserEntity weixin = weixinOpt.get();
        if (!Strings.isNullOrEmpty(remark))
            remark(loginUser, weixinId, remark);
        sign(store, employee, weixin);
    }

    private void sign(StoreEntity store, EmployeeEntity employee, WebChatUserEntity weixin) {
        Optional<WeixinGroupEntity> signGroupOpt = getBean(WeixinGroupAction.class).findGrantedSignedGroup(store, weixin);
        WeixinGroupEntity guideGroup = getBean(WeixinGroupAction.class).addAndGrantGuideIfAbsent(store, employee);
        if (signGroupOpt.isPresent()) {
            if (signGroupOpt.get().isIdSame(guideGroup)) return;
            getBean(WeixinGroupAction.class).removeFriends(signGroupOpt.get(), Lists.newArrayList(weixin));
        }
        getBean(WechatSignAction.class).sign(employee, weixin);
        getBean(WeixinGroupAction.class).addFriends(guideGroup, Lists.newArrayList(weixin));
    }

    private void remark(LoginUserContext loginUser, String weixinId, String remark) {
        getBean(LabelMarkedService.class).addBindRemarks(loginUser, weixinId, remark);
    }

    /**
     * 添加备注
     *
     * @param userContext
     * @param weixinId
     * @param remarks
     * @return
     */
    public UserRemarksEntity addBindRemarks(LoginUserContext userContext, String weixinId, String remarks) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addRemarks(user,weixinId:%s,remarks:%s)", weixinId, remarks));
        Preconditions.checkNotNull(userContext);
        Preconditions.checkState(userContext.getStore().isPresent());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(remarks), "用户备注不可以为空....");
        WebChatUserEntity webChatUser = null;
        if (!Strings.isNullOrEmpty(weixinId)) {
            Preconditions.checkState(userContext.getStore().isPresent(), "当前登陆用户无门店信息...");
            Optional<WebChatUserEntity> weinxin_opt = getBean(WebChatUserAction.class).findById(userContext.getStore().get(),
                    weixinId);
            Preconditions.checkState(weinxin_opt.isPresent(), "账户%s 对应的微信用户不存在...", weixinId);
            webChatUser = weinxin_opt.get();
        }
        return getBean(UserRemarksAction.class).addBindRemarks(userContext, webChatUser, remarks);
    }

    @Subscribe
    public void memberWithWeixinEventListener(MemberWithWeixinEvent event) {
        try {
            LoginUserContext loginUser = event.getUser();
            Preconditions.checkNotNull(loginUser, "操作账户为空，无法执行后续操作...");
            if (event.isBildEvent()) {
                getBean(LabelMarkedAction.class).mergeWxWithMm(event.getStore(), event.getWebChatUsers());
                getBean(UserRemarksAction.class).mergeWxWithMm(event.getStore(), event.getWebChatUsers());
            } else if (event.isUnBildEvent()) {
                getBean(LabelMarkedAction.class).unMergeWxWithMm(event.getStore(), event.getWebChatUser(), loginUser);
                getBean(UserRemarksAction.class).unMergeWxWithMm(event.getStore(), event.getWebChatUser(), loginUser);
            }
        } catch (Exception e) {
            logger.error("handle (MemberWithWeixinEvent e) has error", e);
        }
    }

}
