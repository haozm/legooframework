package com.legooframework.model.customer.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.dict.dto.KvDictDto;

public class CustomerClothesEntity extends BaseEntity<CustomerId>{
	
	private KvDictDto coatUpSize,coatDownSize,underWearSize,pantiesSize,shoesSize;
	
	private int crotchBefore,crotchAfter,pantsLength,coatLength,sleeveLength;
	
	
	protected CustomerClothesEntity(CustomerId id) {
		super(id);
	}


	public CustomerClothesEntity(CustomerEntity customer, KvDictDto coatUpSize,
			KvDictDto coatDownSize, KvDictDto underWearSize, KvDictDto pantiesSize, KvDictDto shoesSize,
			int crotchBefore, int crotchAfter, int pantsLength, int coatLength, int sleeveLength,LoginContext lc) {
		super(customer.getId(), lc.getTenantId(), lc.getLoginId());
		this.coatUpSize = coatUpSize;
		this.coatDownSize = coatDownSize;
		this.underWearSize = underWearSize;
		this.pantiesSize = pantiesSize;
		this.shoesSize = shoesSize;
		this.crotchBefore = crotchBefore;
		this.crotchAfter = crotchAfter;
		this.pantsLength = pantsLength;
		this.coatLength = coatLength;
		this.sleeveLength = sleeveLength;
	}

	public Optional<CustomerClothesEntity> modifyVaried(KvDictDto coatUpSize,
			KvDictDto coatDownSize, KvDictDto underWearSize, KvDictDto pantiesSize, KvDictDto shoesSize,
			int crotchBefore, int crotchAfter, int pantsLength, int coatLength, int sleeveLength){
		CustomerClothesEntity clone = (CustomerClothesEntity) this.cloneMe();
		clone.coatUpSize = coatUpSize;
		clone.coatDownSize = coatDownSize;
		clone.underWearSize = underWearSize;
		clone.pantiesSize = pantiesSize;
		clone.shoesSize = shoesSize;
		clone.crotchBefore = crotchBefore;
		clone.crotchAfter = crotchAfter;
		clone.pantsLength = pantsLength;
		clone.coatLength = coatLength;
		clone.sleeveLength = sleeveLength;
		if(clone.equals(this))
			return Optional.empty();
		return Optional.of(clone);
	}
	
	public CustomerClothesEntity(CustomerId customerId, ResultSet res) throws SQLException {
		super(customerId, res);
		String coatUpSizeRes = res.getString("coatUpSize");
		this.coatUpSize = coatUpSizeRes == null ? null : new KvDictDto(coatUpSizeRes);
		String coatDownSizeRes = res.getString("coatDownSize");
		this.coatDownSize = coatDownSizeRes == null ? null : new KvDictDto(coatDownSizeRes);
		String underWearSizeRes = res.getString("underWearSize");
		this.underWearSize = underWearSizeRes == null ? null : new KvDictDto(underWearSizeRes);
		String pantiesSizeRes = res.getString("pantiesSize");
		this.pantiesSize = pantiesSizeRes == null ? null : new KvDictDto(pantiesSizeRes);
		String shoesSizeRes = res.getString("shoesSize");
		this.shoesSize = shoesSizeRes == null ? null : new KvDictDto(shoesSizeRes);
		this.crotchBefore = res.getInt("crotchBefore");
		this.crotchAfter = res.getInt("crotchAfter");
		this.pantsLength = res.getInt("pantsLength");
		this.coatLength = res.getInt("coatLength");
		this.sleeveLength = res.getInt("sleeveLength");
		this.crotchAfter = res.getInt("crotchAfter");
	}
	
