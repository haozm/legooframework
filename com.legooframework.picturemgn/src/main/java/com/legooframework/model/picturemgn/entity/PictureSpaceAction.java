package com.legooframework.model.picturemgn.entity;

import java.util.Date;
import java.util.Objects;

import org.springframework.jdbc.core.RowMapper;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;

public class PictureSpaceAction extends BaseEntityAction<PictureSpaceDetailEntity>{
	
	protected PictureSpaceAction() {
		super(null);
		// TODO Auto-generated constructor stub
	}
	
	public void addPictureSpace(CrmOrganizationEntity org,Long space,String price,Date beginTime,Date endTime) {
		Objects.requireNonNull(org,"入参org不能为空");
		Objects.requireNonNull(space, "入参space不能为空");
		Objects.requireNonNull(beginTime,"入参beginTime不能为空");
		Objects.requireNonNull(endTime, "入参endTime不能为空");
//		PictureSpaceEntity picspace = null;
//		if(org.isStore()) {
//			picspace = PictureSpaceEntity.newStoreSpace(space, price, store, beginTime, endTime);
//		}else if(org.isDept()) {
//			picspace = PictureSpaceEntity.newDepartmentSpace(space, price, department, beginTime, endTime);
//		}else if(org.isCompany()) {
////			picspace = Picture
//		}
			
		
	}
	
	@Override
	protected RowMapper<PictureSpaceDetailEntity> getRowMapper() {
		// TODO Auto-generated method stub
		return null;
	}

}
