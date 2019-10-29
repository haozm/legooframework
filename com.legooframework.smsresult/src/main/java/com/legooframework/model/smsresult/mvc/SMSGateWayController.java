package com.legooframework.model.smsresult.mvc;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.smsresult.entity.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
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

@Controller
public class SMSGateWayController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SMSGateWayController.class);

    /**
     * 接受短信进行存储,用户稍后发送 协议如下
     * {id,content,mobile,count,sum,cId,sId,channel,status}
     * %s|0000|OK  %s wei id
     *
     * @param requestBody 请求
     * @param request     HttpServletRequest
     * @return JsonMessage
     */
    @PostMapping(value = "/smses/batch/sending.json")
    @ResponseBody
    public JsonMessage acceptSms4Sending(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug("acceptSms4Sending(payload=...) start");
        LoginContextHolder.setAnonymousCtx();
        String smsId = null;
        try {
            String payload_smses = MapUtils.getString(requestBody, "payload");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(payload_smses), "短信发送有效数据为空...");
            String[] payload_sms = StringUtils.splitByWholeSeparator(payload_smses, "||");
            List<String> result = Lists.newArrayListWithCapacity(payload_sms.length);
            for (String $it : payload_sms) {
                try {
                    String[] sms = StringUtils.split($it, '|');
                    if (logger.isDebugEnabled())
                        logger.debug("[HXJ-SMS]" + Arrays.toString(sms));
                    smsId = sms[0];
                    SMSSendAndReceiveEntity instance = SMSSendTransportProtocol.decodingByFlat(sms);
                    getBean(SMSSendAndReceiveEntityAction.class, request).add4Insert(instance);
                    result.add(String.format("%s|0000|OK", instance.getId()));
                } catch (Exception e) {
                    logger.error("acceptSms4Sending(...) has error", e);
                    result.add(String.format("%s|9999|短信内容解码异常", smsId));
                }
            }
            return JsonMessageBuilder.OK().withPayload(StringUtils.join(result, "||")).toMessage();
        } catch (Exception e) {
            logger.error("acceptSms4Sending(%s) has error", e);
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
    @PostMapping(value = "/finalstate/fetching/bymsgId.json")
    @ResponseBody
    public JsonMessage syncFialState(@RequestBody(required = false) Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("syncFialState(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            String smsIds = MapUtils.getString(requestBody, "smsIds");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(smsIds), "带查询的短信资料不可以为空值...");
            List<String> smsIds_list = Stream.of(StringUtils.split(smsIds, ',')).collect(Collectors.toList());
            Optional<List<SMSSendAndReceiveEntity>> final_smses_opt = getBean(SMSSendAndReceiveEntityAction.class, request)
                    .loadByIds(smsIds_list);
            List<String> res_list = Lists.newArrayList();
            String today_time = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
            if (final_smses_opt.isPresent()) {
                smsIds_list.forEach(smsId -> {
                    Optional<SMSSendAndReceiveEntity> opt = final_smses_opt.get().stream()
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
            if (logger.isDebugEnabled())
                logger.debug(String.format("syncFialState() return %s", StringUtils.join(res_list, "|||")));
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