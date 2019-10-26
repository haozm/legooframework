package com.legooframework.model.picturemgn.entity;

import java.util.Set;

import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;

public class PictureSpaceDetailEntity extends BaseEntity<Long>{
	
	private final PictureSpaceEntity space;
	
	private long useSpace = 0L;
	
	private final Integer storeId;
	
	private final Integer companyId;
	
	private final int type;

	public PictureSpaceDetailEntity(Long id, PictureSpaceEntity space, long useSpace, Integer storeId,
			Integer companyId, int type) {
		super(id);
		this.space = space;
		this.useSpace = useSpace;
		this.storeId = storeId;
		this.companyId = companyId;
		this.type = type;
	}
	
	//获取剩余多少空间
	public Long getSurplusSpace() {
		return this.space.getSpace() - this.useSpace;
	}
	
	//是否还有剩余空间
	public boolean hasSurplusSpace() {
		return getSurplusSpace() > 0;
	}
	
	//使用空间
	public boolean consumeSpace(Long space) {
		if(!hasSurplusSpace()) return false;
		if(getSurplusSpace() - space < 0) return false;
		this.useSpace = this.useSpace + space;
		return true;
	}
	
	//回收空间
	public void recoverySpace(Long space) {
		this.useSpace = this.useSpace - space;
	}
	
	
}
