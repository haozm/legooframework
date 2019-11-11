package com.legooframework.model.smsresult.mvc;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.smsresult.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
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


    /**
     * 接受短信进行存储,用户稍后发送 协议如下
     * smsId|companyId|storeId|channel|status|mobile|count|sum|encoding|content
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
                    SMSResultEntity instance = SMSSendTransportProtocol.decodingByFlat(sms_args);
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

    @PostMapping(value = "/smses/batch/syncstate.json")
    @ResponseBody
    public JsonMessage manualSyncState(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("manualSyncState(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            String smsIds_all = MapUtils.getString(requestBody, "smsIds", null);
            String startTime = MapUtils.getString(requestBody, "start", null);
            Preconditions.checkArgument(startTime != null, "搜索日期范围开始时间不可为空....");
            String endTime = MapUtils.getString(requestBody, "end", null);
            Collection<String> smsIds = Splitter.on(',').splitToList(smsIds_all);
        } finally {
            LoginContextHolder.clear();
        }
        return JsonMessageBuilder.OK().toMessage();
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
                String today_time = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
                if (final_smses_opt.isPresent()) {
                    smsIds_list.forEach(smsId -> {
                        Optional<SMSResultEntity> opt = final_smses_opt.get().stream()
                                .filter(x -> StringUtils.equals(x.getId(), smsId)).findFirst();
                        if (opt.isPresent()) {
                            if (opt.get().hasResult())
                                res_list.add(opt.get().toFinalState());
                        } else {
                            res_list.add(String.format("%s|4|2|%s|%s", smsId, today_time, "error:NOTEXITS"));
                        }
                    });
                } else {
                    smsIds_list.forEach(smsId -> res_list.add(String.format("%s|4|2|%s|%s", smsId, today_time, "error:NOTEXITS")));
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
