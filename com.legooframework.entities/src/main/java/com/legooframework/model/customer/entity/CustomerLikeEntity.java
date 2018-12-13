package com.legooframework.model.customer.entity;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.dict.dto.KvDictDto;

public class CustomerLikeEntity extends BaseEntity<CustomerId>{
	
	private KvDictDto trafficType,smokesType,colorSuitable,colorStyle;
	
	private String topicEnjoy,topicTaboo,foodEnjoy,colorEnjoy,brandEnjoy,clothingEnjoy;
	

	public CustomerLikeEntity(CustomerEntity customer, KvDictDto trafficType, KvDictDto smokesType, KvDictDto colorSuitable,
			KvDictDto colorStyle, String topicEnjoy, String topicTaboo, String foodEnjoy, String colorEnjoy,
			String brandEnjoy, String clothingEnjoy,LoginContext lc) {
		super(customer.getId(), lc.getTenantId(), lc.getLoginId());
		this.trafficType = trafficType;
		this.smokesType = smokesType;
		this.colorSuitable = colorSuitable;
		this.colorStyle = colorStyle;
		this.topicEnjoy = topicEnjoy;
		this.topicTaboo = topicTaboo;
		this.foodEnjoy = foodEnjoy;
		this.colorEnjoy = colorEnjoy;
		this.brandEnjoy = brandEnjoy;
		this.clothingEnjoy = clothingEnjoy;
	}
	
	public Optional<CustomerLikeEntity> modifyVaried(KvDictDto trafficType, KvDictDto smokesType, KvDictDto colorSuitable,
			KvDictDto colorStyle, String topicEnjoy, String topicTaboo, String foodEnjoy, String colorEnjoy,
			String brandEnjoy, String clothingEnjoy) {
		CustomerLikeEntity clone = (CustomerLikeEntity) this.cloneMe();
		clone.trafficType = trafficType;
		clone.smokesType = smokesType;
		clone.colorSuitable = colorSuitable;
		clone.colorStyle = colorStyle;
		clone.topicEnjoy = topicEnjoy;
		clone.topicTaboo = topicTaboo;
		clone.foodEnjoy = foodEnjoy;
		clone.colorEnjoy = colorEnjoy;
		clone.brandEnjoy = brandEnjoy;
		clone.clothingEnjoy = clothingEnjoy;
		if(clone.equals(this))
			return Optional.empty();
		return Optional.of(clone);
	}
	
	public CustomerLikeEntity(CustomerId customerId, ResultSet res) throws SQLException {
		super(customerId, res);
		String trafficTypeRes = res.getString("trafficType");
		this.trafficType = trafficTypeRes == null ? null : new KvDictDto(trafficTypeRes);
		String smokesTypeRes = res.getString("smokesType");
		this.smokesType = smokesTypeRes == null ? null : new KvDictDto(smokesTypeRes);
		String colorSuitableRes = res.getString("colorSuitable");
		this.colorSuitable = colorSuitableRes == null ? null : new KvDictDto(colorSuitableRes);
		String colorStyleRes = res.getString("colorStyle");
		this.colorStyle = colorStyleRes == null ? null : new KvDictDto(colorStyleRes);
		this.topicEnjoy = res.getString("topicEnjoy");
		this.topicTaboo = res.getString("topicTaboo");
		this.foodEnjoy = res.getString("foodEnjoy");
		this.colorEnjoy = res.getString("colorEnjoy");
		this.brandEnjoy = res.getString("brandEnjoy");
		this.clothingEnjoy = res.getString("clothingEnjoy");
	}
	
	@Override
	public Map<String, Object> toParamMap(String... excludes) {
		Map<String, Object> paramMap = super.toParamMap(excludes);
		paramMap.put("id", this.getId().getId());
		paramMap.put("channel", this.getId().getChannel().getVal());
		paramMap.put("storeId", this.getId().getStoreId());
		paramMap.put("trafficType", this.trafficType == null?null:this.trafficType.getValue());
		paramMap.put("smokesType", this.smokesType == null?null:this.smokesType.getValue());
		paramMap.put("colorSuitable",this.colorSuitable == null?null:this.colorSuitable.getValue());
		paramMap.put("colorStyle", this.colorStyle == null?null:this.colorStyle.getValue());
		paramMap.put("topicEnjoy", this.topicEnjoy);
		paramMap.put("topicTaboo", this.topicTaboo);
		paramMap.put("foodEnjoy", this.foodEnjoy);
		paramMap.put("colorEnjoy", this.colorEnjoy);
		paramMap.put("brandEnjoy", this.brandEnjoy);
		paramMap.put("clothingEnjoy", this.clothingEnjoy);
		return paramMap;
	}
	

