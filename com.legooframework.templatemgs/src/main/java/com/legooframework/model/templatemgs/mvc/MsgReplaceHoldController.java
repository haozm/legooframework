package com.legooframework.model.templatemgs.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.membercare.entity.AutoRunChannel;
import com.legooframework.model.smsgateway.entity.SendMessageTemplate;
import com.legooframework.model.templatemgs.entity.MsgReplaceHoldEntity;
import com.legooframework.model.templatemgs.entity.MsgReplaceHoldEntityAction;
import com.legooframework.model.templatemgs.entity.MsgReplaceHoldList;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/tempreplace")
public class MsgReplaceHoldController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MsgReplaceHoldController.class);

    @PostMapping(value = "/crud/add.json")
    public JsonMessage addReplaceHold(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addReplaceHold(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        String fieldTag = MapUtils.getString(requestBody, "fieldTag");
        String replaceToken = MapUtils.getString(requestBody, "replaceToken");
        String defaultValue = MapUtils.getString(requestBody, "defaultValue", null);
        // getBean(MsgReplaceHoldEntityAction.class, request).addReplaceHold(user, fieldTag, replaceToken, defaultValue);
        return JsonMessageBuilder.OK().toMessage();
    }

    @PostMapping(value = "/manage/list.json")
    public JsonMessage loadManageReplaceHold(HttpServletRequest request) {
        LoginContext user = LoginContextHolder.get();
        MsgReplaceHoldList holdList = getBean(MsgReplaceHoldEntityAction.class, request).loadByUser(user);
        if (holdList.isEmpty()) return JsonMessageBuilder.OK().toMessage();
        List<MsgReplaceHoldEntity> list = holdList.getReplaceHolds();
        List<Map<String, Object>> maps = Lists.newArrayList();
        list.forEach(x -> maps.add(x.toViewMap()));
        return JsonMessageBuilder.OK().withPayload(maps).toMessage();
    }

    /**
     * 加载可使用的 模板
     *
     * @param request 请求
     * @return 请求的孩子
     */
    @PostMapping(value = "/simple/list.json")
    public JsonMessage loadSimpleReplaceHold(HttpServletRequest request) {
        LoginContext user = LoginContextHolder.get();
        MsgReplaceHoldList holdList = getBean(MsgReplaceHoldEntityAction.class, request).loadByUser(user);
        if (holdList.isEmpty()) return JsonMessageBuilder.OK().toMessage();
        List<MsgReplaceHoldEntity> list = holdList.getReplaceHolds();
        List<String> simple_list = Lists.newArrayList();
        list.forEach(x -> simple_list.add(String.format("{%s}", x.getReplaceToken())));
        return JsonMessageBuilder.OK().withPayload(simple_list).toMessage();
    }

    /**
     * 检查模板的合法性
     *
     * @param requestBody 请求
     * @param request     请求的容器
     * @return JsonMessage
     */
    @PostMapping(value = "/check/validity.json")
    public JsonMessage checkTemplate(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("checkTemplate(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        String templates = MapUtils.getString(requestBody, "templates");
        boolean encoding = MapUtils.getBoolean(requestBody, "encoding", false);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(Strings.emptyToNull(templates)), "待验证的模板内容为空...");
        MsgReplaceHoldList replaceHold = getBean(MsgReplaceHoldEntityAction.class, request).loadByUser(user);
        String[] args = StringUtils.split(templates, "@@");
        if (encoding) {
            for (int i = 0; i < args.length; i++) {
                args[i] = WebUtils.decodeUrl(args[i]);
            }
        }
        Stream.of(args).forEach(x -> Preconditions.checkState(replaceHold.checkTemplate(x), "%s 对应的模板占位符异常.", x));
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * 格式化一批模板并返回 一切尽在不言中
     * 道不尽人间苦辣 模板统一替换并返回 个人详细信息
     * //
     *
     * @param requestBody 请求负载
     * @param request     请求容器
     * @return 请求
     */
    @PostMapping(value = "/batch/member/templates.json")
    public JsonMessage batchReplaceMemberTemplateAction(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchReplaceMemberTemplateAction(%s) ", requestBody));
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Integer employeeId = MapUtils.getInteger(requestBody, "employeeId", -1);
        boolean encoding = MapUtils.getBoolean(requestBody, "encoding", false);
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class, request).findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "ID=%s 对应的公司存在...");

        String payload = MapUtils.getString(requestBody, "payload");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(payload), "待格式化的模板信息不可以为空...");
        String[] fragments = StringUtils.split(payload, '@');
        List<JobDetailTemplate4Replace> template4Replaces = Lists.newArrayListWithCapacity(fragments.length);
        Stream.of(fragments).forEach(x -> template4Replaces.add(new JobDetailTemplate4Replace(companyId, employeeId, x, encoding)));

        MsgReplaceHoldList replaceHoldList = getBean(MsgReplaceHoldEntityAction.class, request).loadByCompany(company.get());

        List<Integer> memberIds = Lists.newArrayList();
        template4Replaces.forEach(x -> memberIds.addAll(x.getMemberIds()));
        Map<String, Object> params = Maps.newHashMap();
        params.put("employeeId", employeeId);
        params.put("companyId", companyId);
        params.put("memberIds", memberIds);
        Optional<List<Map<String, Object>>> _member_infos = getJdbcQuerySupport(request)
                .queryForList("MsgReplaceHoldEntity", "loadReplaceSource", params);
        Map<Integer, Map<String, Object>> replaceSources = Maps.newHashMap();
        _member_infos.ifPresent(x -> x.forEach(m -> replaceSources.put(MapUtils.getInteger(m, "id"), m)));
        template4Replaces.forEach(x -> replaceMembers(replaceHoldList, x, replaceSources));
        List<String> res_list = Lists.newArrayList();
        template4Replaces.forEach(x -> res_list.add(x.toString()));
        // detailId|memberId|channel|weixinId@deviceId|mobile|{encoding}memberName|{encoding}context|resulat|||
        // detailId|memberId|channel|weixinId@deviceId|mobile|{encoding}memberName|{encoding}context|resulat
        return JsonMessageBuilder.OK().withHeader("encoding", true).withPayload(StringUtils.join(res_list, "|||")).toMessage();
    }


    /**
     * 替换动作本身  阿弥陀佛
     *
     * @param replaceHoldList  格式化列表
     * @param replaceSourceMap 格式化结果
     */
    private void replaceMembers(MsgReplaceHoldList replaceHoldList, JobDetailTemplate4Replace replaces,
                                Map<Integer, Map<String, Object>> replaceSourceMap) {
        List<SendMessageTemplate> templates = replaces.getJobDetails();
        templates.forEach(template -> {
            Map<String, Object> replace = replaceSourceMap.get(template.getMemberId());
            if (MapUtils.isNotEmpty(replace)) {
                String[] _tems = replaceHoldList.replaceMembers(replace, replaces.getTemplate());
//                final String mobile, final String name, final String context,
//                final String resulat, final String weixinId, final String deviceId, final AutoRunChannel autoRunChannel
                template.setMobile(_tems[0]);
                template.setMemberName(_tems[1]);
                template.setContext(_tems[2]);
                template.setResulat(_tems[3]);
                template.setWeixinInfo(_tems[4], _tems[5]);
            } else {
                template.setMobile("0000");
                template.setMemberName("EMPTY");
                template.setContext("EMPTY");
                template.setResulat("NOTEXITS");
                template.setWeixinInfo(null, null);
            }
        });
    }

    JdbcQuerySupport getJdbcQuerySupport(HttpServletRequest request) {
        return getBean("templateJdbcQuerySupport", JdbcQuerySupport.class, request);
    }
}