	@Override
	public Map<String, Object> toParamMap(String... excludes) {
		Map<String, Object> paramMap = super.toParamMap(excludes);
		paramMap.put("id", this.getId().getId());
		paramMap.put("channel", this.getId().getChannel().getVal());
		paramMap.put("storeId", this.getId().getStoreId());
		paramMap.put("coatUpSize", this.coatUpSize == null?null:this.coatUpSize.getValue());
		paramMap.put("coatDownSize", this.coatDownSize == null?null:this.coatDownSize.getValue());
		paramMap.put("underWearSize",this.underWearSize == null?null:this.underWearSize.getValue());
		paramMap.put("pantiesSize", this.pantiesSize == null?null:this.pantiesSize.getValue());
		paramMap.put("shoesSize", this.underWearSize == null?null:this.underWearSize.getValue());
		paramMap.put("crotchBefore", this.crotchBefore);
		paramMap.put("crotchAfter", this.crotchAfter);
		paramMap.put("pantsLength", this.pantsLength);
		paramMap.put("coatLength", this.coatLength);
		paramMap.put("sleeveLength", this.sleeveLength);
		return paramMap;
	}


	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}


	public KvDictDto getCoatUpSize() {
		return coatUpSize;
	}


	public KvDictDto getCoatDownSize() {
		return coatDownSize;
	}


	public KvDictDto getUnderWearSize() {
		return underWearSize;
	}


	public KvDictDto getPantiesSize() {
		return pantiesSize;
	}


	public KvDictDto getShoesSize() {
		return shoesSize;
	}


	public int getCrotchBefore() {
		return crotchBefore;
	}


	public int getCrotchAfter() {
		return crotchAfter;
	}


	public int getPantsLength() {
		return pantsLength;
	}


	public int getCoatLength() {
		return coatLength;
	}


	public int getSleeveLength() {
		return sleeveLength;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((coatDownSize == null) ? 0 : coatDownSize.hashCode());
		result = prime * result + coatLength;
		result = prime * result + ((coatUpSize == null) ? 0 : coatUpSize.hashCode());
		result = prime * result + crotchAfter;
		result = prime * result + crotchBefore;
		result = prime * result + ((pantiesSize == null) ? 0 : pantiesSize.hashCode());
		result = prime * result + pantsLength;
		result = prime * result + ((shoesSize == null) ? 0 : shoesSize.hashCode());
		result = prime * result + sleeveLength;
		result = prime * result + ((underWearSize == null) ? 0 : underWearSize.hashCode());
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
		CustomerClothesEntity other = (CustomerClothesEntity) obj;
		if (coatDownSize == null) {
			if (other.coatDownSize != null)
				return false;
		} else if (!coatDownSize.getValue().equals(other.coatDownSize.getValue()))
			return false;
		if (coatLength != other.coatLength)
			return false;
		if (coatUpSize == null) {
			if (other.coatUpSize != null)
				return false;
		} else if (!coatUpSize.getValue().equals(other.coatUpSize.getValue()))
			return false;
		if (crotchAfter != other.crotchAfter)
			return false;
		if (crotchBefore != other.crotchBefore)
			return false;
		if (pantiesSize == null) {
			if (other.pantiesSize != null)
				return false;
		} else if (!pantiesSize.getValue().equals(other.pantiesSize.getValue()))
			return false;
		if (pantsLength != other.pantsLength)
			return false;
		if (shoesSize == null) {
			if (other.shoesSize != null)
				return false;
		} else if (!shoesSize.getValue().equals(other.shoesSize.getValue()))
			return false;
		if (sleeveLength != other.sleeveLength)
			return false;
		if (underWearSize == null) {
			if (other.underWearSize != null)
				return false;
		} else if (!underWearSize.getValue().equals(other.underWearSize.getValue()))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "CustomerclothesEntity [coatUpSize=" + coatUpSize + ", coatDownSize=" + coatDownSize + ", underWearSize="
				+ underWearSize + ", pantiesSize=" + pantiesSize + ", shoesSize=" + shoesSize + ", crotchBefore="
				+ crotchBefore + ", crotchAfter=" + crotchAfter + ", pantsLength=" + pantsLength + ", coatLength="
				+ coatLength + ", sleeveLength=" + sleeveLength + "]";
	}
	
	
	
}
