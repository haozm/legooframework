package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;

import java.util.Map;
import java.util.Optional;

public class RegisCenterProxyAction extends BaseEntityAction<EmptyEntity> {

    public RegisCenterProxyAction() {
        super(null);
    }

    private void verifyPinCode(int pincode) {
        Preconditions.checkArgument(pincode >= 100000 && pincode <= 999999, "非法的pinCode = %s", pincode);
    }

    /**
     * 远程激活设别
     *
     * @param deviceId  是个河北ID
     * @param pincode   设备扣款
     * @param storeId   门店
     * @param companyId 公司
     */
    public void activeDeivce(String deviceId, int pincode, Integer storeId, Integer companyId) {
        verifyPinCode(pincode);
        Map<String, Object> params = Maps.newHashMap();
        params.put("deviceId", deviceId);
        params.put("pincode", pincode);
        params.put("storeId", storeId);
        params.put("companyId", companyId);
        params.put("bundle", "regiscenter");
        Optional<JsonElement> jsonElement = super.post(companyId, "regiscenterActiveDevice", params);
        Preconditions.checkState(jsonElement.isPresent(), "激活设备无结果返回...,参考信息：%s", params);
        boolean active_res = jsonElement.get().getAsBoolean();
        Preconditions.checkState(active_res, "激活设备失败....,参考信息：%s", params);
    }

    public boolean check4use(Integer companyId, int pincode) {
        verifyPinCode(pincode);
        Optional<JsonElement> jsonElement = super.post(companyId, "regiscenterCheck4use", null, pincode, companyId);
        Preconditions.checkState(jsonElement.isPresent(), "请求无结果返回...,companyId：%s ，pincode：%s", companyId, pincode);
        return jsonElement.get().getAsBoolean();
    }


}
