package com.legooframework.model.membercare.mvc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.crmadapter.entity.Authenticationor;
import com.legooframework.model.crmadapter.entity.MsgTemplateProxyAction;
import com.legooframework.model.crmadapter.entity.SmsGatewayProxyAction;
import com.legooframework.model.crmadapter.service.CrmPermissionHelper;
import com.legooframework.model.membercare.entity.*;
import com.legooframework.model.membercare.service.MemberCareJobService;
import com.legooframework.model.templatemgs.entity.Touch90DefauteTemplate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/jobs")
public class MemberJobController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MemberJobController.class);

    /**
     * 取消任务
     *
     * @param requestBody 天雷
     * @param request     滚滚
     * @return JsonMessage
     */
    @PostMapping(value = "/batch/canceled/details.json")
    public JsonMessage batchCanceledDetails(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchCanceledDetails(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        String detail_ids = MapUtils.getString(requestBody, "detailIds");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(detail_ids), "待取消的任务明细ID不可以为空...");
        List<Integer> detailIds = Stream.of(StringUtils.split(detail_ids, ',')).map(Integer::valueOf)
                .collect(Collectors.toList());
        String remarkes = MapUtils.getString(requestBody, "remarke");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(remarkes), "备注不可以为空值...");
        getBean(UpcomingTaskDetailEntityAction.class, request).canceledDetailIds(detailIds, remarkes);
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * 取消任务 按照主任务
     *
     * @param requestBody 天雷
     * @param request     滚滚
     * @return JsonMessage
     */
    @PostMapping(value = "/batch/canceled/task.json")
    public JsonMessage batchCanceledTask(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchCanceledTask(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        Integer taskId = MapUtils.getInteger(requestBody, "taskId", null);
        Preconditions.checkArgument(null != taskId, "入参 taskId 不可以为空值...");
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        getBean(MemberCareJobService.class, request).canceledByTask(taskId);
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * 线下完成任务
     *
     * @param requestBody 天雷
     * @param request     滚滚
     * @return JSON
     */
    @PostMapping(value = "/batch/finish/offline/details.json")
    public JsonMessage batchFinshedByOfflineJobs(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchCanceledJobs(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        Integer storeId = MapUtils.getInteger(requestBody, "storeId", null);
        Authenticationor author = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        String detail_ids = MapUtils.getString(requestBody, "detailIds");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(detail_ids), "待取消的任务明细ID不可以为空...");
        List<Integer> detailIds = Stream.of(StringUtils.split(detail_ids, ',')).map(Integer::valueOf)
                .collect(Collectors.toList());
        String remarkes = MapUtils.getString(requestBody, "remarke");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(remarkes), "备注不可以为空值...");
        getBean(UpcomingTaskDetailEntityAction.class, request).finshedDetailsByOfferline(detailIds, remarkes, author);
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * 单一 发送 TOUCHED90
     * jobDetails: taskId,memberId,template
     *
     * @param businessType 请求
     * @param requestBody  业务员
     * @param request      谷子额
     * @return JSON
     */
    @PostMapping(value = "/execute/{businessType}/single.json")
    public JsonMessage executeSingleJobs(@PathVariable(value = "businessType") String businessType,
                                         @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("executeSingleJobs(businessType:%s,%s)", businessType, requestBody));
        LoginContext user = LoginContextHolder.get();
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        Integer taskDetailId = MapUtils.getInteger(requestBody, "taskDetailId");
        boolean encoding = MapUtils.getBoolean(requestBody, "encoding", false);
        AutoRunChannel autoRunChannel = AutoRunChannel.parse(MapUtils.getInteger(requestBody, "autoRunChannel", 1));
        String template = MapUtils.getString(requestBody, "template");
        Optional<List<UpcomingTaskDetailEntity>> detail_jobs = getBean(UpcomingTaskDetailEntityAction.class, request)
                .loadByDetailIds4Exec(Lists.newArrayList(taskDetailId));
        Preconditions.checkState(detail_jobs.isPresent(), "数据异常....不存在可执行的任务明细,或者该任务节点已经执行完成....");

        String payload = String.format("%s,%s,%s||%s", taskDetailId, detail_jobs.get().get(0).getMemberId(),
                autoRunChannel.getChannel(), encoding ? template : WebUtils.encodeUrl(template));
        String batch_no = getBean(SmsGatewayProxyAction.class, request).sendMessageProxy(user.getTenantId().intValue(),
                authenticationor.getStore().getId(), user.getLoginId().intValue(), Lists.newArrayList(payload), null,
                BusinessType.TOUCHED90, true, request);
        if (StringUtils.isNotEmpty(batch_no))
            getBean(UpcomingTaskDetailEntityAction.class, request).finshedDetails(detail_jobs.get(), user);
        return JsonMessageBuilder.OK().withPayload(batch_no).toMessage();
    }

    /**
     * jobDetails 格式如下： detailId:memberId@detailId:memberId
     * 曹操也不过如此 而已
     *
     * @param businessType 业务类型
     * @param requestBody  亲求负载
     * @param request      亲求
     * @return JSON
     */
    @PostMapping(value = "/execute/{businessType}/batch.json")
    public JsonMessage executeBatchJobs(@PathVariable(value = "businessType") String businessType,
                                        @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("executeBatchJobs(businessType:%s,%s)", businessType, requestBody));
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        Preconditions.checkState(authenticationor.hasStore(), "执行该操作需要指定门店...");
        AutoRunChannel autoRunChannel = AutoRunChannel.parse(MapUtils.getInteger(requestBody, "autoRunChannel", 1));
        // 消息类型：detailId:memberId@detailId:memberId
        String jobDetailIds = MapUtils.getString(requestBody, "jobDetailIds");
        // Map<String, String> jobDetails_data = mapSplitter.split(jobDetails);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobDetailIds), "待执行的任务列表不可以为空....");
        Splitter splitter = Splitter.on(',').trimResults();
        List<Integer> task_detail_ids = splitter.splitToList(jobDetailIds).stream().map(Integer::valueOf)
                .collect(Collectors.toList());
        Optional<List<UpcomingTaskDetailEntity>> detail_jobs = getBean(UpcomingTaskDetailEntityAction.class, request)
                .loadByDetailIds4Exec(task_detail_ids);
        Preconditions.checkState(detail_jobs.isPresent() && task_detail_ids.size() == detail_jobs.get().size(),
                "数据异常...不存在可执行的任务明细或者选中的节点已经执行完毕....");
        // categories: UpcomingTaskDetailEntity
        ArrayListMultimap<String, UpcomingTaskDetailEntity> multimap = ArrayListMultimap.create();
        detail_jobs.get().forEach(x -> multimap.put(x.getCategories(), x));
        // 手机不同类型的分类
        Set<String> categories = multimap.keySet();
        List<UpcomingTaskDetailExecDto> group_steps_list = Lists.newArrayList();
        Map<String, Touch90DefauteTemplate> def_templates = Maps.newHashMap();
        categories.forEach(categorie -> {
            Touch90DefauteTemplate temp = getBean(MsgTemplateProxyAction.class, request)
                    .readTouch90Defaults(authenticationor.getStore(), categorie, request);
            Preconditions.checkArgument(temp != null, "%s 类型的90模板尚未设置...", categorie);
            def_templates.put(categorie, temp);
        });


        multimap.keySet().forEach(categorie -> {
            List<UpcomingTaskDetailEntity> _list = multimap.get(categorie);
            _list.forEach(x -> {
                Touch90DefauteTemplate _temp = def_templates.get(categorie);
                Optional<String> temp_opt = _temp.getTemplateById(categorie, x.getSubRuleId());
                Preconditions.checkState(temp_opt.isPresent(), "节点%s缺失对应的模板", x.getStepIndex());
                UpcomingTaskDetailExecDto dto = new UpcomingTaskDetailExecDto(x, temp_opt.orElse(null), autoRunChannel);
                group_steps_list.add(dto);
            });
        });
        if (CollectionUtils.isNotEmpty(group_steps_list)) {
            List<String> payloads = group_steps_list.stream().map(UpcomingTaskDetailExecDto::toStringWithEncoding)
                    .collect(Collectors.toList());
            getBean(SmsGatewayProxyAction.class, request)
                    .sendMessageProxy(authenticationor.getCompany().getId(), authenticationor.getStore().getId(),
                            authenticationor.getUser().getLoginId().intValue(), payloads, null, BusinessType.TOUCHED90, true, request);
            getBean(UpcomingTaskDetailEntityAction.class, request).finshedDetails(detail_jobs.get(), authenticationor.getUser());
        }
        return JsonMessageBuilder.OK().toMessage();
    }

}
