package com.csosm.module.material;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.commons.util.ReplaceWordsUtil;
import com.csosm.module.base.entity.*;
import com.csosm.module.material.entity.MaterialBlacklistEntity;
import com.csosm.module.material.entity.MaterialDetailAction;
import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.member.entity.MemberEntityAction;
import com.csosm.module.query.QueryEngineService;
import com.csosm.module.query.entity.PagingResult;
import com.csosm.module.webchat.entity.WebChatUserAction;
import com.csosm.module.webchat.entity.WebChatUserEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class MaterialDetailService extends AbstractBaseServer {

    public Long createByUser(LoginUserContext user, int group, String json, Date deadline) {
        Preconditions.checkNotNull(user);
        // 超管管理员添加
        if (user.getEmployee().isAdmin())
            return getBean(MaterialDetailAction.class).createByCsosm(group, json, deadline);

        Optional<StoreEntity> store = user.getStore();
        Optional<OrganizationEntity> org = user.getOrganization();
        Preconditions.checkState(!(store.isPresent() && org.isPresent()), "当前登陆用户同时属于门店与组织，数据异常...");
        if (store.isPresent()) {
            Preconditions.checkState(user.getStore().isPresent(), "当前登陆用户为店长角色，但无分配门店...");
            return getBean(MaterialDetailAction.class).createByStore(group, user.getStore().get(),
                    json, deadline, user);
        }

        if (org.isPresent()) {
            return getBean(MaterialDetailAction.class).createByOrg(group, org.get(), json, deadline, user);
        }
        throw new IllegalArgumentException("当前登陆用户无组织或者门店，数据异常...");
    }

    public PagingResult loadEnbaledTalking(LoginUserContext userContext, Integer groupType, String search,
                                           int pageNum, int pageSize) {
        Preconditions.checkNotNull(userContext);
        Preconditions.checkState(userContext.getStore().isPresent(), "当前登陆账户无门店信息，无法获取门店对应话术...");
        StoreEntity store = userContext.getStore().get();
        Optional<Integer> organizationId = store.getOrganizationId();
        Optional<OrganizationEntity> organization = Optional.absent();
        if (organizationId.isPresent()) {
            organization = getBean(OrganizationEntityAction.class).findById(organizationId.get());
        }
        Set<Integer> ids = Sets.newHashSet();
        ids.add(store.getId());
        if (organization.isPresent() && !Strings.isNullOrEmpty(organization.get().getCode())) {
            String code = organization.get().getCode();
            for (String cc : StringUtils.split(code, '_')) {
                ids.add(Integer.valueOf(cc));
            }
        }
        Optional<MaterialBlacklistEntity> blacklist = getBean(MaterialDetailAction.class).findByStore(store);

        Map<String, Object> params = Maps.newHashMap();
        params.put("ids", ids);
        params.put("groupType", groupType == null ? 2 : groupType);
        if (!Strings.isNullOrEmpty(search))
            params.put("search", "%" + search + "%");
        if (blacklist.isPresent()) {
            if (!blacklist.get().isBlackEmpty())
                params.put("blacklist", blacklist.get().getBlacklist());
            if (!blacklist.get().isWhiteEmpty())
                params.put("writelist", blacklist.get().getWhitelist());
        }
        return getBean("queryEngineService", QueryEngineService.class).queryForPage("MaterialDetail", "loadEnabledTalking",
                pageNum, pageSize, params);
    }

    /**
     * 替换文本
     *
     * @param content    待替换文本内容
     * @param loginUser  当前登录用户
     * @param weixinId   微信ID
     * @param materialId 话术Id
     * @return
     */
    public String replaceWords(String content, LoginUserContext loginUser, String weixinId, Long materialId) {
        if (Strings.isNullOrEmpty(content)) return "";
        if (!ReplaceWordsUtil.hasReplaceWord(content)) return content;
        Preconditions.checkState(loginUser.getStore().isPresent(), "登录用户门店不存在");
        Optional<WebChatUserEntity> weixinOpt = getBean(WebChatUserAction.class).findById(loginUser.getStore().get(), weixinId);
        Preconditions.checkState(weixinOpt.isPresent(), "门店无该微信账号");
        Integer memberId = weixinOpt.get().getBildMemberId();
        getBean(MaterialDetailAction.class).incrementUseTimes(materialId);
        if (memberId == null) return ReplaceWordsUtil.replaceToBlank(ReplaceWordsUtil.replace(content, loginUser));
        java.util.Optional<MemberEntity> memberOpt = getBean(MemberEntityAction.class).findMemberById(loginUser.getExitsStore(), memberId);
        if (!memberOpt.isPresent())
            return ReplaceWordsUtil.replaceToBlank(ReplaceWordsUtil.replace(content, loginUser));
        return ReplaceWordsUtil.replace(content, loginUser, memberOpt.get());
    }
}
