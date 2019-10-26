package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.crmadapter.entity.*;
import com.legooframework.model.crmadapter.service.CrmPermissionHelper;
import com.legooframework.model.membercare.entity.AutoRunChannel;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.membercare.entity.SendMessageTemplate;
import com.legooframework.model.smsgateway.entity.*;
import com.legooframework.model.smsgateway.service.BundleService;
import com.legooframework.model.smsgateway.service.SmsTempCacheService;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import com.legooframework.model.smsprovider.entity.SMSProviderEntity;
import com.legooframework.model.smsprovider.entity.SMSProviderEntityAction;
import com.sun.corba.se.spi.ior.ObjectKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/smssending")
public class MsgSendingController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MsgSendingController.class);

    /**
     * 手动测试发送短信
     * 必须是合法的登陆用户
     *
     * @param requestBody 请求负载
     * @param request     请求
     * @return 比例为社么是透明的 呕吐的小玲下
     */
    @PostMapping(value = "/manual/test/sms.json")
    public JsonMessage sendingMessage4Test(@RequestBody Map<String, Object> requestBody, HttpServletRequest request)
            throws Exception {
        LoginContext user = LoginContextHolder.get();
        String template = MapUtils.getString(requestBody, "template");
        boolean encoding = MapUtils.getBoolean(requestBody, "encoding", true);
        BusinessType businessType = holdBusinessTypeParam(requestBody);
        Integer companyId = user.getTenantId().intValue();
        Integer employeeId = user.getLoginId().intValue();
        Authenticationor org_info = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        String mobiles_str = MapUtils.getString(requestBody, "mobiles");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mobiles_str), "短信号码不可以为空值...");
        String[] mobiles = StringUtils.split(mobiles_str, ',');
        Integer memberId = MapUtils.getInteger(requestBody, "memberId");
        int autoRunChannel_val = MapUtils.getInteger(requestBody, "autoRunChannel", 2);
        AutoRunChannel autoRunChannel = AutoRunChannel.parse(autoRunChannel_val);
        boolean authorization = MapUtils.getBooleanValue(requestBody, "authorization", false);

        String payload = String.format("%s,%s,%s||%s", user.getTenantId(), memberId, autoRunChannel.getChannel(), template);
        List<SendMessageTemplate> job_temps = getBean(MsgTemplateProxyAction.class, request)
                .batchReplaceMemberTemplate(companyId, employeeId, Lists.newArrayList(payload), encoding,
                        authorization, request);

        Preconditions.checkState(CollectionUtils.isNotEmpty(job_temps) && job_temps.size() == 1,
                "短信内容渲染失败，数量为 0....");

        Preconditions.checkState(job_temps.get(0).isOK(), "%s 短信内容渲染失败....", job_temps.get(0).getContext());
        List<SendMessageTemplate> send_smses = Lists.newArrayListWithCapacity(mobiles.length);
        Stream.of(mobiles).forEach(x -> send_smses.add(job_temps.get(0).changeMobile(x)));
        if (logger.isDebugEnabled())
            logger.debug(String.format("本次测试短信发送数量:%s", send_smses.size()));
        String res = sendMessage(send_smses, businessType, encoding ? WebUtils.decodeUrl(template) : template,
                org_info.getStore(), SendMode.ManualSingle, user, request);
        return JsonMessageBuilder.OK().withPayload(res).toMessage();
    }


    /**
     * 预览短信或者微信信息
     * payload = jobId,memberId|jobId,memberId||template
     *
     * @param requestBody 请求实体
     * @param request     请求
     * @return 我的祖国
     */
    @PostMapping(value = "/manual/preview/msg.json")
    public JsonMessage previewSms(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContext user = LoginContextHolder.get();
        Integer companyId = user.getTenantId().intValue();
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        Preconditions.checkState(authenticationor.hasStore(), "需指定发送短信所在门店");
        Integer employeeId = authenticationor.getUser().getLoginId().intValue();
        boolean authorization = MapUtils.getBooleanValue(requestBody, "authorization", false);
        String payload = MapUtils.getString(requestBody, "payload");
        boolean encoding = MapUtils.getBoolean(requestBody, "encoding", false);

        BusinessType businessType = holdBusinessTypeParam(requestBody);
        List<SendMessageTemplate> send_smses = getBean(MsgTemplateProxyAction.class, request)
                .batchReplaceMemberTemplate(companyId, employeeId, Lists.newArrayList(payload), encoding, authorization, request);
        Preconditions.checkState(send_smses.size() > 0, "短信模板占位符替换失败...");
        SMSSettingEntity smsSetting = getBean(SMSSettingEntityAction.class, request).loadByStoreId(companyId,
                authenticationor.getStore().getId());
        final SMSSendRuleEntity sendRule = getBean(SMSSendRuleEntityAction.class, request).loadByType(businessType);

        SMSProviderEntity smsProvider = getBean(SMSProviderEntityAction.class, request).loadSMSSupplier();
        SendMessageTemplate template = send_smses.get(0);
        if (!template.isOK())
            return JsonMessageBuilder.ERROR(String.format("存在无法替换的关键字：%s", template.getContext())).toMessage();
        String context = null;
        if (sendRule.isMarketChannel()) {
            String sms_suffix = smsProvider.getSmsSuffix(SMSChannel.MarketChannel).orElse(null);
            context = sendRule.addPrefixAndSuffix(template.getContext(), smsSetting.getSmsPrefix(), sms_suffix);
        } else {
            context = sendRule.addPrefix(template.getContext(), smsSetting.getSmsPrefix());
        }

        if (logger.isDebugEnabled())
            logger.debug(String.format("本次待预览短信短信:%s", context));
        return JsonMessageBuilder.OK().withPayload(context).toMessage();
    }

    /**
     * 手动批量发送短信
     * payload = jobId,memberId,channel||templaye@jobId,memberId,channel||templaye@jobId,memberId,channel||templaye
     * 按照 门店为最小粒度 进行 发送
     * 不支持跨店发送 动作
     *
     * @param requestBody 请求负载
     * @param request     请求
     * @return EEE
     */
    @PostMapping(value = "/manual/send/message.json")
    public JsonMessage sendMessageAction(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) throws Exception {
        SendMessageDto sendMessageDto = SmsGatewayProxyAction.deCoding(requestBody);
        getBean(SmsTempCacheService.class, request).put(sendMessageDto.getBatchNo(), sendMessageDto.getPayloads());
        if (!sendMessageDto.isEnd()) return JsonMessageBuilder.OK().toMessage();
        Optional<List<String>> send_smses_payload = getBean(SmsTempCacheService.class, request).get(sendMessageDto.getBatchNo());
        if (!send_smses_payload.isPresent()) return JsonMessageBuilder.OK().toMessage();

        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class, request).findCompanyById(sendMessageDto.getCompanyId());
        Preconditions.checkState(company.isPresent(), "id=%s 对应的公司不存在 ...");
        Optional<CrmStoreEntity> store = getBean(CrmStoreEntityAction.class, request).findById(company.get(), sendMessageDto.getStoreId());
        Preconditions.checkState(store.isPresent(), "门店ID=%s 不存在...", sendMessageDto.getStoreId());

        List<SendMessageTemplate> msg_formated_list = getBean(MsgTemplateProxyAction.class, request)
                .batchReplaceMemberTemplate(sendMessageDto.getCompanyId(), sendMessageDto.getEmployeeId(),
                        sendMessageDto.getPayloads(), true, sendMessageDto.isAuthorization(), request);

        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            String sms_batchno = sendMessage(msg_formated_list, sendMessageDto.getBusinessType(), null, store.get(),
                    sendMessageDto.getSendMode(), LoginContextHolder.get(), request);
            if (logger.isDebugEnabled())
                logger.debug(String.format("本次待发送数量共计:%s,发送结果：%s", msg_formated_list.size(), sms_batchno));
            return JsonMessageBuilder.OK().withPayload(sms_batchno).toMessage();
        } catch (Exception e) {
            logger.error("sendMessage(...) gas error", e);
            throw e;
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 职业全职猎人
     *
     * @param requestBody 请求负载
     * @return EEE
     */
    private BusinessType holdBusinessTypeParam(Map<String, Object> requestBody) {
        String businessType_str = MapUtils.getString(requestBody, "businessType");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(businessType_str), "发送业务类型不可为空值....");
        Preconditions.checkState(EnumUtils.isValidEnum(BusinessType.class, businessType_str), "不支持%s对应的业务类型....",
                businessType_str);
        return EnumUtils.getEnum(BusinessType.class, businessType_str);
    }

    private MessagingTemplate getMessagingTemplate(HttpServletRequest request) {
        return getBean("smsMessagingTemplate", MessagingTemplate.class, request);
    }

    /**
     * @param send_smses   待发送的短信数量
     * @param businessType 业务
     * @param template     模板内容
     * @param store        门店
     * @param user         用户
     * @param request      请求
     * @return EEE
     * @throws Exception 异常
     */
    private String sendMessage(List<SendMessageTemplate> send_smses, BusinessType businessType, String template,
                               CrmStoreEntity store, SendMode sendMode, LoginContext user, HttpServletRequest request)
            throws Exception {
        List<SMSEntity> smses = Lists.newArrayListWithCapacity(send_smses.size());
        send_smses.forEach(x -> smses.addAll(SMSEntity.createSMSMsg(x)));
        if (logger.isDebugEnabled()) {
            long sms_size = smses.stream().filter(SMSEntity::isSMSMsg).count();
            logger.debug(String.format("本次处理消息共计 %s 条，其中短信 %s 条， 微信 %s 条", smses.size(), sms_size, smses.size() - sms_size));
        }
        DeductionReqDto payload = new DeductionReqDto(store, businessType, smses, template, sendMode);
        Message<DeductionReqDto> message = MessageBuilder.withPayload(payload).setHeader("user", user)
                .setHeader("action", "charge").build();
        Message<?> result = getMessagingTemplate(request).sendAndReceive(BundleService.CHANNEL_SMS_BILLING, message);
        Preconditions.checkNotNull(result);
        Object rsp = result.getPayload();
        if (rsp instanceof Exception) throw (Exception) rsp;
        return (String) rsp;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager(HttpServletRequest request) {
        return getBean("transactionManager", PlatformTransactionManager.class, request);
    }
}
