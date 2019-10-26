package com.legooframework.model.wechatcircle.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.wechatcircle.entity.*;
import com.legooframework.model.wechatcircle.service.BundleService;
import com.legooframework.model.wechatcircle.service.ProtocolCodingFactory;
import com.legooframework.model.wechatcircle.service.WechatCircleCommonsService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class CircleSyncController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(CircleSyncController.class);

    @RequestMapping(value = "/sync/{weixinId}/{syncType}/lastTime.json")
    public JsonMessage syncCircleLastTime(@PathVariable(value = "weixinId") String weixinId,
                                          @PathVariable(value = "syncType") String syncType, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("syncCircleLastTime(url=%s)", request.getRequestURI()));
        Preconditions.checkArgument(ArrayUtils.contains(new String[]{"batch", "single"}, syncType), "非法的请求类型 %s",
                request.getRequestURI());
        Optional<CircleSyncCycleEntity> exits;
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            exits = getBean(CircleSyncCycleEntityAction.class, request).findById(weixinId, StringUtils.equals("batch", syncType) ? 1 : 2);
        } finally {
            LoginContextHolder.clear();
        }

        if (exits.isPresent())
            return JsonMessageBuilder.OK().withPayload(exits.get().toViewMap()).toMessage();
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", weixinId);
        params.put("syncType", syncType);
        params.put("startTime", 0L);
        params.put("lastTime", 0L);
        return JsonMessageBuilder.OK().withPayload(params).toMessage();
    }

    @PostMapping(value = "/sync/batch/lastTime.json")
    public JsonMessage batchSyncCircleLastTime(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchSyncCircleLastTime(requestBody=%s)", requestBody));
        String weixinIds = MapUtils.getString(requestBody, "weixinIds", null);
        List<String> wxIds = Lists.newArrayList(StringUtils.split(weixinIds, ','));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinIds), "参数 weixinIds 不可以为空值...");
        Optional<List<CircleSyncCycleEntity>> exits_list;
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            exits_list = getBean(CircleSyncCycleEntityAction.class, request).findByBatchWxIds(wxIds);
        } finally {
            LoginContextHolder.clear();
        }
        List<Map<String, Object>> result = Lists.newArrayList();
        exits_list.ifPresent(exits -> exits.forEach(x -> result.add(x.toViewMap())));
        for (String wxId : wxIds) {
            Optional<Map<String, Object>> opt = result.stream()
                    .filter(x -> StringUtils.equals(wxId, MapUtils.getString(x, "id"))).findFirst();
            if (opt.isPresent()) continue;
            Map<String, Object> params = Maps.newHashMap();
            params.put("id", wxId);
            params.put("syncType", "batch");
            params.put("startTime", 0L);
            params.put("lastTime", 0L);
            result.add(params);
        }
        return JsonMessageBuilder.OK().withPayload(result).toMessage();
    }

    @PostMapping(value = "/sync/{syncType}/circle.json")
    public JsonMessage syncCircleData(@PathVariable(value = "syncType") String syncType,
                                      @RequestBody(required = false) Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("syncCircleData(requestBody=%s)", requestBody));
        Preconditions.checkArgument(MapUtils.isNotEmpty(requestBody), "请求报文不可以为空....");
        Preconditions.checkArgument(ArrayUtils.contains(new String[]{"batch", "single"}, syncType), "非法的请求类型 %s",
                request.getRequestURI());
        String weixinId = MapUtils.getString(requestBody, "weixinId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), "入参 weixidId 不可以为空值...");
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Preconditions.checkNotNull(companyId, "入参 companyId 不可以为空值...");
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        Preconditions.checkNotNull(companyId, "入参 storeId 不可以为空值...");
        String permission = MapUtils.getString(requestBody, "permission");
        if (StringUtils.equals("single", syncType) && StringUtils.isNotEmpty(permission)) {
            String[] args = StringUtils.split(permission, ',');
            Preconditions.checkArgument(args.length == 3, "非法的朋友圈权限...%s", permission);
            // weixinId,0,207
            CirclePermissionEntity permissionEntity = new CirclePermissionEntity(args[0], Integer.valueOf(args[2]), weixinId);
            LoginContextHolder.setIfNotExitsAnonymousCtx();
            try {
                getBean(CirclePermissionEntityAction.class, request).saveOrUpdate(permissionEntity);
            } finally {
                LoginContextHolder.clear();
            }

        }
        String circle_json = MapUtils.getString(requestBody, "circles", null);
        if (Strings.isNullOrEmpty(circle_json) || StringUtils.equals("[]", circle_json))
            return JsonMessageBuilder.OK().withPayload(new String[0]).toMessage();
        List<WechatCircleTranDto> circleList = ProtocolCodingFactory.deCodingCircle(weixinId, companyId, storeId, circle_json);
        final CircleSyncCycleBuilder cycleBuilder = StringUtils.equals("batch", syncType) ? new CircleSyncCycleBuilder(weixinId) :
                new CircleSyncCycleBuilder();
        circleList.forEach(x -> cycleBuilder.setData(x.getWeixinId(), x.getSendTime()));
        TransactionStatus tx = startTx(request, null);
        List<WechatCircleEntity> mixCircle = null;
        try {
            mixCircle = getBean(WechatCircleCommonsService.class, request).mergeWechatCircles(circleList, cycleBuilder.builder());
            commitTx(request, tx);
        } catch (Exception e) {
            rollbackTx(request, tx);
            throw e;
        }
        if (CollectionUtils.isNotEmpty(mixCircle)) {
            List<Map<String, Object>> map_list = mixCircle.stream().filter(WechatCircleEntity::hasImages)
                    .map(WechatCircleEntity::toResultMap01).collect(Collectors.toList());
            return JsonMessageBuilder.OK().withPayload(map_list).toMessage();
        }
        return JsonMessageBuilder.OK().withPayload(new String[0]).toMessage();
    }

    /**
     * 补全朋友圈图片接口
     *
     * @param requestBody 大侠风范
     * @param request     大侠
     * @return 回家睡觉
     */
    @PostMapping(value = "/sync/circle/images.json")
    public JsonMessage addCircleImages(@RequestBody(required = false) Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addCircleImages(requestBody=%s)", requestBody));
        Preconditions.checkArgument(MapUtils.isNotEmpty(requestBody), "请求报文不可以为空....");
        String weixinId = MapUtils.getString(requestBody, "weixinId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), "入参 weixidId 不可以为空值...");
        long fieldId = MapUtils.getLongValue(requestBody, "fieldId");
        String images_json = MapUtils.getString(requestBody, "images", null);
        Preconditions.checkNotNull(images_json, "非法的朋友圈图片数据....");
        JsonParser parser = new JsonParser();
        JsonArray json_all_array = parser.parse(images_json).getAsJsonArray();
        List<WechatCircleImage> images_ctx = Lists.newArrayList();
        json_all_array.forEach(json_img -> ProtocolCodingFactory.deCodingImage(json_img.getAsJsonObject()).ifPresent(images_ctx::add));
        if (CollectionUtils.isEmpty(images_ctx)) return JsonMessageBuilder.OK().toMessage();
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        TransactionStatus tx = startTx(request, null);
        try {
            getBean(WechatCircleEntityAction.class, request).addImages(fieldId, weixinId, images_ctx);
            commitTx(request, tx);
        } catch (Exception e) {
            rollbackTx(request, tx);
            throw e;
        } finally {
            LoginContextHolder.clear();
        }
        return JsonMessageBuilder.OK().toMessage();
    }


    /**
     * @param requestBody 请求时提
     * @param request     请求neir
     * @return 你的样子
     */
    @PostMapping(value = "/sync/unread/comments.json")
    public JsonMessage syncUnreadComments(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("syncUnreadComments(requestBody=%s)", requestBody));
        String weixinId = MapUtils.getString(requestBody, "weixinId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), "入参 weixidId 不可以为空值...");
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Preconditions.checkNotNull(companyId, "入参 companyId 不可以为空值...");
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        Preconditions.checkNotNull(companyId, "入参 storeId 不可以为空值...");
        DataSourcesFrom sourcesFrom = new DataSourcesFrom(weixinId, companyId, storeId);
        String comments_json = MapUtils.getString(requestBody, "comments", null);
        Preconditions.checkNotNull(comments_json, "非法的朋友圈评论数据....");
        TransactionStatus tx = startTx(request, null);
        try {
            getBean(WechatCircleCommonsService.class, request).addUnReadComments(comments_json, sourcesFrom);
            commitTx(request, tx);
        } catch (Exception e) {
            rollbackTx(request, tx);
            throw e;
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    @Override
    protected PlatformTransactionManager getTransactionManager(HttpServletRequest request) {
        return getBean("transactionManager", PlatformTransactionManager.class, request);
    }
}

