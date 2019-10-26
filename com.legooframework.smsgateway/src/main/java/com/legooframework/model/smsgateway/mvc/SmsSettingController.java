package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.crmadapter.entity.Authenticationor;
import com.legooframework.model.crmadapter.service.CrmPermissionHelper;
import com.legooframework.model.smsgateway.entity.SMSSettingEntity;
import com.legooframework.model.smsgateway.entity.SMSSettingEntityAction;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/smssetting")
public class SmsSettingController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SmsSettingController.class);

    /**
     * @param requestBody 请求负载
     * @param request     请求
     * @return EEE
     */
    @PostMapping(value = "/check/sms/prefix.json")
    public JsonMessage checkSmsPrefix(@RequestBody(required = false) Map<String, Object> requestBody,
                                      HttpServletRequest request) throws Exception {
        String prefix = MapUtils.getString(requestBody, "smsPrefix");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "入参 smsPrefix 不可以未空...");
        Preconditions.checkState(prefix.length() <= 13, "短信前缀最大长度需>=13");
        Authenticationor authentication = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        Preconditions.checkState(authentication.hasStore(), "需指定修改的门店....");
        Optional<List<SMSSettingEntity>> list = getBean(SMSSettingEntityAction.class, request)
                .checkSmsPrefix(authentication.getStore(), prefix);
        return JsonMessageBuilder.OK().withPayload(list.map(List::size).orElse(0)).toMessage();
    }

    /**
     * @param requestBody 请求负载
     * @param request     请求
     * @return EEE
     */
    @PostMapping(value = "/edit/sms/prefix.json")
    public JsonMessage settingSmsPrefix(@RequestBody(required = false) Map<String, Object> requestBody,
                                        HttpServletRequest request) throws Exception {
        String prefix = MapUtils.getString(requestBody, "smsPrefix");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "入参 smsPrefix 不可以未空...");
        Preconditions.checkState(prefix.length() <= 13, "短信前缀最大长度需>=13");
        Authenticationor authentication = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        Preconditions.checkState(authentication.hasStore(), "需指定修改的门店....");
        getBean(SMSSettingEntityAction.class, request).changeSmsPrefix(authentication.getStore(), prefix);
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * @param requestBody 请求负载
     * @param request     请求
     * @return EEE
     */
    @PostMapping(value = "/load/sms/prefix.json")
    public JsonMessage loadSmsPreix(@RequestBody(required = false) Map<String, Object> requestBody,
                                    HttpServletRequest request) throws Exception {
        Authenticationor authentication = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        if (authentication.hasStore()) {
            SMSSettingEntity settingEntity = getBean(SMSSettingEntityAction.class, request).loadByStoreId(authentication.getCompany().getId(),
                    authentication.getStore().getId());
        } else if (authentication.hasStores()) {
            List<SMSSettingEntity> my_list = getBean(SMSSettingEntityAction.class, request).loadByStoreIds(authentication.getCompany().getId(),
                    authentication.getStoreIds().toArray(new Integer[0]));
        }
        return JsonMessageBuilder.OK().toMessage();
    }

}
