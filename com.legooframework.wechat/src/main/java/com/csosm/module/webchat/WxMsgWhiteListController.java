package com.csosm.module.webchat;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.EmployeeEntityAction;
import com.csosm.module.webchat.entity.WxMsgWhiteListAction;
import com.csosm.module.webchat.entity.WxMsgWhiteListEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller(value = "wxMsgWhiteListController")
@RequestMapping("/whitelist")
public class WxMsgWhiteListController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

    @RequestMapping(value = "/include/list.json")
    @ResponseBody
    public Map<String, Object> loadWhiteList(HttpServletRequest request) {
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkState(userContext.getStore().isPresent(), "当前账户未指定门店...");
        Preconditions.checkState(userContext.getMaxPowerRole().isPresent() &&
                userContext.getMaxPowerRole().get().isStoreManager(), "只有店长可以使用该功能...");
        Optional<WxMsgWhiteListEntity> opt = getBean(WxMsgWhiteListAction.class, request).findByStore(userContext.getStore().get());
        Optional<List<Map<String, Object>>> list =
                getBean(WxMsgWhiteListAction.class, request).findWhitListByStore(userContext.getStore().get());
        Map<String, Object> model = Maps.newHashMap();
        model.put("prohibit", opt.isPresent() && !opt.get().isProhibit());
        model.put("list", list.isPresent() ? list.get() : new String[0]);
        return wrapperResponse(model);
    }

    @RequestMapping(value = "/include/add.json")
    @ResponseBody
    public Map<String, Object> addWhitList(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addWhitList(params=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkState(userContext.getStore().isPresent());
        Preconditions.checkState(userContext.getMaxPowerRole().isPresent() &&
                userContext.getMaxPowerRole().get().isStoreManager(), "只有店长可以使用该功能...");
        Preconditions.checkState(userContext.getCompany().isPresent(),"当前登录用户无公司信息");
        String accountNo = MapUtils.getString(params, "accountNo");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountNo), "入参 accountNo 不可以为空...");
//        Optional<EmployeeEntity> employee = getBean(EmployeeEntityAction.class, request).findByAccount(accountNo, userContext);
        Optional<EmployeeEntity> employee = getBean(EmployeeEntityAction.class, request).findByLoginName(accountNo, userContext.getCompany().get());
        Preconditions.checkState(employee.isPresent(), "%s 对用的账户信息不存在...", accountNo);
        getBean(WxMsgWhiteListAction.class, request).addWhiteList(userContext.getStore().get(), employee.get(), userContext);
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/include/delete.json")
    @ResponseBody
    public Map<String, Object> removeWhitList(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("removeWhitList(params=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkState(userContext.getStore().isPresent());
        Preconditions.checkState(userContext.getCompany().isPresent(),"当前登录用户无公司信息");
        Preconditions.checkState(userContext.getMaxPowerRole().isPresent() &&
                userContext.getMaxPowerRole().get().isStoreManager(), "只有店长可以使用该功能...");
        String accountNo = MapUtils.getString(params, "accountNo");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountNo), "入参 accountNo 不可以为空...");
//        Optional<EmployeeEntity> employee = getBean(EmployeeEntityAction.class, request).findByAccount(accountNo, userContext);
        Optional<EmployeeEntity> employee = getBean(EmployeeEntityAction.class, request).findByLoginName(accountNo, userContext.getCompany().get());
        Preconditions.checkState(employee.isPresent(), "%s 对用的账户信息不存在...", accountNo);
        getBean(WxMsgWhiteListAction.class, request).removeWhiteList(userContext.getStore().get(), employee.get(), userContext);
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/prohibit/switch.json")
    @ResponseBody
    public Map<String, Object> switchProhibit(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("switchProhibit(params=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkState(userContext.getStore().isPresent());
        Preconditions.checkState(userContext.getMaxPowerRole().isPresent() &&
                userContext.getMaxPowerRole().get().isStoreManager(), "只有店长可以使用该功能...");
        boolean prohibit = MapUtils.getBoolean(params, "prohibit");
        getBean(WxMsgWhiteListAction.class, request).switchProhibit(userContext.getStore().get(), userContext, !prohibit);
        return wrapperResponse(null);
    }

}
