package com.legooframework.model.smsresult.mvc;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.smsgateway.entity.MsgEntity;
import com.legooframework.model.smsresult.entity.SMSBlackListEntity;
import com.legooframework.model.smsresult.entity.SMSBlackListEntityAction;
import com.legooframework.model.smsresult.entity.SMSResultEntity;
import com.legooframework.model.smsresult.entity.SMSResultEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller(value = "SMSGateWayController")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);

    @GetMapping(value = "/welcome.json")
    @ResponseBody
    public JsonMessage welcome(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("welcome(url=%s)", request.getRequestURI()));
        Bundle bundle = getBean("smsResultBundle", Bundle.class, request);
        return JsonMessageBuilder.OK().withPayload(bundle.toDesc()).toMessage();
    }

    private static SMSResultEntity decoding(String[] payload) {
        Preconditions.checkState(payload.length == 9, "报文格式异常,数据缺失");
        try {
            String smsId = payload[0];
            int companyId = Integer.parseInt(payload[1]);
            int storeId = Integer.parseInt(payload[2]);
            int channel = Integer.parseInt(payload[3]);
            String mobile = payload[4];
            int count = Integer.parseInt(payload[5]);
            int sum = Integer.parseInt(payload[6]);
            boolean encoding = StringUtils.equals("1", payload[7]);
            String content = encoding ? WebUtils.decodeUrl(payload[8]) : payload[8];
            MsgEntity sendSMS = MsgEntity.create4Sending(smsId, content, mobile, count, sum);
            return new SMSResultEntity(companyId, storeId, sendSMS, channel, RandomUtils.nextLong(1L, 9999999999L));
        } catch (Exception e) {
            logger.error(String.format("decodingByFlat(%s) has exception", Arrays.toString(payload)), e);
            throw new RuntimeException("报文解析异常", e);
        }
    }

    /**
     * 接受短信进行存储,用户稍后发送 协议如下
     * smsId|companyId|storeId|channel|mobile|count|sum|encoding|content
     * %s|0000|OK  %s wei id
     *
     * @param requestBody 请求
     * @param request     HttpServletRequest
     * @return JsonMessage
     */
    @PostMapping(value = "/smses/batch/sending.json")
    @ResponseBody
    public JsonMessage batchSMSSending(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug("batchSMSSending(payload=...) start");
        LoginContextHolder.setAnonymousCtx();
        String smsId = null;
        try {
            String payload_smses = MapUtils.getString(requestBody, "payload");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(payload_smses), "短信发送有效数据为空...");
            String[] payload_sms = StringUtils.splitByWholeSeparator(payload_smses, "||");
            List<String> result = Lists.newArrayListWithCapacity(payload_sms.length);
            List<SMSResultEntity> cacheList = Lists.newArrayList();
            for (String $it : payload_sms) {
                try {
                    String[] sms_args = StringUtils.split($it, '|');
                    if (logger.isTraceEnabled())
                        logger.trace("[SMS-GATEWAT]" + Arrays.toString(sms_args));
                    smsId = sms_args[0];
                    SMSResultEntity instance = decoding(sms_args);
                    cacheList.add(instance);
                    result.add(String.format("%s|0000|OK", instance.getId()));
                    if (cacheList.size() >= 512) {
                        getBean(SMSResultEntityAction.class, request).batchInsert(cacheList);
                        cacheList.clear();
                    }
                } catch (Exception e) {
                    logger.error(String.format("batchSMSSending(%s) has error", $it), e);
                    result.add(String.format("%s|9999|短信接受异常", smsId));
                }
            }
            if (CollectionUtils.isNotEmpty(cacheList)) {
                getBean(SMSResultEntityAction.class, request).batchInsert(cacheList);
                cacheList.clear();
            }
            return JsonMessageBuilder.OK().withPayload(StringUtils.join(result, "||")).toMessage();
        } catch (Exception e) {
            logger.error("batchSMSSending(%s) has error", e);
            return JsonMessageBuilder.ERROR("9999", "请求数据异常").toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 返回按照批次号分类的 聚合
     *
     * @param requestBody 请求负载
     * @param request     网络请求 云妮
     * @return 无敌破环王
     */
    @PostMapping(value = "/smses/state/fetching.json")
    @ResponseBody
    public JsonMessage fetchingSMSState(@RequestBody(required = false) Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("fetchingSMSState(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            String mode = MapUtils.getString(requestBody, "mode", "BYSMSId");
            List<String> res_list = Lists.newArrayList();
            if (StringUtils.equals("BYSMSId", mode)) {
                String smsIds = MapUtils.getString(requestBody, "payload");
                Preconditions.checkArgument(!Strings.isNullOrEmpty(smsIds), "带查询的短信资料不可以为空值...");
                List<String> smsIds_list = Stream.of(StringUtils.split(smsIds, ',')).collect(Collectors.toList());
                Optional<List<SMSResultEntity>> final_smses_opt = getBean(SMSResultEntityAction.class, request)
                        .loadByIds(smsIds_list);
                String today_time = LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss");
                if (final_smses_opt.isPresent()) {
                    final_smses_opt.get().forEach(x -> res_list.add(x.toFinalState()));
                    List<String> exits_ids = final_smses_opt.get().stream().map(BaseEntity::getId).collect(Collectors.toList());
                    smsIds_list.removeAll(exits_ids);
                    if (CollectionUtils.isNotEmpty(smsIds_list)) {
                        smsIds_list.forEach(smsId -> res_list.add(String.format("%s|9|%s|%s", smsId, today_time, "error:NOTEXITS")));
                    }
                } else {
                    smsIds_list.forEach(smsId -> res_list.add(String.format("%s|9|%s|%s", smsId, today_time, "error:NOTEXITS")));
                }
            }
            if (logger.isDebugEnabled())
                logger.debug(String.format("fetchingSMSState() return %s", StringUtils.join(res_list, "|||")));
            return JsonMessageBuilder.OK().withPayload(StringUtils.join(res_list, "|||")).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 接受短信进行存储
     *
     * @param requestBody 请求
     * @param request     HttpServletRequest
     * @return JsonMessage
     */
    @PostMapping(value = "/blacklist/sms/sync.json")
    public JsonMessage loadBlackList(@RequestBody(required = false) Map<String, Object> requestBody,
                                     HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadBlackList(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            String date_start = MapUtils.getString(requestBody, "dateStart");
            String date_end = MapUtils.getString(requestBody, "dateEnd");
            String companyIds = MapUtils.getString(requestBody, "companyIds");
            if (Strings.isNullOrEmpty(companyIds)) return JsonMessageBuilder.OK().toMessage();
            List<Integer> comIds = Stream.of(StringUtils.split(companyIds, ',')).map(Integer::valueOf).collect(Collectors.toList());
            LocalDateTime start = DateTimeUtils.parseDef(date_start);
            LocalDateTime end = DateTimeUtils.parseDef(date_end);
            Optional<List<SMSBlackListEntity>> list = getBean(SMSBlackListEntityAction.class, request)
                    .loadByInterval(start, end, comIds);
            if (list.isPresent()) {
                List<String> list_data = list.get().stream().filter(SMSBlackListEntity::enabled)
                        .map(SMSBlackListEntity::toViewPayload).collect(Collectors.toList());
                return JsonMessageBuilder.OK().withPayload(Joiner.on(',').join(list_data)).toMessage();
            } else {
                return JsonMessageBuilder.OK().toMessage();
            }
        } finally {
            LoginContextHolder.clear();
        }
    }

}
