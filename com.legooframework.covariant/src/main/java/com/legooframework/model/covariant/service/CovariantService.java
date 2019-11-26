package com.legooframework.model.covariant.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.EntityNotExitsException;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.covariant.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CovariantService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(CovariantService.class);

    /**
     * OOXX
     *
     * @param storeId Integer
     * @return StoreAgg
     */
    StoreAgg loadStoreAgg(Integer storeId) {
        StoEntity store = storeAction.loadById(storeId);
        Optional<List<EmpEntity>> emps = employeeAction.findEmpsByStore(store);
        return new StoreAgg(store, emps.orElse(null));
    }

    public EmployeeAgg loadEmployeeAgg(Integer empId) {
        Optional<EmpEntity> employee = employeeAction.findById(empId);
        Preconditions.checkState(employee.isPresent(), String.format("empId=%d 对应的职员不存在...", empId));
        StoEntity store = null;
        if (employee.get().hasStore() && employee.get().getStoreId().isPresent()) {
            store = storeAction.loadById(employee.get().getStoreId().get());
        }
        EmployeeAgg agg = new EmployeeAgg(employee.get(), store);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadEmployeeAgg(%s) is %s", empId, agg));
        return agg;
    }

    public WxUserAgg loadWxUserAgg(Integer storeId, String userName) {
        StoEntity store = storeAction.loadById(storeId);
        Optional<WxUserEntity> wxUser = wxUserAction.findById(store, userName);
        Preconditions.checkState(wxUser.isPresent(), String.format("门店%d 对应的%s 微信不存在", store.getId(), userName));
        Optional<MemberEntity> member = memberAction.findByWxUser(wxUser.get());
        EmpEntity shoppingGuide = null;
        if (member.isPresent() && member.get().getShoppingGuideId().isPresent()) {
            try {
                shoppingGuide = employeeAction.loadById(member.get().getShoppingGuideId().get());
            } catch (EntityNotExitsException e) {
                logger.error(String.format("Member=%s 含导购 %s 对应的实体不存在...", member.get().getId(),
                        member.get().getShoppingGuideId().get()), e);
                shoppingGuide = null;
            }
        }
        WxUserAgg agg = new WxUserAgg(wxUser.get(), store, member.orElse(null), shoppingGuide);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadWxUserAgg(%s,%s) is %s", storeId, userName, agg));
        return agg;
    }

    /**
     * OOXX
     *
     * @param memberId Integer
     * @return StoreAgg
     */
    public MemberAgg loadMemberAgg(Integer memberId) {
        Optional<MemberEntity> member = memberAction.findById(memberId);
        Preconditions.checkState(member.isPresent());
        StoEntity store = storeAction.loadById(member.get().getStoreId());
        EmpEntity shoppingGuide = null;
        if (member.get().getShoppingGuideId().isPresent()) {
            try {
                shoppingGuide = employeeAction.loadById(member.get().getShoppingGuideId().get());
            } catch (EntityNotExitsException e) {
                logger.error(String.format("Member=%s 含导购 %s 对应的实体不存在...", member.get().getId(),
                        member.get().getShoppingGuideId().get()), e);
                shoppingGuide = null;
            }
        }
        Optional<WxUserEntity> wxUser = wxUserAction.findByMember(member.get());
        Optional<EWeiShopMemberEntity> shopUser = eWeiShopMemberAction.findByMember(member.get());
        MemberAgg agg = new MemberAgg(member.get(), store, shoppingGuide, wxUser.orElse(null), shopUser.orElse(null));
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadMemberAgg(%s) is %s", memberId, agg));
        return agg;
    }

    public void sendSmsByStore(Integer storeId, String content, String phone, String name, BusinessType businessType,
                               String batchNo) {
        StoEntity store = storeAction.loadById(storeId);
        SendSmsEntity sms = SendSmsEntity.createSmsByStore(content, phone, name, store, businessType, batchNo, null);
        try {
            getBean(SmsBalanceEntityAction.class).billing(store, sms.getSmsCount());
        } catch (SmsBillingException e) {
            logger.error(String.format("sendSmsByStore(storeId=%s,phon=%s,batchNo=%s)  has error", storeId, phone, batchNo), e);
            sms.setError(e.getMessage());
        }
        getBean(SendSmsEntityAction.class).batchAdd4Send(Lists.newArrayList(sms));
    }

    public void preSendSmsByStore(Integer storeId, Integer memberId, Collection<Integer> empIds,
                                  BusinessType businessType, String ctxTemp) {
        StoEntity store = storeAction.loadById(storeId);
        String batchNo = UUID.randomUUID().toString();
        MemberAgg memberAgg = loadMemberAgg(memberId);
        String sms_prefix = getBean(SendSmsEntityAction.class).getSmsPrefix(store);
        String content = replace(ctxTemp, memberAgg.toReplaceMap());
        Optional<List<EmpEntity>> _emps = employeeAction.findEmpsByStore(store, empIds);
        List<EmpEntity> emps_list = _emps.orElse(null);
        Preconditions.checkState(CollectionUtils.isNotEmpty(emps_list), "指定人员ID为空...");
        List<SendSmsEntity> sms_list = Lists.newArrayList();
        emps_list.forEach(x -> {
            SendSmsEntity sms = SendSmsEntity.createSmsByStore(sms_prefix + content, x.getPhone(), x.getName(), store, businessType, batchNo, null);
            sms_list.add(sms);
        });
        int sms_sum = sms_list.stream().mapToInt(SendSmsEntity::getSmsCount).sum();
        try {
            getBean(SmsBalanceEntityAction.class).billing(store, sms_sum);
        } catch (SmsBillingException e) {
            logger.error(String.format("sendSmsByStore(storeId=%s,batchNo=%s)  has error", storeId, batchNo), e);
        }
        getBean(SendSmsEntityAction.class).batchAdd4Send(sms_list);
    }

    public String preViewSmsByStore(Integer memberId, String ctxTemp) {
        MemberAgg memberAgg = loadMemberAgg(memberId);
        String prefix = getBean(SendSmsEntityAction.class).getSmsPrefix(memberAgg.getStore());
        String res = replace(ctxTemp, memberAgg.toReplaceMap());
        return String.format("%s%s退订回T", prefix, res);
    }

    private String replace(String content, Map<String, Object> params) throws TemplateReplaceException {
        if (MapUtils.isEmpty(params) || Strings.isNullOrEmpty(content)) return content;
        try {
            StringSubstitutor substitutor = new StringSubstitutor(params, "{", "}");
            return substitutor.replace(content);
        } catch (Exception e) {
            throw new TemplateReplaceException(String.format("模板 %s 替换发送异常...%s", content, params), e);
        }
    }

    public void sendSmsByCompany(Integer companyId, String content, String phone, String name, BusinessType businessType, String batchNo) {
        OrgEntity company = getBean(OrgEntityAction.class).loadComById(companyId);
        SendSmsEntity sms = SendSmsEntity.createSmsByCompany(content, phone, name, company, businessType, batchNo, null);
        try {
            getBean(SmsBalanceEntityAction.class).billing(company, sms.getSmsCount());
        } catch (SmsBillingException e) {
            logger.error(String.format("sendSmsByStore(companyId=%s,phon=%s,batchNo=%s)  has error", companyId, phone, batchNo), e);
            sms.setError(e.getMessage());
        }
        getBean(SendSmsEntityAction.class).batchAdd4Send(Lists.newArrayList(sms));
    }

    public void sendSmsesByStore(Integer storeId, Collection<SendSmsEntity> sendSmses) {
        StoEntity store = storeAction.loadById(storeId);
        if (CollectionUtils.isEmpty(sendSmses)) return;
        int sms_sum = sendSmses.stream().mapToInt(SendSmsEntity::getSmsCount).sum();
        String errMsg = null;
        try {
            getBean(SmsBalanceEntityAction.class).billing(store, sms_sum);
        } catch (SmsBillingException e) {
            logger.error(String.format("sendSmsByStore(storeId=%s)  has error", storeId), e);
            errMsg = e.getMessage();
        }
        String error_msg = errMsg;
        if (!Strings.isNullOrEmpty(errMsg))
            sendSmses.forEach(sms -> sms.setError(error_msg));
        getBean(SendSmsEntityAction.class).batchAdd4Send(sendSmses);
    }

    public void sendWxMsgByStore(String msgTxt, String[] imageInfo, Collection<String> wechatIds, Integer storeId,
                                 BusinessType businessType) {
        if (CollectionUtils.isEmpty(wechatIds)) return;
        StoEntity store = storeAction.loadById(storeId);
        Preconditions.checkState(store.hasWexin(), "门店%s无微信或者设备信息.....", store.getName());
        getBean(SendWechatEntityAction.class).sendMsg(msgTxt, imageInfo, wechatIds, store, businessType);
    }

    public Optional<List<Integer>> loadMemberIds(Map<String, Object> queryParams, UserAuthorEntity user) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("builderByOther(queryParams:%s)", queryParams));
        int queryType = MapUtils.getIntValue(queryParams, "queryType", 1);
        Preconditions.checkArgument(ArrayUtils.contains(new int[]{1, 2, 3, 4}, queryType), "非法的入参 queryType = %d", queryType);
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now();
        switch (queryType) {
            case 1:
                start = LocalDate.now();
                end = LocalDate.now();
                break;
            case 2:
                start = start.dayOfWeek().withMinimumValue();
                end = end.dayOfWeek().withMaximumValue();
                break;
            case 3:
                start = start.dayOfMonth().withMinimumValue();
                end = end.dayOfMonth().withMaximumValue();
                break;
            case 4:
                start = start.plusMonths(1).dayOfMonth().withMinimumValue();
                end = end.plusMonths(1).dayOfWeek().withMaximumValue();
                break;
            default:
                ;
        }
        Map<String, Object> params = user.toViewMap();
        params.putAll(queryParams);
        params.put("birthday", String.format("%s,%s", start.toString("yyyy-MM-dd"), end.toString("yyyy-MM-dd")));
        JdbcQuerySupport querySupport = getBean("covariantJdbcQuerySupport", JdbcQuerySupport.class);
        MemberQueryHelper.QuerySql querySql = MemberQueryHelper
                .builderByOther(user.getCompanyId(), user.getStoreId().orElse(0), params, querySupport);
        List<Integer> memberIds = querySupport.getJdbcTemplate().queryForList(querySql.toString(), Integer.class);
        return Optional.ofNullable(CollectionUtils.isEmpty(memberIds) ? null : memberIds);
    }

    private MemberEntityAction memberAction;
    private StoEntityAction storeAction;
    private EmpEntityAction employeeAction;
    private WxUserEntityAction wxUserAction;
    private EWeiShopMemberEntityAction eWeiShopMemberAction;

    public void seteWeiShopMemberAction(EWeiShopMemberEntityAction eWeiShopMemberAction) {
        this.eWeiShopMemberAction = eWeiShopMemberAction;
    }

    public void setEmployeeAction(EmpEntityAction employeeAction) {
        this.employeeAction = employeeAction;
    }

    public void setWxUserAction(WxUserEntityAction wxUserAction) {
        this.wxUserAction = wxUserAction;
    }

    public void setStoreAction(StoEntityAction storeAction) {
        this.storeAction = storeAction;
    }

    public void setMemberAction(MemberEntityAction memberAction) {
        this.memberAction = memberAction;
    }

}
