package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.membercare.entity.AutoRunChannel;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsgateway.entity.SendMode;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SmsGatewayProxyAction extends BaseEntityAction<EmptyEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SmsGatewayProxyAction.class);

    public SmsGatewayProxyAction() {
        super(null);
    }

    /**
     * @param token      AA
     * @param companyId  CC
     * @param storeId    AA
     * @param employeeId SS
     */
    private void checkParams(String token, Integer companyId, Integer storeId, Integer employeeId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(token), "当前请求无法获取合法token...");
        Preconditions.checkNotNull(companyId);
        Preconditions.checkNotNull(storeId);
        Preconditions.checkNotNull(employeeId);
    }

    /**
     * @param payloads [deitalID,memerbId,autoSendChannel||template]
     *                 其中 deitalID：任务ID，memerbId：会员ID，autoSendChannel：发送模式
     * @param template 待发送的模板， 当template 赋值时，payloads 协议修改为 deitalID,memerbId,autoSendChannel
     * @return MM
     */
    public String sendMessageProxy(Integer companyId, Integer storeId, Integer employeeId, List<String> payloads,
                                   String template,
                                   BusinessType businessType, boolean authorization, HttpServletRequest request) {
        String token = request == null ? WebUtils.SECURE_ANONYMOUS_TOKEN : request.getHeader(KEY_HEADER);
        checkParams(token, companyId, storeId, employeeId);
        final String batchNo = UUID.randomUUID().toString();
        if (payloads.size() == 1) {
            Map<String, Object> params = enCoding(companyId, storeId, employeeId, payloads, template, businessType, authorization,
                    batchNo, true, SendMode.ManualSingle);
            Optional<JsonElement> jsonElement = super.postWithToken(companyId, "smsgateway.sendMessageUrl", token, params);
            return jsonElement.map(JsonElement::getAsString).orElse(null);
        }

        List<List<String>> payload_list = Lists.partition(payloads, batchSize);
        final int payload_size = payload_list.size();
        int index = 0;
        Optional<JsonElement> jsonElement = Optional.empty();
        if (payload_size == 1) {
            Map<String, Object> params = enCoding(companyId, storeId, employeeId, payload_list.get(0), template, businessType,
                    authorization, batchNo, true, SendMode.ManualBatch);
            jsonElement = super.postWithToken(companyId, "smsgateway.sendMessageUrl", token, params);
        } else {
            for (List<String> $it : payload_list) {
                Map<String, Object> params = enCoding(companyId, storeId, employeeId, $it, template, businessType, authorization,
                        batchNo, index == payload_size - 1, SendMode.ManualBatch);
                jsonElement = super.postWithToken(companyId, "smsgateway.sendMessageUrl", token, params);
                index++;
            }
        }
        return jsonElement.map(JsonElement::getAsString).orElse(null);
    }

    // deitalID,memerbId,autoChannel||template@
    // deitalID,memerbId,autoChannel||template
    private static Map<String, Object> enCoding(Integer companyId, Integer storeId, Integer employeeId, List<String> payload,
                                                String template, BusinessType businessType, boolean authorization,
                                                String uuid, boolean end,
                                                SendMode sendMode) {
        Map<String, Object> params = Maps.newHashMap();
        if (Strings.isNullOrEmpty(template)) {
            params.put("payload", StringUtils.join(payload, '@'));
        } else {
            String _encode_temp = WebUtils.encodeUrl(template);
            List<String> _payload = Lists.newArrayListWithCapacity(payload.size());
            payload.forEach(x -> _payload.add(String.format("%s||%s", x, _encode_temp)));
            params.put("payload", StringUtils.join(_payload, '@'));
        }
        params.put("sId", storeId);
        params.put("storeId", storeId);
        params.put("cId", companyId);
        params.put("companyId", companyId);
        params.put("empId", employeeId);
        params.put("businessType", businessType.toString());
        params.put("authorization", authorization);
        params.put("sendMode", sendMode.getMode());
        params.put("batchNo", uuid);
        params.put("end", end);
        return params;
    }

    public static SendMessageDto deCoding(Map<String, Object> requestBody) {
        Integer companyId = MapUtils.getInteger(requestBody, "cId");
        Integer storeId = MapUtils.getInteger(requestBody, "sId");
        Integer employeeId = MapUtils.getInteger(requestBody, "empId");
        boolean authorization = MapUtils.getBooleanValue(requestBody, "authorization", false);
        String template = MapUtils.getString(requestBody, "template");
        SendMode sendMode = SendMode.paras(MapUtils.getInteger(requestBody, "sendMode"));
        String batchNo = MapUtils.getString(requestBody, "batchNo"); // 批次号
        boolean end = MapUtils.getBoolean(requestBody, "end"); // 是否结束标识
        String businessType_str = MapUtils.getString(requestBody, "businessType");
        String payload_str = MapUtils.getString(requestBody, "payload");
        List<String> payload_list = Stream.of(StringUtils.split(payload_str, '@')).collect(Collectors.toList());
        SendMessageDto dto = new SendMessageDto(companyId, storeId, employeeId, authorization,
                end, template, batchNo, sendMode, EnumUtils.getEnum(BusinessType.class, businessType_str), payload_list);
        if (logger.isDebugEnabled())
            logger.debug(String.format("SendMessageDto:%s", dto));
        return dto;
    }

    private int batchSize = 500;

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
