package com.legooframework.model.templatemgs.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.web.*;
import com.legooframework.model.crmadapter.entity.Authenticationor;
import com.legooframework.model.crmadapter.service.CrmPermissionHelper;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.templatemgs.entity.*;
import com.legooframework.model.templatemgs.service.HolidayService;
import com.legooframework.model.templatemgs.service.TemplateMgnService;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/template")
public class TemplateReaderController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateReaderController.class);

    @PostMapping(value = "/load/classify-tree.json")
    public JsonMessage loadClassifyTree(@RequestBody(required = false) Map<String, Object> requestBody,
                                        HttpServletRequest request) {
        LoginContext user = LoginContextHolder.get();
        Authenticationor authentication = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        List<TreeNode> treeNodes = Lists.newArrayList();
        Optional<List<TreeNode>> treeNodesOpt = getBean(TemplateClassifyEntityAction.class, request)
                .loadTreeNodeByCompany(authentication.getCompany());
        List<TreeNode> hilodayTrees = getBean(HolidayService.class, request).loadTreeNode(user);
        treeNodesOpt.ifPresent(treeNodes::addAll);

        if (!CollectionUtils.isEmpty(hilodayTrees)) {
            hilodayTrees.forEach(x -> {
                Optional<TreeNode> treeOpt = treeNodes.stream().filter(n -> String.valueOf(n.getId())
                        .equals(String.valueOf(x.getId()))).findFirst();
                if (!treeOpt.isPresent()) treeNodes.add(x);
            });
        }
        final TreeNode root = new TreeNode("0000", "0000", "模板类型", null);
        TreeUtil.buildTree(root, treeNodes);
        return JsonMessageBuilder.OK().withPayload(new Map[]{root.toViewMap()}).toMessage();
    }

    /**
     * 应从当前帐号 获取 可激活使用的分类的模板 需指定类别标识
     *
     * @param requestBody 你的请求
     * @param request     你的答复
     * @return 我是税
     */
    @PostMapping(value = "/read/enable/list.json")
    public JsonMessage readEnabledList(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("readEnabledList(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        String classifyId = MapUtils.getString(requestBody, "classifies");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(classifyId), "分类标识不可以未空值 classifies=?");

        String _useScopes = MapUtils.getString(requestBody, "useScopes");
        List<UseScope> useScopes = Lists.newArrayListWithCapacity(2);
        if (!Strings.isNullOrEmpty(_useScopes)) {
            Stream.of(StringUtils.split(_useScopes, ',')).forEach(x -> useScopes.add(UseScope.paras(Integer.valueOf(x))));
        }

        if (CollectionUtils.isNotEmpty(useScopes)) {
            Optional<List<MsgTemplateEntity>> enbaled_list = getBean(MsgTemplateEntityAction.class, request)
                    .loadEnabledListByUser(user, null, classifyId);
            if (!enbaled_list.isPresent()) return JsonMessageBuilder.OK().withPayload(new String[0]).toMessage();
            List<MsgTemplateEntity> my_list = enbaled_list.get().stream().filter(x -> x.contains(useScopes))
                    .collect(Collectors.toList());
            return JsonMessageBuilder.OK().withPayload(CollectionUtils.isEmpty(my_list) ? new String[0] :
                    my_list.stream().map(MsgTemplateEntity::toSimpleMap).collect(Collectors.toList())).toMessage();
        } else {
            Optional<List<MsgTemplateEntity>> enbaled_list = getBean(MsgTemplateEntityAction.class, request)
                    .loadEnabledListByUser(user, null, classifyId);
            if (!enbaled_list.isPresent()) return JsonMessageBuilder.OK().withPayload(new String[0]).toMessage();
            return JsonMessageBuilder.OK().withPayload(enbaled_list.get().stream()
                    .map(MsgTemplateEntity::toSimpleMap).collect(Collectors.toList())).toMessage();
        }
    }

    /**
     * @param request 历史的悲剧
     * @return 我的悲剧
     */
    @PostMapping(value = "/read/{businessType}/defaults.json")
    public JsonMessage readDefaultTemplate(@PathVariable(value = "businessType") String businessType,
                                           @RequestBody(required = false) Map<String, Object> requestBody,
                                           HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("read%sTemplate(%s)", businessType, businessType));
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        if (StringUtils.equalsIgnoreCase("touched90", businessType)) {
            String categories = MapUtils.getString(requestBody, "categories", null);
            Integer storeId = MapUtils.getInteger(requestBody, "storeId", -1);
            Touch90DefauteTemplate defauteTemplate = null;
            if (storeId == -1) {
                defauteTemplate = getBean(TemplateMgnService.class, request)
                        .loadDefaultTouch90Template(authenticationor.getCompany(), null);
            } else {
                defauteTemplate = getBean(TemplateMgnService.class, request)
                        .loadDefaultTouch90Template(authenticationor.getCompany(), authenticationor.getStore());
            }
            return JsonMessageBuilder.OK().withPayload(defauteTemplate.getTemplatesByCategories(categories)).toMessage();
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * @param request 历史的悲剧
     * @return 我的悲剧
     */
    @PostMapping(value = "/read/defaults/classifies.json")
    public JsonMessage readDefaultTemplateByClassfies(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("readTemplate(%s)", requestBody));
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            String classifies_str = MapUtils.getString(requestBody, "classifies");
            List<String> classifies = Splitter.on(',').splitToList(classifies_str);
            Optional<List<MsgTemplateEntity>> msgTemplates = getBean(MsgTemplateEntityAction.class, request)
                    .loadDefaultByClassifies(classifies);
            List<String> payload = Lists.newArrayList();
            msgTemplates.ifPresent(temps -> temps.forEach(x -> payload.add(x.toSimpleValue(true))));
            return JsonMessageBuilder.OK().withPayload(StringUtils.join(payload, "||")).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * @param request 历史的悲剧 大家的悲剧
     * @return 我的悲剧
     */
    @PostMapping(value = "/read/{businessType}/details.json")
    public JsonMessage readEnabledTemplates(@PathVariable(value = "businessType") String businessType,
                                            @RequestBody Map<String, Object> requestBody,
                                            HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("readEnabledTemplates(%s,%s)", businessType, requestBody));
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        String classifyId = null;
        if (StringUtils.equalsIgnoreCase("touched90", businessType)) {
            String categories = MapUtils.getString(requestBody, "categories");
            String subRuleId = MapUtils.getString(requestBody, "subRuleId");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(categories), "模板分类不可以为空...");
            classifyId = String.format("%s_%s_%s_%s", BusinessType.TOUCHED90.toString(),
                    authenticationor.getCompany().getId(), categories, subRuleId);
        }
        Preconditions.checkArgument(!Strings.isNullOrEmpty(classifyId), "模板分类ID不可以为空值....");
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadEnabledListByUser(%s,%s)", authenticationor.getCompany().getId(), classifyId));
        Optional<List<MsgTemplateEntity>> templates = getBean(MsgTemplateEntityAction.class, request).
                loadEnabledListByUser(authenticationor.getUser(), authenticationor.getStore(), classifyId);
        List<Map<String, Object>> mapList = Lists.newArrayList();
        templates.ifPresent(list -> list.stream().map(MsgTemplateEntity::toSimpleMap).forEach(mapList::add));
        return JsonMessageBuilder.OK().withPayload(mapList).toMessage();
    }

    JdbcQuerySupport getJdbcQuerySupport(HttpServletRequest request) {
        return getBean("templateJdbcQuerySupport", JdbcQuerySupport.class, request);
    }
}
