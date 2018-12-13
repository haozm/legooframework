package com.legooframework.model.devices.event;

import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.devices.entity.DeviceEntity.OsType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

public class DeviceModuleEvent extends LegooEvent {

    DeviceModuleEvent(String source, String eventName) {
        super(source, "devices", eventName);
    }

    public void setId(String id) {
        super.putPayload("id", id);
    }

    public String getId() {
        return super.getString("id");
    }

    public void setImei(String imei) {
        super.putPayload("imei", imei);
    }

    public String getImei() {
        return super.getString("imei");
    }

    public void setName(String name) {
        super.putPayload("name", name);
    }

    public String getName() {
        return super.getString("name");
    }

    public void setBrand(String brand) {
        super.putPayload("brand", brand);
    }

    public String getBrand() {
        return super.getString("brand");
    }

    public void setModel(String model) {
        super.putPayload("model", model);
    }

    public String getModel() {
        return super.getString("model");
    }

    public void setColor(String color) {
        super.putPayload("color", color);
    }

    public String getColor() {
        return super.getString("color");
    }

    public void setCpu(String cpu) {
        super.putPayload("cpu", cpu);
    }

    public String getCpu() {
        return super.getString("cpu");
    }

    public void setMemorySize(Integer memorySize) {
        super.putPayload("memorySize", memorySize);
    }

    public Integer getMemorySize() {
        Optional<Integer> intOpt = super.getValue("memorySize", Integer.class);
        if (intOpt.isPresent())
            return intOpt.get();
        return null;
    }

    public void setOs(String os) {
        super.putPayload("os", os);
    }

    public String getOs() {
        return super.getString("os");
    }

    public void setXportOs(String xportOs) {
        super.putPayload("xportOs", xportOs);
    }

    public String getXportOs() {
        return super.getString("xportOs");
    }

    public void setScreenSize(Double screenSize) {
        super.putPayload("screenSize", screenSize);
    }

    public Double getScreenSize() {
        Optional<Double> doubleOpt = super.getValue("screenSize", Double.class);
        if (doubleOpt.isPresent())
            return doubleOpt.get();
        return null;
    }

    public void setOsType(OsType osType) {
        super.putPayload("osType", osType);
    }

    public OsType getOsType() {
        Optional<OsType> osTypeOpt = super.getValue("osType", OsType.class);
        if (osTypeOpt.isPresent())
            return osTypeOpt.get();
        return null;
    }

    public void setPrice(BigDecimal price) {
        super.putPayload("price", price);
    }

    public BigDecimal getPrice() {
        Optional<BigDecimal> bigDecimalOpt = super.getValue("price", BigDecimal.class);
        if (bigDecimalOpt.isPresent())
            return bigDecimalOpt.get();
        return null;
    }


    public void setProductionDate(Date productionDate) {
        super.putPayload("productionDate", productionDate);
    }

    public Date getProductionDate() {
        Optional<Date> dateOpt = super.getValue("productionDate", Date.class);
        if (dateOpt.isPresent())
            return dateOpt.get();
        return null;
    }

    public void setRepairReason(String repairReason) {
        super.putPayload("repairReason", repairReason);
    }

    public String getRepairReason() {
        return super.getString("repairReason");
    }

    public void setScrapReason(String scrapReason) {
        super.putPayload("scrapReason", scrapReason);
    }

    public String getScrapReason() {
        return super.getString("scrapReason");
    }

}
