package com.legooframework.model.devices.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.devices.entity.DeviceEntity;
import com.legooframework.model.devices.entity.DeviceEntity.State;

import java.util.Map;
import java.util.Optional;

public class DeviceDto {
	
	private final String id;
	
    private final String imei;

    private final String name;

    private final State state;

    public DeviceDto(String id,String imei, String name, Integer state) {
    	this.id = id;
        this.imei = imei;
        this.name = name;
        Preconditions.checkNotNull(state);
        Preconditions.checkArgument(state<= 3,"非法的状态取值");
        this.state = DeviceEntity.State.valueOf(state);
    }

    public String getImei() {
        return imei;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }
    

	public String getId() {
		return id;
	}
    
	
    public DeviceEntity.State getState() {
        return state;
    }

    /**
     * 判断该设备是否是正常使用状态
     *
     * @return
     */
    public boolean isNormalState() {
        return this.state == State.NORMAL;
    }

    /**
     * 判断该设备是否使维修状态
     *
     * @return
     */
    public boolean isReqairState() {
        return this.state == State.REPAIR;
    }

    /**
     * 判断设备是否使报废状态
     *
     * @return
     */
    public boolean isScrapState() {
        return this.state == State.SCRAP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceDto)) return false;
        DeviceDto deviceDto = (DeviceDto) o;
        return Objects.equal(id, deviceDto.id) &&
        		Objects.equal(imei, deviceDto.imei) &&
                Objects.equal(name, deviceDto.name) &&
                state == deviceDto.state;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id,imei, name, state);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
        		.add("id", id)
                .add("imei", imei)
                .add("name", name)
                .add("state", state)
                .toString();
    }
    
    public Map<String,Object> toMap(){
    	Map<String,Object> map = Maps.newHashMap();
    	map.put("id", this.id);
    	map.put("imei", this.imei);
    	map.put("name", this.name);
    	map.put("state", String.format("%s:%s", state.getValue(),state.getName()));
    	return map;
    }

	}

