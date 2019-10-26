package com.legooframework.model.templatemgs.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.crmadapter.entity.Authenticationor;
import com.legooframework.model.crmadapter.service.CrmPermissionHelper;
import com.legooframework.model.templatemgs.entity.MsgTemplateEntityAction;
import com.legooframework.model.templatemgs.service.TemplateMgnService;
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

@RestController
@RequestMapping(value = "/template")
public class TemplateManageController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateManageController.class);

    /**
     * 面向管理使用 List
     *
     * @param requestBody 请求实体
     * @param request     请求句柄
     * @return JsonMessage
     */
    @PostMapping(value = "/manage/list.json")
    public JsonMessage loadTemplateList(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContext user = LoginContextHolder.get();
        int pageNum = MapUtils.getInteger(requestBody, "pageNum", 0);
        int pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        Authenticationor authentication = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        PagingResult pages = null;
        if (user.isManager()) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("loadTemplateList(%s,user:%s)", requestBody, user));
            pages = getJdbcQuerySupport(request).queryForPage("MsgTemplateEntity", "general_list", pageNum, pageSize,
                    requestBody);
        } else if (authentication.hasStore()) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("loadTemplateList(%s,user:%s)", requestBody, user));
            Map<String, Object> params = Maps.newHashMap(requestBody);
            params.put("companyId", authentication.getCompany().getId());
            params.put("storeId", authentication.getStore().getId());
            pages = getJdbcQuerySupport(request).queryForPage("MsgTemplateEntity", "store_list", pageNum, pageSize, params);
        } else {
            if (logger.isDebugEnabled())
                logger.debug(String.format("loadTemplateList(%s,user:%s)", requestBody, user));
            Map<String, Object> params = Maps.newHashMap(requestBody);
            params.put("companyId", authentication.getCompany().getId());
            pages = getJdbcQuerySupport(request).queryForPage("MsgTemplateEntity", "company_list", pageNum, pageSize, params);
        }

        // 加工处理结果
        if (!pages.isEmpty()) {
            Map<String, Object> data = pages.toData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("data");
            list.forEach(x -> {
                x.put("blackedTag", MapUtils.getIntValue(x, "blacked", 0) == 0);
                x.put("isDefault", MapUtils.getIntValue(x, "isDefault", 0) == 1);
            });
        }
        return JsonMessageBuilder.OK().withPayload(pages.toData()).toMessage();
    }

    @PostMapping(value = "/crud/insert.json")
    public JsonMessage addTemplateInstance(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addTemplateInstance(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        String template = MapUtils.getString(requestBody, "template");
        String classify = MapUtils.getString(requestBody, "classify");
        String useScopes = MapUtils.getString(requestBody, "useScopes");
        String title = MapUtils.getString(requestBody, "title");
        boolean defaulted = MapUtils.getBoolean(requestBody, "defaulted", false);
        getBean(TemplateMgnService.class, request).insertTemplate(title, template, classify, useScopes, defaulted, user);
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * 修改模板内容  恶狼传输
     *
     * @param requestBody fast
     * @param request     chanel
     * @return 死
     */
    @PostMapping(value = "/crud/setdefault.json")
    public JsonMessage setTempDefault(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("setTempDefault(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        String templateId = MapUtils.getString(requestBody, "templateId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(templateId), "参数 templateId 不允许为空值.");
        boolean defaulted = MapUtils.getBoolean(requestBody, "defaulted", false);
        if (defaulted) {
            getBean(MsgTemplateEntityAction.class, request).setDefaulte(templateId, user);
        } else {
            getBean(MsgTemplateEntityAction.class, request).setUnDefaulte(templateId, user);
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * 修改模板内容  恶狼传输
     *
     * @param requestBody fast
     * @param request     chanel
     * @return 死
     */
    @PostMapping(value = "/crud/update.json")
    public JsonMessage updateTemplateInstance(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addTemplateInstance(%s)", requestBody));
        String templateId = MapUtils.getString(requestBody, "templateId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(templateId), "参数 templateId 不允许为空值.");
        String template = MapUtils.getString(requestBody, "template");
        String title = MapUtils.getString(requestBody, "title");
        String classify_Id = MapUtils.getString(requestBody, "classify", null);
        String _useScopes = MapUtils.getString(requestBody, "useScopes");
        getBean(TemplateMgnService.class, request).updateTemplate(templateId, title, template, classify_Id, _useScopes,
                LoginContextHolder.get());
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * 有效状态修改
     *
     * @param requestBody 请求实体
     * @param request     请求实体  铁臂阿童木
     * @return
     */
    @PostMapping(value = "/change/status.json")
    public JsonMessage changeTmplateStatus(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("changeTmplateStatus(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        String templateId = MapUtils.getString(requestBody, "templateId");
        boolean blacked = MapUtils.getBoolean(requestBody, "blacked");
        if (!blacked) {
            getBean(MsgTemplateEntityAction.class, request).blackTemplate(templateId);
        } else {
            getBean(MsgTemplateEntityAction.class, request).unBlackTemplate(templateId);
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    JdbcQuerySupport getJdbcQuerySupport(HttpServletRequest request) {
        return getBean("templateJdbcQuerySupport", JdbcQuerySupport.class, request);
    }
}
