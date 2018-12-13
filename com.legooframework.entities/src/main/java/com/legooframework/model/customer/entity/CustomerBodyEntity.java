package com.legooframework.model.customer.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.dict.dto.KvDictDto;

public class CustomerBodyEntity extends BaseEntity<CustomerId>{
	
	private int bodyHeight,bodyWeight;
	
	private KvDictDto skinType,bodyType,shoulderType;
	
	private int shoulderWidth;
	
	private int bustSize,bustUpSize,bustDownSize;
	
	private KvDictDto waistlineSize;
	
	private int bottomSize,thighSize,kneeSize,footLength;

	public CustomerBodyEntity(CustomerEntity customer, int bodyHeight, int bodyWeight, KvDictDto skinType, KvDictDto bodyType,
			KvDictDto shoulderType, int shoulderWidth, int bustSize, int bustUpSize, int bustDownSize,
			KvDictDto waistlineSize, int bottomSize, int thighSize, int kneeSize, int footLength,LoginContext lc) {
		super(customer.getId(), lc.getTenantId(), lc.getLoginId());
		this.bodyHeight = bodyHeight;
		this.bodyWeight = bodyWeight;
		this.skinType = skinType;
		this.bodyType = bodyType;
		this.shoulderType = shoulderType;
		this.shoulderWidth = shoulderWidth;
		this.bustSize = bustSize;
		this.bustUpSize = bustUpSize;
		this.bustDownSize = bustDownSize;
		this.waistlineSize = waistlineSize;
		this.bottomSize = bottomSize;
		this.thighSize = thighSize;
		this.kneeSize = kneeSize;
		this.footLength = footLength;
	}
	
	public Optional<CustomerBodyEntity> modifyVaried(int bodyHeight, int bodyWeight, KvDictDto skinType, KvDictDto bodyType,
			KvDictDto shoulderType, int shoulderWidth, int bustSize, int bustUpSize, int bustDownSize,
			KvDictDto waistlineSize, int bottomSize, int thighSize, int kneeSize, int footLength){
		CustomerBodyEntity clone = (CustomerBodyEntity) this.cloneMe();
		clone.bodyHeight = bodyHeight;
		clone.bodyWeight = bodyWeight;
		clone.skinType = skinType;
		clone.bodyType = bodyType;
		clone.shoulderType = shoulderType;
		clone.shoulderWidth = shoulderWidth;
		clone.bustSize = bustSize;
		clone.bustUpSize = bustUpSize;
		clone.bustDownSize = bustDownSize;
		clone.waistlineSize = waistlineSize;
		clone.bottomSize = bottomSize;
		clone.thighSize = thighSize;
		clone.kneeSize = kneeSize;
		clone.footLength = footLength;
		if(clone.equals(this))
			return Optional.empty();
		return Optional.of(clone);
	}
	
	
	public CustomerBodyEntity(CustomerId customerId, ResultSet res) throws SQLException {
		super(customerId, res);
		this.bodyHeight = res.getInt("bodyHeight");
		this.bodyWeight = res.getInt("bodyWeight");
		String skinTypeRes = res.getString("skinType");
		this.skinType = skinTypeRes == null ? null : new KvDictDto(skinTypeRes);
		String bodyTypeRes = res.getString("skinType");
		this.bodyType = bodyTypeRes == null ? null : new KvDictDto(bodyTypeRes);
		String shoulderTypeRes = res.getString("skinType");
		this.shoulderType = shoulderTypeRes == null ? null : new KvDictDto(shoulderTypeRes);
		String waistlineSizeRes = res.getString("skinType");
		this.waistlineSize = waistlineSizeRes == null ? null : new KvDictDto(waistlineSizeRes);
		this.shoulderWidth = res.getInt("shoulderWidth");
		this.bustSize = res.getInt("bustSize");
		this.bustUpSize = res.getInt("bustUpSize");
		this.bustDownSize = res.getInt("bustDownSize");
		this.bottomSize = res.getInt("bottomSize");
		this.thighSize = res.getInt("thighSize");
		this.kneeSize = res.getInt("kneeSize");
		this.footLength = res.getInt("footLength");
	}
	
	

