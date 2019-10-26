package com.legooframework.model.regiscenter.mvc;

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
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.regiscenter.entity.*;
import com.legooframework.model.regiscenter.service.RegisCenterService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/regcntmgr")
public class RegisCenterMgrController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RegisCenterMgrController.class);

    @PostMapping(value = "/pincode/create.json")
    public JsonMessage createByCompanyId(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContext user = LoginContextHolder.get();
        Preconditions.checkState(user.isManager(), "鉴权失败，当前账号不允许操作此功能");
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Integer size = MapUtils.getInteger(requestBody, "size");
        Preconditions.checkState(size > 0, "非法的数量....");
        CrmOrganizationEntity company = getBean(RegisCenterService.class, request)
                .loadCompanyById(companyId);
        Collection<Integer> pinCodes = getBean(DevicePinCodeEntityAction.class, request)
                .batchCreatePinCodes(company, size);
        return JsonMessageBuilder.OK().withPayload(pinCodes).toMessage();
    }

    /**
     * 获取指定BatchNO的代码
     *
     * @param requestBody
     * @param request
     * @return
     */
    @PostMapping(value = "/pincode/load/bybatchno.json")
    public JsonMessage loadByBatchNo(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContext user = LoginContextHolder.get();
        Preconditions.checkState(user.isManager(), "鉴权失败，当前账号不允许操作此功能");
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        String batchNo = MapUtils.getString(requestBody, "batchNo");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(batchNo), "batchNo 不可以为空值...");
        CrmOrganizationEntity company = getBean(RegisCenterService.class, request)
                .loadCompanyById(companyId);
        Optional<List<DevicePinCodeEntity>> list = getBean(DevicePinCodeEntityAction.class, request)
                .loadByBatchNo(batchNo, company);
        if (!list.isPresent()) return JsonMessageBuilder.OK().toMessage();
        List<String> res = list.get().stream().map(DevicePinCodeEntity::getPinCode).collect(Collectors.toList());
        return JsonMessageBuilder.OK().withPayload(res).toMessage();
    }

    /**
     * 门店查询
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/pincode/manage.json")
    public JsonMessage loadPinCodeList(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContext user = LoginContextHolder.get();
        Preconditions.checkState(user.isManager(), "鉴权失败，当前账号不允许操作此功能");
        int pageNum = MapUtils.getInteger(requestBody, "pageNum", 1);
        int pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
        int companyId = MapUtils.getInteger(requestBody, "companyId", -1);
        String status = MapUtils.getString(requestBody, "status");
        Map<String, Object> params = Maps.newHashMap();
        if (companyId != -1) params.put("companyId", companyId);
        if (!Strings.isNullOrEmpty(status)) params.put("status", status);
        PagingResult pagingResult = getBean(JdbcQuerySupport.class, request).queryForPage("DevicePinCodeEntity", "loadPincode", pageNum, pageSize,
                params);
        return JsonMessageBuilder.OK().withPayload(pagingResult.toData()).toMessage();
    }

    @Override
    protected PlatformTransactionManager getTransactionManager(HttpServletRequest request) {
        return getBean("transactionManager", DataSourceTransactionManager.class, request);
    }

}
