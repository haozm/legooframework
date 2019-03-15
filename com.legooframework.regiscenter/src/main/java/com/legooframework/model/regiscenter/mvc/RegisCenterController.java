package com.legooframework.model.regiscenter.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@RequestMapping(value = "/regcnt")
public class RegisCenterController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RegisCenterController.class);

    @PostMapping(value = "/active.json")
    public JsonMessage activedWithDeviced(@RequestBody Map<String, Object> datas, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        String deviceId = MapUtils.getString(datas, "deviceId");
        String pincode = MapUtils.getString(datas, "pincode");
        Integer storeId = MapUtils.getInteger(datas, "storeId");
        final TransactionStatus transactionStatus = startTx(request, null);
        try {
            getBean(RegisCenterService.class, request).activedDevice(deviceId, pincode, storeId);
            commitTx(request, transactionStatus);
        } catch (Exception ex) {
            logger.error(String.format("(%s,%s,%s) 激活设备失败....", deviceId, pincode, storeId), ex);
            rollbackTx(request, transactionStatus);
            throw ex;
        }
        return JsonMessageBuilder.OK().withPayload(true).toMessage();
    }

    @PostMapping(value = "/pincode/create.json")
    public JsonMessage createByCompanyId(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Integer size = MapUtils.getInteger(requestBody, "size");
        Preconditions.checkState(size > 0, "非法的数量....");
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class, request)
                .findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "非法的公司ID=%s", companyId);
        Collection<Integer> pinCodes = getBean(DevicePinCodeEntityAction.class, request)
                .batchCreatePinCodes(company.get(), size);
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
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        String batchNo = MapUtils.getString(requestBody, "batchNo");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(batchNo), "batchNo 不可以为空值...");
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class, request)
                .findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "非法的公司ID=%s", companyId);
        Optional<List<DevicePinCodeEntity>> list = getBean(DevicePinCodeEntityAction.class, request)
                .loadByBatchNo(batchNo, company.get());
        if (!list.isPresent()) return JsonMessageBuilder.OK().toMessage();
        List<String> res = list.get().stream().map(DevicePinCodeEntity::getPinCode).collect(Collectors.toList());
        return JsonMessageBuilder.OK().withPayload(res).toMessage();
    }

    /**
     * 判断该 pinCode 是否被使用
     *
     * @param pinCode
     * @param companyId
     * @param request
     * @return
     */
    @PostMapping(value = "/{pinCode}/{companyId}/check4use.json")
    public JsonMessage check4use(@PathVariable String pinCode, @PathVariable Long companyId, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        Optional<DevicePinCodeEntity> pinCodeEntity = getBean(DevicePinCodeEntityAction.class, request)
                .findByCode(pinCode);
        Preconditions.checkState(pinCodeEntity.isPresent(), "pinCode=%s 非法，该pinCode不存在...", pinCode);
        Preconditions.checkState(pinCodeEntity.get().getCompanyId().equals(companyId),
                "pinCode = %s 所属公司与传入公司ID不一致...", pinCode);
        Preconditions.checkState(!pinCodeEntity.get().isBinding(), "pinCode = %s 已经被激活使用...", pinCode);
        Preconditions.checkState(pinCodeEntity.get().isEnabled(), "pinCode = %s 已经被停用...", pinCode);
        return JsonMessageBuilder.OK().withPayload(true).toMessage();
    }

    @PostMapping(value = "/{deviceId}/touched.json")
    public JsonMessage touchDeviceId(@PathVariable String deviceId, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        Optional<DevicePinCodeEntity> _exits = getBean(DevicePinCodeEntityAction.class, request)
                .findByDeviceId(deviceId);
        Map<String, Object> res_map = Maps.newHashMap();
        res_map.put("actived", _exits.isPresent());
        _exits.ifPresent(x -> res_map.putAll(getNetConfig(x.getCompanyId(), request)));
        return JsonMessageBuilder.OK().withPayload(res_map).toMessage();
    }

    @PostMapping(value = "/load/{deviceId}/detail.json")
    public JsonMessage loadDetailByDeviceId(@PathVariable String deviceId, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        Optional<StoreActiveInfoEntity> _exits = getBean(StoreActiveInfoEntityAction.class, request)
                .findByDeviceId(deviceId);
        Map<String, Object> res_map = Maps.newHashMap();
        if (_exits.isPresent()) {
            res_map.put("deviceId", _exits.get().getDeviceId());
            res_map.put("storeId", _exits.get().getStoreId());
            res_map.put("companyId", _exits.get().getCompanyId());
            res_map.put("activeDate", _exits.get().getActiveDate().toString("yyyy-MM-dd"));
            res_map.put("deadline", _exits.get().getDeadline().toString("yyyy-MM-dd"));
            res_map.put("isExpired", _exits.get().isExpired());
            return JsonMessageBuilder.OK().withPayload(res_map).toMessage();
        }
        Optional<DevicePinCodeEntity> _pincode = getBean(DevicePinCodeEntityAction.class, request)
                .findByDeviceId(deviceId);
        if (_pincode.isPresent()) {
            res_map.put("deviceId", _pincode.get().getDeviceId().orElse(null));
            res_map.put("storeId", _pincode.get().getStoreId());
            res_map.put("companyId", _pincode.get().getCompanyId());
            res_map.put("active", _pincode.get().getBindingDate().isPresent());
            if (_pincode.get().getBindingDate().isPresent()) {
                res_map.put("activeDate", _pincode.get().getBindingDate().get().toString("yyyy-MM-dd"));
                res_map.put("deadline", _pincode.get().getBindingDate().get().plusYears(1).toString("yyyy-MM-dd"));
            }
            res_map.put("isExpired", false);
            return JsonMessageBuilder.OK().withPayload(res_map).toMessage();
        }
        return JsonMessageBuilder.ERROR("9998").toMessage();
    }

    /**
     * 门店替换
     *
     * @param oldDeviceId
     * @param newDeviceId
     * @param request
     * @return
     */
    @PostMapping(value = "/{oldDeviceId}/{newDeviceId}/{storeId}/changedevice.json")
    public JsonMessage changeDevice(@PathVariable String oldDeviceId, @PathVariable String newDeviceId,
                                    @PathVariable Integer storeId, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        final TransactionStatus transactionStatus = startTx(request, null);
        try {
            getBean(RegisCenterService.class, request).changeDevice(oldDeviceId, newDeviceId, storeId);
            commitTx(request, transactionStatus);
        } catch (Exception ex) {
            rollbackTx(request, transactionStatus);
            throw ex;
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    @PostMapping(value = "/{pinCode}/{deviceId}/touched.json")
    public JsonMessage touchPinCodeAndDeviceId(@PathVariable(name = "pinCode") String pinCode,
                                               @PathVariable String deviceId, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        Optional<List<DevicePinCodeEntity>> _exits = getBean(DevicePinCodeEntityAction.class, request)
                .findByCodeOrDeviceId(pinCode, deviceId);
        Preconditions.checkState(_exits.isPresent(), "非法的pincode =%s", pinCode);

        Optional<DevicePinCodeEntity> device_opt = _exits.get().stream()
                .filter(x -> x.getDeviceId().isPresent() && StringUtils.equals(x.getDeviceId().get(), deviceId))
                .findFirst();
        Map<String, Object> res_map = Maps.newHashMap();
        if (device_opt.isPresent()) {
            res_map.put("actived", true);
            res_map.put("deviceId", device_opt.get().getDeviceId().orElse(null));
            res_map.putAll(getNetConfig(device_opt.get().getCompanyId(), request));
            return JsonMessageBuilder.OK().withPayload(res_map).toMessage();
        }

        Optional<DevicePinCodeEntity> pin_code_opt = _exits.get().stream()
                .filter(x -> StringUtils.equals(x.getPinCode(), pinCode)).findFirst();
        Preconditions.checkState(pin_code_opt.isPresent(), "非法的pincode =%s", pinCode);
        if (pin_code_opt.get().getDeviceId().isPresent()) {
            res_map.put("actived", true);
            res_map.put("deviceId", pin_code_opt.get().getDeviceId().get());
            res_map.putAll(getNetConfig(pin_code_opt.get().getCompanyId(), request));
            return JsonMessageBuilder.OK().withPayload(res_map).toMessage();
        }
        // 合法的pincode 码
        res_map.put("actived", false);
        res_map.putAll(getNetConfig(pin_code_opt.get().getCompanyId(), request));
        return JsonMessageBuilder.OK().withPayload(res_map).toMessage();
    }

    /**
     * 门店查询
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/pincode/manage.json")
    public JsonMessage loadPinCodeList(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        int pageNum = MapUtils.getInteger(requestBody, "pageNum", 1);
        int pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
        int companyId = MapUtils.getInteger(requestBody, "companyId", -1);
        String status = MapUtils.getString(requestBody, "status");
        Map<String, Object> params = Maps.newHashMap();
        if (companyId != -1) params.put("companyId", companyId);
        if (!Strings.isNullOrEmpty(status)) params.put("status", status);
        PagingResult pagingResult = jdbcQuerySupport.queryForPage("DevicePinCodeEntity", "loadPincode", pageNum, pageSize,
                params);
        return JsonMessageBuilder.OK().withPayload(pagingResult.toData()).toMessage();
    }

    private Map<String, Object> getNetConfig(Long companyId, HttpServletRequest request) {
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class, request)
                .findCompanyById(companyId.intValue());
        Preconditions.checkState(company.isPresent(), "id=%s 对应的公司不存在...", companyId);
        Optional<TenantNetConfigEntity> config = getBean(TenantNetConfigEntityAction.class, request)
                .findByCompany(company.get());
        Preconditions.checkState(config.isPresent(), "租户%s对应的网络配置参数未初始化...", company.get().getName());
        return config.get().toMap();
    }


    @Autowired
    @Qualifier(value = "regiscenterJdbcQuery")
    private JdbcQuerySupport jdbcQuerySupport;

    @Override
    protected PlatformTransactionManager getTransactionManager(HttpServletRequest request) {
        return getBean("transactionManager", DataSourceTransactionManager.class, request);
    }

}