	@Override
	public Map<String, Object> toParamMap(String... excludes) {
		Map<String, Object> paramMap = super.toParamMap(excludes);
		paramMap.put("id", this.getId().getId());
		paramMap.put("channel", this.getId().getChannel().getVal());
		paramMap.put("storeId", this.getId().getStoreId());
		paramMap.put("bodyHeight", this.bodyHeight);
		paramMap.put("bodyWeight", this.bodyWeight);
		paramMap.put("shoulderWidth", this.shoulderWidth);
		paramMap.put("bustSize", this.bustSize);
		paramMap.put("bustUpSize", this.bustUpSize);
		paramMap.put("bustDownSize", this.bustDownSize);
		paramMap.put("bottomSize", this.bottomSize);
		paramMap.put("thighSize", this.thighSize);
		paramMap.put("kneeSize", this.kneeSize);
		paramMap.put("footLength", this.footLength);
		paramMap.put("skinType", this.skinType == null?null:this.skinType.getValue());
		paramMap.put("bodyType", this.bodyType == null?null:this.bodyType.getValue());
		paramMap.put("shoulderType", this.shoulderType == null?null:this.shoulderType.getValue());
		paramMap.put("waistlineSize", this.waistlineSize == null?null:this.waistlineSize.getValue());
		return paramMap;
	}

	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	public int getBodyHeight() {
		return bodyHeight;
	}


	public int getBodyWeight() {
		return bodyWeight;
	}


	public KvDictDto getSkinType() {
		return skinType;
	}


	public KvDictDto getBodyType() {
		return bodyType;
	}


	public KvDictDto getShoulderType() {
		return shoulderType;
	}


	public int getShoulderWidth() {
		return shoulderWidth;
	}


	public int getBustSize() {
		return bustSize;
	}


	public int getBustUpSize() {
		return bustUpSize;
	}


	public int getBustDownSize() {
		return bustDownSize;
	}


	public KvDictDto getWaistlineSize() {
		return waistlineSize;
	}


	public int getBottomSize() {
		return bottomSize;
	}


	public int getThighSize() {
		return thighSize;
	}


	public int getKneeSize() {
		return kneeSize;
	}


	public int getFootLength() {
		return footLength;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + bodyHeight;
		result = prime * result + ((bodyType == null) ? 0 : bodyType.hashCode());
		result = prime * result + bodyWeight;
		result = prime * result + bottomSize;
		result = prime * result + bustDownSize;
		result = prime * result + bustSize;
		result = prime * result + bustUpSize;
		result = prime * result + footLength;
		result = prime * result + kneeSize;
		result = prime * result + ((shoulderType == null) ? 0 : shoulderType.hashCode());
		result = prime * result + shoulderWidth;
		result = prime * result + ((skinType == null) ? 0 : skinType.hashCode());
		result = prime * result + thighSize;
		result = prime * result + ((waistlineSize == null) ? 0 : waistlineSize.hashCode());
		return result;
	}
	
	
	@Override
	public String toString() {
		return "CustomerBodyEntity [bodyHeight=" + bodyHeight + ", bodyWeight=" + bodyWeight + ", skinType=" + skinType
				+ ", bodyType=" + bodyType + ", shoulderType=" + shoulderType + ", shoulderWidth=" + shoulderWidth
				+ ", bustSize=" + bustSize + ", bustUpSize=" + bustUpSize + ", bustDownSize=" + bustDownSize
				+ ", waistlineSize=" + waistlineSize + ", bottomSize=" + bottomSize + ", thighSize=" + thighSize
				+ ", kneeSize=" + kneeSize + ", footLength=" + footLength + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomerBodyEntity other = (CustomerBodyEntity) obj;
		if (bodyHeight != other.bodyHeight)
			return false;
		if (bodyType == null) {
			if (other.bodyType != null)
				return false;
		} else if (!bodyType.getValue().equals(other.bodyType.getValue()))
			return false;
		if (bodyWeight != other.bodyWeight)
			return false;
		if (bottomSize != other.bottomSize)
			return false;
		if (bustDownSize != other.bustDownSize)
			return false;
		if (bustSize != other.bustSize)
			return false;
		if (bustUpSize != other.bustUpSize)
			return false;
		if (footLength != other.footLength)
			return false;
		if (kneeSize != other.kneeSize)
			return false;
		if (shoulderType == null) {
			if (other.shoulderType != null)
				return false;
		} else if (!shoulderType.getValue().equals(other.shoulderType.getValue()))
			return false;
		if (shoulderWidth != other.shoulderWidth)
			return false;
		if (skinType == null) {
			if (other.skinType != null)
				return false;
		} else if (!skinType.getValue().equals(other.skinType.getValue()))
			return false;
		if (thighSize != other.thighSize)
			return false;
		if (waistlineSize == null) {
			if (other.waistlineSize != null)
				return false;
		} else if (!waistlineSize.getValue().equals(other.waistlineSize.getValue()))
			return false;
		return true;
	}
	
}