	public KvDictDto getTrafficType() {
		return trafficType;
	}

	public KvDictDto getSmokesType() {
		return smokesType;
	}

	public KvDictDto getColorSuitable() {
		return colorSuitable;
	}

	public KvDictDto getColorStyle() {
		return colorStyle;
	}

	public String getTopicEnjoy() {
		return topicEnjoy;
	}

	public String getTopicTaboo() {
		return topicTaboo;
	}

	public String getFoodEnjoy() {
		return foodEnjoy;
	}

	public String getColorEnjoy() {
		return colorEnjoy;
	}

	public String getBrandEnjoy() {
		return brandEnjoy;
	}

	public String getClothingEnjoy() {
		return clothingEnjoy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((brandEnjoy == null) ? 0 : brandEnjoy.hashCode());
		result = prime * result + ((clothingEnjoy == null) ? 0 : clothingEnjoy.hashCode());
		result = prime * result + ((colorEnjoy == null) ? 0 : colorEnjoy.hashCode());
		result = prime * result + ((colorStyle == null) ? 0 : colorStyle.hashCode());
		result = prime * result + ((colorSuitable == null) ? 0 : colorSuitable.hashCode());
		result = prime * result + ((foodEnjoy == null) ? 0 : foodEnjoy.hashCode());
		result = prime * result + ((smokesType == null) ? 0 : smokesType.hashCode());
		result = prime * result + ((topicEnjoy == null) ? 0 : topicEnjoy.hashCode());
		result = prime * result + ((topicTaboo == null) ? 0 : topicTaboo.hashCode());
		result = prime * result + ((trafficType == null) ? 0 : trafficType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomerLikeEntity other = (CustomerLikeEntity) obj;
		if (brandEnjoy == null) {
			if (other.brandEnjoy != null)
				return false;
		} else if (!brandEnjoy.equals(other.brandEnjoy))
			return false;
		if (clothingEnjoy == null) {
			if (other.clothingEnjoy != null)
				return false;
		} else if (!clothingEnjoy.equals(other.clothingEnjoy))
			return false;
		if (colorEnjoy == null) {
			if (other.colorEnjoy != null)
				return false;
		} else if (!colorEnjoy.equals(other.colorEnjoy))
			return false;
		if (colorStyle == null) {
			if (other.colorStyle != null)
				return false;
		} else if (!colorStyle.getValue().equals(other.colorStyle.getValue()))
			return false;
		if (colorSuitable == null) {
			if (other.colorSuitable != null)
				return false;
		} else if (!colorSuitable.getValue().equals(other.colorSuitable.getValue()))
			return false;
		if (foodEnjoy == null) {
			if (other.foodEnjoy != null)
				return false;
		} else if (!foodEnjoy.equals(other.foodEnjoy))
			return false;
		if (smokesType == null) {
			if (other.smokesType != null)
				return false;
		} else if (!smokesType.getValue().equals(other.smokesType.getValue()))
			return false;
		if (topicEnjoy == null) {
			if (other.topicEnjoy != null)
				return false;
		} else if (!topicEnjoy.equals(other.topicEnjoy))
			return false;
		if (topicTaboo == null) {
			if (other.topicTaboo != null)
				return false;
		} else if (!topicTaboo.equals(other.topicTaboo))
			return false;
		if (trafficType == null) {
			if (other.trafficType != null)
				return false;
		} else if (!trafficType.getValue().equals(other.trafficType.getValue()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CustomerLikeEntity [trafficType=" + trafficType + ", smokesType=" + smokesType + ", colorSuitable="
				+ colorSuitable + ", colorStyle=" + colorStyle + ", topicEnjoy=" + topicEnjoy + ", topicTaboo="
				+ topicTaboo + ", foodEnjoy=" + foodEnjoy + ", colorEnjoy=" + colorEnjoy + ", brandEnjoy=" + brandEnjoy
				+ ", clothingEnjoy=" + clothingEnjoy + "]";
	}
	
	
}
