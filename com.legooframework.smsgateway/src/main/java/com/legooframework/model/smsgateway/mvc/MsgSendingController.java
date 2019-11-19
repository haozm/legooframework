package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsgateway.entity.*;
import com.legooframework.model.smsgateway.service.BundleService;
import com.legooframework.model.smsgateway.service.SmsTempCacheService;
import com.legooframework.model.smsprovider.entity.*;
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
public class MsgSendingController extends SmsBaseController {

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
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            Preconditions.checkState(user.hasStore());
            StoEntity store = getBean(StoEntityAction.class, request).loadById(user.getStoreId().orElse(0));
            String template = MapUtils.getString(requestBody, "template");
            OrgEntity company = loadCompanyById(user.getCompanyId(), request);
            boolean encoding = MapUtils.getBoolean(requestBody, "encoding", true);
            BusinessType businessType = holdBusinessTypeParam(requestBody);
            String mobiles_str = MapUtils.getString(requestBody, "mobiles");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(mobiles_str), "短信号码不可以为空值...");
            String[] mobiles = StringUtils.split(mobiles_str, ',');
            Integer memberId = MapUtils.getInteger(requestBody, "memberId");
            int autoRunChannel_val = MapUtils.getInteger(requestBody, "autoRunChannel", 2);
            AutoRunChannel autoRunChannel = AutoRunChannel.parse(autoRunChannel_val);
            String payload = String.format("%s,%s,%s||%s", user.getTenantId(), memberId, autoRunChannel.getChannel(), template);
            JobDetailTemplate4Replace template4Replace = new JobDetailTemplate4Replace(user.getCompanyId(), user.getId(), payload, false);
            getBean(SmsTempCacheService.class, request).replaceTemplateAction(company, user.getId(), Lists.newArrayList(template4Replace));
            List<SendMessageTemplate> job_temps = template4Replace.getJobDetails();
            Preconditions.checkState(CollectionUtils.isNotEmpty(job_temps) && job_temps.size() == 1,
                    "短信内容渲染失败，数量为 0....");
            Preconditions.checkState(job_temps.get(0).isOK(), "%s 短信内容渲染失败....", job_temps.get(0).getContext());
            List<SendMessageTemplate> send_smses = Lists.newArrayListWithCapacity(mobiles.length);
            Stream.of(mobiles).forEach(x -> send_smses.add(job_temps.get(0).changeMobile(x)));
            if (logger.isDebugEnabled())
                logger.debug(String.format("本次测试短信发送数量:%s", send_smses.size()));
            String res = sendMessage(send_smses, businessType, encoding ? WebUtils.decodeUrl(template) : template,
                    store, SendMode.ManualSingle, user, request);
            return JsonMessageBuilder.OK().withPayload(res).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 预览短信或者微信信息
     * payload = jobId,memberId,AutoRunChannel|jobId,memberId,AutoRunChannel||template
     *
     * @param requestBody 请求实体
     * @param request     请求
     * @return 我的祖国
     */
    @PostMapping(value = "/manual/preview/msg.json")
    public JsonMessage previewSms(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            Preconditions.checkState(user.hasStore());
            Integer companyId = user.getCompanyId();
            OrgEntity company = loadCompanyById(companyId, request);
            Integer employeeId = user.getId();
            BusinessType businessType = holdBusinessTypeParam(requestBody);
            SMSSettingEntity smsSetting = getBean(SMSSettingEntityAction.class, request)
                    .loadByStoreId(companyId, user.getStoreId().orElse(0));
            final SMSSendRuleEntity sendRule = getBean(SMSSendRuleEntityAction.class, request).loadByType(businessType);
            SMSProviderEntity smsProvider = getBean(SMSProviderEntityAction.class, request).loadSMSSupplier();
            String payload = MapUtils.getString(requestBody, "payload");
            JobDetailTemplate4Replace template4Replace = new JobDetailTemplate4Replace(companyId, employeeId, payload, false);
            getBean(SmsTempCacheService.class, request).replaceTemplateAction(company, employeeId, Lists.newArrayList(template4Replace));
            List<SendMessageTemplate> send_smses = template4Replace.getJobDetails();
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
        } finally {
            LoginContextHolder.clear();
        }
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
        UserAuthorEntity user = loadLoginUser(requestBody, request);
        LoginContextHolder.setAnonymousCtx();
        try {
            SendMessageDto sendMessageDto = SmsGatewayProxyAction.deCoding(requestBody);
            OrgEntity company = loadCompanyById(user.getCompanyId(), request);
            getBean(SmsTempCacheService.class, request).put(sendMessageDto.getBatchNo(), sendMessageDto.getPayloads());
            if (!sendMessageDto.isEnd()) return JsonMessageBuilder.OK().toMessage();
            Optional<List<String>> send_smses_payload = getBean(SmsTempCacheService.class, request).get(sendMessageDto.getBatchNo());
            if (!send_smses_payload.isPresent()) return JsonMessageBuilder.OK().toMessage();
            StoEntity store = loadStoreById(sendMessageDto.getStoreId(), request);
            JobDetailTemplate4Replace template4Replace = new JobDetailTemplate4Replace(user.getCompanyId(), user.getId(),
                    "", false);

            getBean(SmsTempCacheService.class, request)
                    .replaceTemplateAction(company, user.getId(), Lists.newArrayList(template4Replace));
            List<SendMessageTemplate> msg_formated_list = template4Replace.getJobDetails();

            String sms_batchno = sendMessage(msg_formated_list, sendMessageDto.getBusinessType(), null, store,
                    sendMessageDto.getSendMode(), user, request);
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
                               StoEntity store, SendMode sendMode, UserAuthorEntity user, HttpServletRequest request)
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


    private JdbcQuerySupport getJdbcQuerySupport(HttpServletRequest request) {
        return getBean("smsJdbcQuerySupport", JdbcQuerySupport.class, request);
    }
}
