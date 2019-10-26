package com.legooframework.model.devices.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.devices.dto.DeviceDto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class DeviceEntity extends BaseEntity<String> {

    // 设备序列号
    private final String imei;
    // 设备名称
    private final String name;
    // 设备品牌
    private final String brand;
    // 设备型号
    private final String model;
    // 设备颜色
    private final String color;
    // 设备CPU
    private final String cpu;
    // 设备内存大小 以M为单位
    private final int memorySize;
    // 设备操作系统
    private final String os;
    // 设备Xport系统
    private final String xportOs;
    // 设备屏幕大小 以英寸为单位
    private final double screenSize;
    // 设备操作系统类型，分安卓系统与苹果系统
    private final OsType osType;
    // 设备价格 以元为单位
    private final BigDecimal price;
    // 设备状态：1：正常使用状态 2：维修状态 3：报废状态
    private State state;
    // 设备出厂时间
    private final long productionDate;
    // 设备维修原因
    private String repairReason;
    // 设备报废原因
    private String scrapReason;
    //设备imei1
    private String imei1;
    //设备imei2
    private String imei2;
    
    private DeviceEntity(String id, String imei, String name, String brand, String model, String color, String cpu,
                         int memorySize, String os, String xportOs, double screenSize, OsType osType, BigDecimal price, State state,
                         long productionDate, String repairReason, String scrapReason,String imei1,String imei2) {
        super(id);
        this.imei = imei;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.cpu = cpu;
        this.memorySize = memorySize;
        this.os = os;
        this.xportOs = xportOs;
        this.screenSize = screenSize;
        this.osType = osType;
        this.price = price;
        this.state = state;
        this.productionDate = productionDate;
        this.repairReason = repairReason;
        this.scrapReason = scrapReason;
        this.imei1 = imei1;
        this.imei2 = imei2;
    }

    private DeviceEntity(DeviceEntity orgin) {
        super(orgin.getId());
        this.imei = orgin.getImei();
        this.name = orgin.getName();
        this.brand = orgin.getBrand();
        this.model = orgin.getModel();
        this.color = orgin.getColor();
        this.cpu = orgin.getCpu();
        this.memorySize = orgin.getMemorySize();
        this.os = orgin.getOs();
        this.xportOs = orgin.getXportOs();
        this.screenSize = orgin.getScreenSize();
        this.osType = orgin.getOsType();
        this.price = orgin.getPrice();
        this.state = orgin.getState();
        this.productionDate = orgin.getProductionDate();
        this.repairReason = orgin.getRepairReason();
        this.scrapReason = orgin.getScrapReason();
        this.imei1 = orgin.imei1;
        this.imei2 = orgin.imei2;
    }

    public static enum State {

        NORMAL("正常", 1), REPAIR("维修", 2), SCRAP("报废", 3);

        private final String name;

        private final int value;

        private State(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public static State valueOf(int value) {
            for (State state : State.values()) {
                if (state.value == value)
                    return state;
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

    }

    public enum OsType {

        IOS("苹果系统", 2), ANDROID("安卓系统", 1);

        private final String name;

        private final int value;

        private OsType(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        public static OsType valueOf(int value) {
            for (OsType type : values()) {
                if (type.value == value)
                    return type;
            }
            return null;
        }

    }

    static class Builder {

        private final String id;
        private final String imei;
        private String name;
        private String brand;
        private String model;
        private String color;
        private String cpu;
        private int memorySize;
        private String os;
        private String xportOs;
        private double screenSize;
        private OsType osType;
        private BigDecimal price;
        private State state;
        private long productionDate;
        private String repairReason;
        private String scrapReason;
        private String imei1;
        private String imei2;
        
        Builder(DeviceEntity orgin) {
            this.id = orgin.getId();
            this.imei = orgin.getImei();
            this.name = orgin.getName();
            this.brand = orgin.getBrand();
            this.model = orgin.getModel();
            this.color = orgin.getColor();
            this.cpu = orgin.getCpu();
            this.memorySize = orgin.getMemorySize();
            this.os = orgin.getOs();
            this.xportOs = orgin.getXportOs();
            this.screenSize = orgin.getScreenSize();
            this.osType = orgin.getOsType();
            this.price = orgin.getPrice();
            this.state = orgin.getState();
            this.productionDate = orgin.getProductionDate();
            this.repairReason = orgin.getRepairReason();
            this.scrapReason = orgin.getScrapReason();
        }

        Builder(String id, String imei, String name) {
            if (Strings.isNullOrEmpty(id))
                throw new IllegalArgumentException("非法入参ID");
            fixedLengthCheck(id, 32);
            this.id = id;
            maxLengthCheck(imei, 128);
            this.imei = imei;
            maxLengthCheck(name, 128);
            this.name = name;
            this.state = State.NORMAL;
            this.osType = OsType.ANDROID;
        }

        private void maxLengthCheck(String data, int length) {
            if (data.length() > length)
                throw new IllegalArgumentException(String.format("非法参数 %s 小于最大长度%s", data, length));
        }

        private void fixedLengthCheck(String data, int length) {
            if (data.length() != length)
                throw new IllegalArgumentException(String.format("非法参数%s 长度必须是%s", data, length));
        }

        Builder name(String name) {
            if (name != null) {
                maxLengthCheck(name, 128);
                this.name = name;
            }
            return this;
        }

        Builder brand(String brand) {
            if (brand != null) {
                maxLengthCheck(brand, 128);
                this.brand = brand;
            }
            return this;
        }

        Builder model(String model) {
            if (model != null) {
                maxLengthCheck(model, 128);
                this.model = model;
            }

            return this;
        }

        Builder color(String color) {
            if (color != null) {
                maxLengthCheck(color, 64);
                this.color = color;
            }
            return this;
        }

        Builder cpu(String cpu) {
            if (cpu != null) {
                maxLengthCheck(cpu, 64);
                this.cpu = cpu;
            }
            return this;
        }

        Builder memorySize(Integer size) {
            if (size != null && size < 99999999)
                this.memorySize = size;
            return this;
        }

        Builder os(String os) {
            if (os != null) {
                maxLengthCheck(os, 64);
                this.os = os;
            }
            return this;
        }

        Builder xportOs(String os) {
            if (os != null) {
                maxLengthCheck(os, 64);
                this.xportOs = os;
            }
            return this;
        }

        Builder screenSize(Double size) {
            if (size != null && size < 99) {
                this.screenSize = size;
            }
            return this;
        }

        Builder osType(OsType osType) {
            if (osType != null)
                this.osType = osType;
            return this;
        }

        Builder price(BigDecimal price) {
            if (price != null && price.doubleValue() < 99999999) {
                this.price = price;
            }
            return this;
        }

        Builder state(State state) {
            if (state != null)
                this.state = state;
            return this;
        }

        Builder productionDate(Date date) {
            if (date != null)
                this.productionDate = date.getTime();
            return this;
        }

        Builder repairReason(String reason) {
            if (reason != null) {
                maxLengthCheck(reason, 512);
                this.repairReason = reason;
            }
            return this;
        }

        Builder scrapReason(String reason) {
            if (reason != null) {
                maxLengthCheck(reason, 512);
                this.scrapReason = reason;
            }
            return this;
        }
        
        Builder imei1(String imei1) {
        	this.imei1 = imei1;
        	return this;
        }
        
        Builder imei2(String imei2) {
        	this.imei2 = imei2;
        	return this;
        }
        
        DeviceEntity build() {
            return new DeviceEntity(id, imei, name, brand, model, color, cpu, memorySize, os, xportOs, screenSize,
                    osType, price, state, productionDate, repairReason, scrapReason,imei1,imei2);
        }
    }

    /**
     * 判断该设备是否IOS系统
     *
     * @return
     */
    public boolean isIosSystem() {
        if (this.osType == OsType.IOS)
            return true;
        return false;
    }

    public boolean isAndroidSystem() {
        if (this.osType == OsType.ANDROID)
            return true;
        return false;
    }

    /**
     * 判断该设备是否是正常使用状态
     *
     * @return
     */
    public boolean isNormalState() {
        if (this.state == null)
            return false;
        if (this.state == State.NORMAL)
            return true;
        return false;
    }

    /**
     * 判断该设备是否使维修状态
     *
     * @return
     */
    public boolean isReqairState() {
        if (this.state == null)
            return false;
        if (this.state == State.REPAIR)
            return true;
        return false;
    }

    /**
     * 判断设备是否使报废状态
     *
     * @return
     */
    public boolean isScrapState() {
        if (this.state == null)
            return false;
        if (this.state == State.SCRAP)
            return true;
        return false;
    }

    /**
     * 将设备状态更新为正常使用状态
     *
     * @return
     */
    public DeviceEntity execNormal() {
        DeviceEntity clone = new DeviceEntity(this);
        clone.state = State.NORMAL;
        return clone;
    }

    /**
     * 将设备状态更新为维修状态
     *
     * @return
     */
    public DeviceEntity execReqair() {
        DeviceEntity clone = new DeviceEntity(this);
        clone.state = State.REPAIR;
        return clone;
    }

    /**
     * 将设备状态更新报废状态
     *
     * @return
     */
    public DeviceEntity execScrap() {
        DeviceEntity clone = new DeviceEntity(this);
        clone.state = State.SCRAP;
        return clone;
    }

    public State getState() {
        return state;
    }

    public String getRepairReason() {
        return repairReason;
    }

    /**
     * 设置维修原因
     *
     * @param repairReason
     * @return
     */
    public DeviceEntity modifyRepairReason(String repairReason) {
        DeviceEntity clone = new DeviceEntity(this);
        clone.repairReason = repairReason;
        return clone;
    }

    public String getScrapReason() {
        return scrapReason;
    }

    /**
     * 设置报废原因
     *
     * @param scrapReason
     */
    public DeviceEntity modifyScrapReason(String scrapReason) {
        DeviceEntity clone = new DeviceEntity(this);
        clone.scrapReason = scrapReason;
        return clone;
    }

    /**
     * 生成Dto
     *
     * @return
     */
    public DeviceDto createDto() {
        return new DeviceDto(this.getId(), this.imei, this.name, this.state.value);
    }

    public String getImei() {
        return imei;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getColor() {
        return color;
    }

    public String getCpu() {
        return cpu;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public String getOs() {
        return os;
    }

    public String getXportOs() {
        return xportOs;
    }

    public double getScreenSize() {
        return screenSize;
    }

    public OsType getOsType() {
        return osType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public long getProductionDate() {
        return productionDate;
    }
    
    public String getImei1() {
    	return this.imei1;
    }
    
    public Optional<String> getImei2(){
    	return this.imei2 == null ? Optional.empty():Optional.of(this.imei2);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DeviceEntity that = (DeviceEntity) o;
        return Objects.equal(imei, that.imei);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), imei);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("id", this.getId());
        paramMap.put("imei", this.getImei());
        paramMap.put("name", this.getName());
        paramMap.put("brand", this.getBrand());
        paramMap.put("model", this.getModel());
        paramMap.put("color", this.getColor());
        paramMap.put("cpu", this.getCpu());
        paramMap.put("memorySize", this.getMemorySize());
        paramMap.put("os", this.getOs());
        paramMap.put("xportOs", this.getXportOs());
        paramMap.put("screenSize", this.getScreenSize());
        paramMap.put("osType", this.getOsType().value);
        paramMap.put("price", this.getPrice());
        paramMap.put("state", this.getState().value);
        paramMap.put("productionDate", this.getProductionDate() != 0L ? new Date(this.getProductionDate()) : null);
        paramMap.put("reqairReason", this.getRepairReason());
        paramMap.put("scrapReason", this.getScrapReason());
        paramMap.put("imei1", this.imei1);
        paramMap.put("imei2", this.imei2);
        return paramMap;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("imei", imei)
                .add("name", name)
                .add("brand", brand)
                .add("model", model)
                .add("color", color)
                .add("cpu", cpu)
                .add("memorySize", memorySize)
                .add("os", os)
                .add("xportOs", xportOs)
                .add("screenSize", screenSize)
                .add("osType", osType)
                .add("price", price)
                .add("state", state)
                .add("productionDate", productionDate)
                .add("repairReason", repairReason)
                .add("scrapReason", scrapReason)
                .toString();
    }
}
