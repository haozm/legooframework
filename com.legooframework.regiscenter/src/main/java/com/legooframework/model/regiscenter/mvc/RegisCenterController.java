package com.legooframework.model.regiscenter.mvc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.organization.entity.CompanyEntity;
import com.legooframework.model.organization.entity.CompanyEntityAction;
import com.legooframework.model.regiscenter.entity.DevicePinCodeEntity;
import com.legooframework.model.regiscenter.entity.DevicePinCodeEntityAction;
import com.legooframework.model.regiscenter.entity.TenantNetConfigEntity;
import com.legooframework.model.regiscenter.entity.TenantNetConfigEntityAction;
import com.legooframework.model.regiscenter.service.RegisCenterService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/regcnt")
public class RegisCenterController extends BaseController {

    @PostMapping(value = "/active.json")
    public JsonMessage activedWithDeviced(@RequestBody Map<String, Object> datas, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        String deviceId = MapUtils.getString(datas, "deviceId");
        String pincode = MapUtils.getString(datas, "pincode");
        String imei1 = null, imei2 = null;
        if (datas.containsKey("imei")) {
            String imei = MapUtils.getString(datas, "imei");
            String[] imeis = imei.split(",");
            if (imeis.length == 1) imei1 = imeis[0];
            if (imeis.length == 2) {
                imei1 = imeis[0];
                imei2 = imeis[1];
            }
        }
        boolean res = getBean(RegisCenterService.class, request).activedDeviceByPinCode(deviceId, pincode, imei1, imei2);
        return JsonMessageBuilder.OK().withPayload(res).toMessage();
    }

    @PostMapping(value = "/{size}/{companyId}/create.json")
    public JsonMessage createByCompanyId(@PathVariable int size, @PathVariable Long companyId, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        Preconditions.checkState(size > 0, "非法的数量....");
        Optional<CompanyEntity> company = getBean(CompanyEntityAction.class, request).findById(companyId);
        Preconditions.checkState(company.isPresent(), "非法的公司ID=%s", companyId);
        getBean(DevicePinCodeEntityAction.class, request).batchCreatePinCodes(company.get(), null, size);
        return JsonMessageBuilder.OK().withPayload(true).toMessage();
    }

    @PostMapping(value = "/{pincode}/{companyId}/check4use.json")
    public JsonMessage check4use(@PathVariable String pincode, @PathVariable Long companyId, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        Optional<DevicePinCodeEntity> pinCodeEntity = getBean(DevicePinCodeEntityAction.class, request)
                .findByCode(pincode);
        Preconditions.checkState(pinCodeEntity.isPresent(), "pinCode = %s 非法...", pincode);
        Preconditions.checkState(pinCodeEntity.get().getCompanyId().equals(companyId), "pinCode = %s 所属公司异常...", pincode);
        Preconditions.checkState(!pinCodeEntity.get().isBinding(), "pinCode = %s 已经被激活使用...", pincode);
        Preconditions.checkState(pinCodeEntity.get().isEnabled(), "pinCode = %s 已经被停用...", pincode);
        Preconditions.checkState(!pinCodeEntity.get().isDeadlined(), "pinCode = %s 已经超过有效期...", pincode);
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

    @PostMapping(value = "/{pincode}/{deviceId}/touched.json")
    public JsonMessage touchpinCodeAndDeviceId(@PathVariable String pincode,
                                               @PathVariable String deviceId, HttpServletRequest request) {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
        Optional<List<DevicePinCodeEntity>> _exits = getBean(DevicePinCodeEntityAction.class, request)
                .findByCodeOrDeviceId(pincode, deviceId);
        Preconditions.checkState(_exits.isPresent(), "非法的pincode =%s", pincode);

        Optional<DevicePinCodeEntity> device_opt = _exits.get().stream()
                .filter(x -> x.getDeviceId().isPresent() && StringUtils.equals(x.getDeviceId().get(), deviceId))
                .findFirst();
        Map<String, Object> res_map = Maps.newHashMap();
        if (device_opt.isPresent()) {
            res_map.put("actived", true);
            res_map.putAll(getNetConfig(device_opt.get().getCompanyId(), request));
            return JsonMessageBuilder.OK().withPayload(res_map).toMessage();
        }

        Optional<DevicePinCodeEntity> pin_code_opt = _exits.get().stream()
                .filter(x -> StringUtils.equals(x.getPinCode(), pincode)).findFirst();
        Preconditions.checkState(pin_code_opt.isPresent(), "非法的pincode =%s", pincode);
        if (pin_code_opt.get().getDeviceId().isPresent())
            Preconditions.checkState(StringUtils.equals(pin_code_opt.get().getDeviceId().get(), deviceId),
                    "pincode = %s 已经被激活其他设备...", pincode);
        // 合法的pincode 码
        res_map.put("actived", false);
        res_map.putAll(getNetConfig(pin_code_opt.get().getCompanyId(), request));
        return JsonMessageBuilder.OK().withPayload(res_map).toMessage();
    }

    private Map<String, Object> getNetConfig(Long companyId, HttpServletRequest request) {
        Optional<CompanyEntity> company = getBean(CompanyEntityAction.class, request).findById(companyId);
        Preconditions.checkState(company.isPresent(), "id=%s 对应的公司不存在...", companyId);
        Optional<TenantNetConfigEntity> config = getBean(TenantNetConfigEntityAction.class, request)
                .findByCompany(company.get());
        Preconditions.checkState(config.isPresent(), "租户%s对应的网络配置参数未初始化...", company.get().getFullName());
        return config.get().toMap();
    }

}
