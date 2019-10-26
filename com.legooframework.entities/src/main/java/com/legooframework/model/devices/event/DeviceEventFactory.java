package com.legooframework.model.devices.event;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.devices.entity.DeviceEntity.OsType;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public final class DeviceEventFactory {

    private static String EVENT_SAVEORUPDATEDEVICE = "saveOrUpdateDeviceEvent";

    private static String EVENT_LOADDEVICEBYIMEIEVENT = "loadDeviceByImeiEvent";

    private static String EVENT_LOADDEVICEDTOBYIDEVENT = "loadDeviceDtoByIdEvent";

    private static String EVENT_CHANGEDEVICETONORMALEVENT = "changeDeviceToNormalEvent";

    private static String EVENT_CHANGEDEVICETOREPAIREVENT = "changeDeviceToRepairEvent";

    private static String EVENT_CHANGEDEVICETOSCRAPEVENT = "changeDeviceToScrapEvent";

    private DeviceEventFactory() {
        throw new AssertionError();
    }

    public static DeviceModuleEvent loadDeviceByImeiEvent(Bundle source, String imei) {
        Objects.requireNonNull(source);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(imei), "入参 imei 不可以为空.");
        DeviceModuleEvent event = new DeviceModuleEvent(source.getName(), EVENT_LOADDEVICEBYIMEIEVENT);
        event.setImei(imei);
        return event;
    }

    public static DeviceModuleEvent loadDeviceDtoByIdEvent(Bundle source, String id) {
        Objects.requireNonNull(source);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "入参 id 不可以为空.");
        DeviceModuleEvent event = new DeviceModuleEvent(source.getName(), EVENT_LOADDEVICEDTOBYIDEVENT);
        event.setId(id);
        return event;
    }

    public static DeviceModuleEvent changeDeviceToNormalEvent(Bundle source, String imei) {
        Objects.requireNonNull(source);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(imei), "入参 imei 不可以为空.");
        DeviceModuleEvent event = new DeviceModuleEvent(source.getName(), EVENT_CHANGEDEVICETONORMALEVENT);
        event.setImei(imei);
        return event;
    }

    public static DeviceModuleEvent changeDeviceToRepairEvent(Bundle source, String imei) {
        Objects.requireNonNull(source);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(imei), "入参 imei 不可以为空.");
        DeviceModuleEvent event = new DeviceModuleEvent(source.getName(), EVENT_CHANGEDEVICETOREPAIREVENT);
        event.setImei(imei);
        return event;
    }

    public static DeviceModuleEvent changeDeviceToScrapEvent(Bundle source, String imei) {
        Objects.requireNonNull(source);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(imei), "入参 imei 不可以为空.");
        DeviceModuleEvent event = new DeviceModuleEvent(source.getName(), EVENT_CHANGEDEVICETOSCRAPEVENT);
        event.setImei(imei);
        return event;
    }

    public static DeviceModuleEvent saveOrUpdateEvent(Bundle source, String imei, String name, String brand,
                                                      String model, String color, String cpu, Integer memorySize, String os, String xportOs, Double screenSize,
                                                      OsType osType, BigDecimal price, Date productionDate, String repairReason, String scrapReason) {
        Objects.requireNonNull(source);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(imei), "入参 imei 不可以为空.");
        DeviceModuleEvent event = new DeviceModuleEvent(source.getName(), EVENT_SAVEORUPDATEDEVICE);
        event.setImei(imei);
        event.setName(name);
        event.setBrand(brand);
        event.setModel(model);
        event.setColor(color);
        event.setCpu(cpu);
        event.setMemorySize(memorySize);
        event.setOs(os);
        event.setXportOs(xportOs);
        event.setScreenSize(screenSize);
        event.setOsType(osType);
        event.setPrice(price);
        event.setProductionDate(productionDate);
        event.setRepairReason(repairReason);
        event.setScrapReason(scrapReason);
        return event;
    }

    public static DeviceModuleEvent saveOrUpdateEvent(Bundle source, String imei, String name) {
        Objects.requireNonNull(source);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(imei), "入参 imei 不可以为空.");
        DeviceModuleEvent event = new DeviceModuleEvent(source.getName(), EVENT_SAVEORUPDATEDEVICE);
        event.setImei(imei);
        event.setName(name);
        return event;
    }

    public static boolean isLoadDeviceByImeiEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_LOADDEVICEBYIMEIEVENT);
    }

    public static boolean isLoadDeviceDtoByIdEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_LOADDEVICEDTOBYIDEVENT);
    }

    public static boolean isSaveOrUpdateDeviceEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_SAVEORUPDATEDEVICE);
    }

    public static boolean isChangeDeviceToNormalEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_CHANGEDEVICETONORMALEVENT);
    }

    public static boolean isChangeDeviceToRepairEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_CHANGEDEVICETOREPAIREVENT);
    }

    public static boolean isChangeDeviceToScrapEvent(LegooEvent event) {
        return StringUtils.equals(event.getEventName(), EVENT_CHANGEDEVICETOSCRAPEVENT);
    }
}
