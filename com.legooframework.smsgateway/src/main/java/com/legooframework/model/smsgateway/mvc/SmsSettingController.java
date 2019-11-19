package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.smsgateway.entity.SMSSettingEntity;
import com.legooframework.model.smsgateway.entity.SMSSettingEntityAction;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class SmsSettingController extends SmsBaseController {

    private static final Logger logger = LoggerFactory.getLogger(SmsSettingController.class);

    @RequestMapping(value = "/welcome.json")
    @ResponseBody
    public JsonMessage welcome(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("welcome(url=%s)", request.getRequestURI()));
        Bundle bundle = getBean("smsGateWayBundle", Bundle.class, request);
        return JsonMessageBuilder.OK().withPayload(bundle.toDesc()).toMessage();
    }

    /**
     * @param requestBody 请求负载
     * @param request     请求
     * @return EEE
     */
    @PostMapping(value = "/smssetting/check/sms/prefix.json")
    public JsonMessage checkSmsPrefix(@RequestBody(required = false) Map<String, Object> requestBody,
                                      HttpServletRequest request) throws Exception {
        UserAuthorEntity user = loadLoginUser(requestBody, request);
        String prefix = MapUtils.getString(requestBody, "smsPrefix");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "入参 smsPrefix 不可以未空...");
        Preconditions.checkState(prefix.length() <= 13, "短信前缀最大长度需>=13");
        Preconditions.checkState(user.hasStore(), "需指定修改的门店....");
        StoEntity store = getBean(StoEntityAction.class, request).loadById(user.getStoreId().orElse(0));
        Optional<List<SMSSettingEntity>> list = getBean(SMSSettingEntityAction.class, request)
                .checkSmsPrefix(store, prefix);
        return JsonMessageBuilder.OK().withPayload(list.map(List::size).orElse(0)).toMessage();
    }

    /**
     * @param requestBody 请求负载
     * @param request     请求
     * @return EEE
     */
    @PostMapping(value = "/smssetting/edit/sms/prefix.json")
    public JsonMessage settingSmsPrefix(@RequestBody(required = false) Map<String, Object> requestBody,
                                        HttpServletRequest request) throws Exception {
        UserAuthorEntity user = loadLoginUser(requestBody, request);
        String prefix = MapUtils.getString(requestBody, "smsPrefix");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "入参 smsPrefix 不可以未空...");
        Preconditions.checkState(prefix.length() <= 13, "短信前缀最大长度需>=13");
        Preconditions.checkState(user.hasStore(), "需指定修改的门店....");
        StoEntity store = getBean(StoEntityAction.class, request).loadById(user.getStoreId().orElse(0));
        getBean(SMSSettingEntityAction.class, request).changeSmsPrefix(store, prefix);
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * @param requestBody 请求负载
     * @param request     请求
     * @return EEE
     */
    @PostMapping(value = "/smssetting/load/sms/prefix.json")
    public JsonMessage loadSmsPreix(@RequestBody(required = false) Map<String, Object> requestBody,
                                    HttpServletRequest request) throws Exception {
        UserAuthorEntity user = loadLoginUser(requestBody, request);
        if (user.hasStore()) {
            getBean(SMSSettingEntityAction.class, request).loadByStoreId(user.getCompanyId(), user.getStoreId().orElse(0));
        } else if (user.hasStores() && user.getSubStoreIds().isPresent()) {
            getBean(SMSSettingEntityAction.class, request).loadByStoreIds(user.getCompanyId(),
                    user.getSubStoreIds().get().toArray(new Integer[0]));
        }
        return JsonMessageBuilder.OK().toMessage();
    }

}
