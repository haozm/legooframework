package com.legooframework.model.picturemgn.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmEmployeeEntity;
import com.legooframework.model.crmadapter.entity.CrmMemberEntity;

public class MemberPictureEntity extends BaseEntity<Long> implements BatchSetter{
	
	//会员ID
	private final Integer memberId;
	//上传的导购ID
	private final Integer uploadEmpId;
	//上传时间
	private final Date uploadTime;
	//门店ID
	private final Integer storeId;
	//公司ID
	private final Integer companyId;
	//图片
	private PictureEntity picture;

	public MemberPictureEntity(CrmMemberEntity member, CrmEmployeeEntity employee, String url, Long size, String description) {
		super(null);
		this.memberId = member.getId();
		this.uploadEmpId = employee.getId();
		this.uploadTime = new Date();
		this.storeId = member.getStoreId();
		this.companyId = member.getCompanyId();
		this.picture = new PictureEntity(url, size, description);
	}
	
	public MemberPictureEntity(CrmMemberEntity member, CrmEmployeeEntity employee,PictureEntity picture) {
		super(null);
		this.memberId = member.getId();
		this.uploadEmpId = employee.getId();
		this.uploadTime = new Date();
		this.storeId = member.getStoreId();
		this.companyId = member.getCompanyId();
		this.picture = picture;
	}
	
	public MemberPictureEntity(ResultSet res) {
		super(null,res);
		try {
			this.memberId = ResultSetUtil.getObject(res, "memberId", Integer.class);
			this.uploadEmpId = ResultSetUtil.getObject(res, "uploadEmp", Integer.class);
			this.uploadTime = res.getTimestamp("uploadTime");
			this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
			this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
			String pictureId = ResultSetUtil.getString(res, "pictureId");
			this.picture = new PictureEntity(pictureId, res);
		} catch (SQLException e) {
			throw new RuntimeException("Restore MemberPictureEntity has SQLException", e);
		}
	}
	
	@Override
	public Map<String, Object> toParamMap(String... excludes) {
		Map<String, Object> paramMap = this.picture.toParamMap(excludes);
		paramMap.put("memberId", this.memberId);
		paramMap.put("uploadEmp", this.uploadEmpId);
		paramMap.put("uploadTime", this.uploadTime);
		paramMap.put("storeId", this.storeId);
		paramMap.putAll(super.toParamMap(excludes));
		return paramMap;
	}

	public Integer getMemberId() {
		return memberId;
	}

	public Integer getUploadEmpId() {
		return uploadEmpId;
	}

	public Date getUploadTime() {
		return uploadTime;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public PictureEntity getPicture() {
		return picture;
	}
	
	//判断标签是否已存在
	public boolean hasLabels(Collection<PictureLabelEntity> labels) {
		return this.picture.hasLabels(labels);
	}
	
	//添加标签
	public void addLabel(PictureLabelEntity label) {
		this.picture.addLabel(label);
	}
	
	//添加多个标签
	public void addLabels(Collection<PictureLabelEntity> labels) {
		this.picture.addLabels(labels);
	}
	
	//移除标签
	public void removeLabel(PictureLabelEntity label) {
		this.picture.removeLabel(label);
	}
	
	//移除多个标签
	public void removeLabels(Collection<PictureLabelEntity> labels) {
		this.picture.removeLabels(labels);
	}
	
	//判断描述是否相同
	public boolean isSameDescription(String description) {
		return this.picture.isSameDescription(description);
	}
	
	//修改图片描述
	public MemberPictureEntity modifyDescription(String description) {
		MemberPictureEntity clone = (MemberPictureEntity) this.cloneMe();
		PictureEntity clonePicture = clone.picture.modifyDescription(description);
		clone.picture = clonePicture;
		return clone;
	}

	@Override
	public String toString() {
		return "MemberPictureEntity [memberId=" + memberId + ", uploadEmpId=" + uploadEmpId + ", uploadTime="
				+ uploadTime + ", storeId=" + storeId + ", companyId=" + companyId + ", picture=" + picture + "]";
	}

	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		ps.setObject(1, this.memberId);
		ps.setObject(2, this.picture.getId());
		ps.setObject(3,this.uploadEmpId);
		ps.setObject(4,this.storeId);
		ps.setObject(5,this.companyId);
	}
	
	
}
