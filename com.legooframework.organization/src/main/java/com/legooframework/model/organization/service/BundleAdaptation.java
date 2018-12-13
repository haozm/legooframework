package com.legooframework.model.organization.service;

import com.legooframework.model.devices.dto.DeviceDto;

import java.util.Optional;

// 事件适配其他模块
public class BundleAdaptation extends  OrgService {

    // 调用接口 新增或者更新一台设备，与租户无关
    public void saveOrUpdateDeviceBaseInfo(){
    }

    // 通过ID 加载设备基本信息
    public Optional<DeviceDto> loadDeviceDtoById(String id){
        return Optional.empty();
    }

    // 通过IME 号加载设备基本信息
    public Optional<DeviceDto> loadDeviceDtoByImei(String imei){
        return Optional.empty();
    }

}
