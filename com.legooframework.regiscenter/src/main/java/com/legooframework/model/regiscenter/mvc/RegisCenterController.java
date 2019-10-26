package com.legooframework.model.regiscenter.mvc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/regcnt")
public class RegisCenterController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RegisCenterController.class);

    /**
     * 激活设备
     *
     * @param datas   有效
     * @param request 我请求
     * @return 你姐
     */
    @PostMapping(value = "/active.json")
    public JsonMessage activedWithDeviced(@RequestBody Map<String, Object> datas, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        final TransactionStatus transactionStatus = startTx(request, null);
        String deviceId = MapUtils.getString(datas, "deviceId");
        String pincode = MapUtils.getString(datas, "pincode");
        Integer storeId = MapUtils.getInteger(datas, "storeId");
        try {
            if (logger.isDebugEnabled())
                logger.debug(String.format("activedWithDeviced(deviceId:%s,pincode:%s,storeId:%s)", deviceId, pincode, storeId));
            getBean(RegisCenterService.class, request).activedDevice(deviceId, pincode, storeId);
            commitTx(request, transactionStatus);
        } catch (Exception ex) {
            logger.error(String.format("(%s,%s,%s) 激活设备失败....", deviceId, pincode, storeId), ex);
            rollbackTx(request, transactionStatus);
            throw ex;
        } finally {
            LoginContextHolder.clear();
        }
        return JsonMessageBuilder.OK().withPayload(true).toMessage();
    }

    /**
     * 判断该 pinCode 是否被使用
     *
     * @param pinCode   PP
     * @param companyId CC
     * @param request   RR
     * @return 你好
     */
    @PostMapping(value = "/{pinCode}/{companyId}/check4use.json")
    public JsonMessage check4use(@PathVariable String pinCode, @PathVariable Long companyId, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        try {
            Optional<DevicePinCodeEntity> pinCodeEntity = getBean(DevicePinCodeEntityAction.class, request)
                    .findByCode(pinCode);
            Preconditions.checkState(pinCodeEntity.isPresent(), "pinCode=%s 非法，该pinCode不存在...", pinCode);
            Preconditions.checkState(pinCodeEntity.get().getCompanyId().equals(companyId),
                    "pinCode = %s 所属公司与传入公司ID不一致...", pinCode);
            Preconditions.checkState(!pinCodeEntity.get().isBinding(), "pinCode = %s 已经被激活使用...", pinCode);
            Preconditions.checkState(pinCodeEntity.get().isEnabled(), "pinCode = %s 已经被停用...", pinCode);
            return JsonMessageBuilder.OK().withPayload(true).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @PostMapping(value = "/{deviceId}/touched.json")
    public JsonMessage touchDeviceId(@PathVariable String deviceId, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        try {
            Optional<DevicePinCodeEntity> _exits = getBean(DevicePinCodeEntityAction.class, request)
                    .findByDeviceId(deviceId);
            Map<String, Object> res_map = Maps.newHashMap();
            res_map.put("actived", _exits.isPresent());
            _exits.ifPresent(x -> res_map.putAll(getNetConfig(x.getCompanyId(), request)));
            return JsonMessageBuilder.OK().withPayload(res_map).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @PostMapping(value = "/load/{deviceId}/detail.json")
    public JsonMessage loadDetailByDeviceId(@PathVariable String deviceId, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        try {
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
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 门店替换
     *
     * @param oldDeviceId XXXX
     * @param newDeviceId OOOO
     * @param request     XOXOXO
     * @return 爽
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
        } finally {
            LoginContextHolder.clear();
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * @param pinCode  凭证
     * @param deviceId 标签
     * @param request  亲求
     * @return 结果
     */
    @PostMapping(value = "/{pinCode}/{deviceId}/touched.json")
    public JsonMessage touchPinCodeAndDeviceId(@PathVariable(name = "pinCode") String pinCode,
                                               @PathVariable String deviceId, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        try {
            if (logger.isDebugEnabled())
                logger.debug(String.format("touchPinCodeAndDeviceId(%s,%s)", pinCode, deviceId));
            Optional<List<DevicePinCodeEntity>> _exits = getBean(DevicePinCodeEntityAction.class, request)
                    .findByCodeOrDeviceId(pinCode, deviceId);
            Preconditions.checkState(_exits.isPresent(), "非法的pincode =%s 或者设备ID=%s", pinCode, deviceId);

            Optional<DevicePinCodeEntity> device_opt = _exits.get().stream().filter(x -> x.hasDeviceId(deviceId))
                    .findFirst();
            Map<String, Object> res_map = Maps.newHashMap();
            if (device_opt.isPresent()) {
                res_map.put("actived", true);
                res_map.put("deviceId", device_opt.get().getDeviceId().orElse(null));
                res_map.putAll(getNetConfig(device_opt.get().getCompanyId(), request));
                return JsonMessageBuilder.OK().withPayload(res_map).toMessage();
            }

            Optional<DevicePinCodeEntity> pin_code_opt = _exits.get().stream().filter(x -> StringUtils.equals(x.getPinCode(), pinCode))
                    .findFirst();
            Preconditions.checkState(pin_code_opt.isPresent(), "非法的pincode =%s ", pinCode);
            res_map.put("actived", pin_code_opt.get().getDeviceId().isPresent());
            res_map.put("deviceId", pin_code_opt.get().getDeviceId().orElse(null));
            res_map.putAll(getNetConfig(pin_code_opt.get().getCompanyId(), request));
            return JsonMessageBuilder.OK().withPayload(res_map).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    private Map<String, Object> getNetConfig(Long companyId, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("getNetConfig(companyId:%s)", companyId));
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class, request)
                .findCompanyByIdWithRest(companyId.intValue());
        Preconditions.checkState(company.isPresent(), "id=%s 对应的公司不存在...", companyId);
        Optional<TenantNetConfigEntity> config = getBean(TenantNetConfigEntityAction.class, request)
                .findByCompany(company.get());
        Preconditions.checkState(config.isPresent(), "租户%s对应的网络配置参数未初始化...", company.get().getName());
        return config.get().toMap();
    }

    @Override
    protected PlatformTransactionManager getTransactionManager(HttpServletRequest request) {
        return getBean("transactionManager", DataSourceTransactionManager.class, request);
    }

}
