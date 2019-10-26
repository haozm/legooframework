package com.legooframework.model.wechatcircle.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.wechatcircle.service.UnReadStatistics;
import com.legooframework.model.wechatcircle.service.WechatCircleAllSet;
import com.legooframework.model.wechatcircle.service.WechatCircleCommonsService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/reader")
public class CircleReaderController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(CircleReaderController.class);

    @RequestMapping(value = "/{ownerWxId}/{friendWxId}/circle.json")
    public JsonMessage readerCircleDatas(@PathVariable(value = "ownerWxId") String ownerWxId,
                                         @PathVariable(value = "friendWxId") String friendWxId,
                                         @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("url=%s,requestBody = %s", request.getRequestURI(), requestBody));
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        Preconditions.checkState(companyId != null && companyId > 0, "入参 companyId 不可以为空值或者非法... ");
        Preconditions.checkState(storeId != null && storeId > 0, "入参 storeId 不可以为空值或者非法... ");
        int pageNum = MapUtils.getIntValue(requestBody, "pageNum", 0);
        int pageSize = MapUtils.getIntValue(requestBody, "pageSize", 10);
        Optional<WechatCircleAllSet> wechatCircleSet;
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            if (StringUtils.equals("0000", ownerWxId)) {
                wechatCircleSet = getBean(WechatCircleCommonsService.class, request).loadWechatCircleDetails(ownerWxId, "0000",
                        pageNum, pageSize);
            } else {
                wechatCircleSet = getBean(WechatCircleCommonsService.class, request).loadWechatCircleDetails(ownerWxId, friendWxId,
                        pageNum, pageSize);
            }
        } finally {
            LoginContextHolder.clear();
        }
        if (!wechatCircleSet.isPresent()) return JsonMessageBuilder.OK().toMessage();
        return JsonMessageBuilder.OK().withPayload(wechatCircleSet.get().toViewMap01()).toMessage();
    }

    /**
     * @param ownerWxId   手机ID
     * @param circleId    圈子ID
     * @param requestBody 请求
     * @param request     请求
     * @return 请求
     */
    @RequestMapping(value = "/{ownerWxId}/{circleId}/single.json")
    public JsonMessage readerCircleById(@PathVariable(value = "ownerWxId") String ownerWxId,
                                        @PathVariable(value = "circleId") String circleId,
                                        @RequestBody(required = false) Map<String, Object> requestBody,
                                        HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("readerCircleById(url=%s,requestBody = %s)", request.getRequestURI(), requestBody));
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            Optional<WechatCircleAllSet> wechatCircleSet = getBean(WechatCircleCommonsService.class, request)
                    .loadSingleWechatCircle(ownerWxId, Long.valueOf(circleId));
            if (!wechatCircleSet.isPresent()) return JsonMessageBuilder.OK().toMessage();
            return JsonMessageBuilder.OK().withPayload(wechatCircleSet.get().toViewMap01()).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * @param ownerWxId OOXX
     * @param request   XXKSS
     * @return OSD
     */
    @RequestMapping(value = "/{companyId}/{storeId}/{ownerWxId}/unread/comments.json")
    public JsonMessage unReadComments(@PathVariable(value = "companyId") Integer companyId,
                                      @PathVariable(value = "storeId") Integer storeId,
                                      @PathVariable(value = "ownerWxId") String ownerWxId, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("unReadComments(... )url=%s,", request.getRequestURI()));
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            Optional<WechatCircleAllSet> wechatCircleSet = getBean(WechatCircleCommonsService.class, request)
                    .loadUnReadComments(ownerWxId);
            if (!wechatCircleSet.isPresent()) return JsonMessageBuilder.OK().withHeader("size", 0).toMessage();
            return JsonMessageBuilder.OK().withHeader("size", wechatCircleSet.get().getSize())
                    .withPayload(wechatCircleSet.get().toViewMap01()).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/{companyId}/{storeId}/{ownerWxId}/unread/count.json")
    public JsonMessage unReadStatistics(@PathVariable(value = "companyId") Integer companyId,
                                        @PathVariable(value = "storeId") Integer storeId,
                                        @PathVariable(value = "ownerWxId") String ownerWxId,
                                        @RequestBody(required = false) Map<String, Object> requestBody,
                                        HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("unReadStatistics(... )url=%s,", request.getRequestURI()));
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            if (StringUtils.equals(ownerWxId, "all")) {
                String ownerWxIds = MapUtils.getString(requestBody, "ownerWxIds");
                if (Strings.isNullOrEmpty(ownerWxIds)) return JsonMessageBuilder.OK().toMessage();
                List<UnReadStatistics> list = getBean(WechatCircleCommonsService.class, request)
                        .loadAllUnReaderCount(Lists.newArrayList(StringUtils.split(ownerWxIds, ',')));
                List<String> res = list.stream().map(UnReadStatistics::toViewMap).collect(Collectors.toList());
                return JsonMessageBuilder.OK().withPayload(StringUtils.join(res, "||")).toMessage();
            }
            UnReadStatistics unRead = getBean(WechatCircleCommonsService.class, request).loadUnReaderCount(ownerWxId);
            return JsonMessageBuilder.OK().withPayload(unRead.toViewMap()).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

}

