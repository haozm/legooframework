package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping(value = "/charge")
public class ChargeController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ChargeController.class);

    /**
     * 返回按照批次号分类的 聚合
     *
     * @param channel
     * @param range
     * @param requestBody
     * @param request
     * @return
     */
    @PostMapping(value = "/{channel}/{range}/batch/list.json")
    public JsonMessage chargeListAction(@PathVariable(value = "channel") String channel,
                                        @PathVariable(value = "range") String range,
                                        @RequestBody(required = false) Map<String, Object> requestBody,
                                        HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("chargeListAction(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        Preconditions.checkArgument(ArrayUtils.contains(CHANNELS, channel), "非法的取值 %s,取值范围为：%s", channel, CHANNELS);
        Preconditions.checkArgument(ArrayUtils.contains(RANGES, range), "非法的取值 %s,取值范围为：%s", range, RANGES);
        int pageNum = MapUtils.getIntValue(requestBody, "pageNum", 1);
        int pageSize = MapUtils.getIntValue(requestBody, "pageSize", 10);
        Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);
        if (StringUtils.equals("company", range))
            companyId = getLoginContext().getTenantId().intValue();

        int storeId = MapUtils.getIntValue(requestBody, "storeId", -1);
        String storeIds = MapUtils.getString(requestBody, "storeIds");
        String smsContext = MapUtils.getString(requestBody, "smsContext");
        String createTime = MapUtils.getString(requestBody, "createTime", null);
        int sendMode = MapUtils.getInteger(requestBody, "sendMode", -1);
        int shoppingGuideId = MapUtils.getInteger(requestBody, "shoppingGuideId", -1);
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        if (-1 != storeId) params.put("storeId", storeId);
        if (-1 != shoppingGuideId) params.put("shoppingGuideId", shoppingGuideId);
        if (!Strings.isNullOrEmpty(storeIds)) params.put("storeIds", storeIds);
        if (!Strings.isNullOrEmpty(smsContext)) params.put("smsContext", String.format("%%%s%%", smsContext));
        if (!Strings.isNullOrEmpty(createTime)) params.put("createTime", createTime);
        if (sendMode != -1) params.put("sendMode", sendMode);
        PagingResult page = getQueryEngine(request).queryForPage("ChargeDetailEntity", "batch_list", pageNum, pageSize, params);
        return JsonMessageBuilder.OK().withPayload(page.toData()).toMessage();
    }

    /**
     * 返回指定批次号的发送明细
     *
     * @param channel
     * @param requestBody
     * @param request
     * @return
     */
    @PostMapping(value = "/{channel}/detail/list.json")
    public JsonMessage chargeDetailListAction(@PathVariable(value = "channel") String channel,
                                              @RequestBody(required = false) Map<String, Object> requestBody,
                                              HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("chargeDetailListAction(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        Preconditions.checkArgument(ArrayUtils.contains(CHANNELS, channel), "非法的取值 %s,取值范围为：%s", channel, CHANNELS);
        int pageNum = MapUtils.getIntValue(requestBody, "pageNum", 1);
        int pageSize = MapUtils.getIntValue(requestBody, "pageSize", 10);
        String smsBatchNo = MapUtils.getString(requestBody, "smsBatchNo");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(smsBatchNo), "发送批次号 smsBatchNo 不可以为空值...");
        String phoneNo = MapUtils.getString(requestBody, "phoneNo");
        String memberName = MapUtils.getString(requestBody, "memberName", null);
        int sendStatus = MapUtils.getIntValue(requestBody, "sendStatus", -1);
        Preconditions.checkState(sendStatus == -1 || sendStatus == 0 || sendStatus == 1 || sendStatus == 2,
                "非法的状态值 %s,合法取值[0,1,2]", sendStatus);
        Map<String, Object> params = Maps.newHashMap();
        params.put("smsBatchNo", smsBatchNo);
        if (!Strings.isNullOrEmpty(phoneNo)) params.put("phoneNo", phoneNo);
        if (!Strings.isNullOrEmpty(memberName)) params.put("memberName", String.format("%%%s%%", memberName));
        if (-1 != sendStatus) params.put("sendStatus", sendStatus);
        PagingResult page = getQueryEngine(request).queryForPage("ChargeDetailEntity", "detail_list", pageNum, pageSize, params);
        return JsonMessageBuilder.OK().withPayload(page.toData()).toMessage();
    }

    JdbcQuerySupport getQueryEngine(HttpServletRequest request) {
        return getBean(JdbcQuerySupport.class, request);
    }

}
