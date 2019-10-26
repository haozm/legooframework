package com.legooframework.model.picturemgn.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.crmadapter.entity.CrmEmployeeEntity;
import com.legooframework.model.crmadapter.entity.CrmEmployeeEntityAction;
import com.legooframework.model.crmadapter.entity.CrmMemberEntity;
import com.legooframework.model.crmadapter.entity.CrmMemberEntityAction;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.picturemgn.entity.PictureLabelEntity;
import com.legooframework.model.picturemgn.dto.PictureDTO;
import com.legooframework.model.picturemgn.entity.MemberPictureAction;
import com.legooframework.model.picturemgn.entity.MemberPictureEntity;
import com.legooframework.model.picturemgn.entity.PictureEntity;
import com.legooframework.model.picturemgn.entity.PictureLabelAction;

public class PictureMgnService extends BaseService {

	@Override
	protected Bundle getLocalBundle() {
		// TODO Auto-generated method stub
		return null;
	}

	// 获取存在的公司，如不存在，报错
	private CrmOrganizationEntity getExistCompany(Integer companyId) {
		Optional<CrmOrganizationEntity> companyOpt = getBean(CrmOrganizationEntityAction.class)
				.findCompanyById(companyId);
		Preconditions.checkState(companyOpt.isPresent(), String.format("公司[%s]不存在", companyId));
		return companyOpt.get();
	}

	private CrmStoreEntity getExistStore(CrmOrganizationEntity company, Integer storeId) {
		Optional<CrmStoreEntity> storeOpt = getBean(CrmStoreEntityAction.class).findById(company, storeId);
		Preconditions.checkArgument(storeOpt.isPresent(), String.format("门店[%s]不存在", storeOpt.get()));
		return storeOpt.get();
	}

	/**
	 * 添加标签
	 * 
	 * @param storeId
	 * @param companyId
	 * @param pid
	 * @param name
	 * @param desc
	 * @param minDate
	 * @param maxDate
	 * @return
	 */
	public Long addPictureLabel(Integer storeId, Integer companyId, Long pid, String name, String desc, Date minDate,
			Date maxDate) {
		Objects.requireNonNull(companyId, "入参companyId不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参name不能为空");
		CrmOrganizationEntity company = getExistCompany(companyId);
		if (null != storeId) {
			CrmStoreEntity store = getExistStore(company, storeId);
			if (null != minDate && null != maxDate)
				return getBean(PictureLabelAction.class).addPictureLabel(store, pid, name, desc, minDate, maxDate);
			return getBean(PictureLabelAction.class).addPictureLabel(store, pid, name, desc);
		}

		if (null != minDate && null != maxDate)
			return getBean(PictureLabelAction.class).addPictureLabel(company, pid, name, desc, minDate, maxDate);
		return getBean(PictureLabelAction.class).addPictureLabel(company, pid, name, desc);
	}

	/**
	 * 移除门店或公司标签， 当storeId不为null时,companyId不为null时，移除门店标签，
	 * 当storeId为null,companyId不为null时移除公司标签
	 * 
	 * @param storeId
	 * @param companyId
	 * @param labelIds
	 */
	public void removePictureLabels(Integer storeId, Integer companyId, Collection<Long> labelIds) {
		Objects.requireNonNull(companyId, "入参companyId不能为空");
		if (CollectionUtils.isEmpty(labelIds))
			return;
		CrmOrganizationEntity company = getExistCompany(companyId);
		if (null != storeId) {
			CrmStoreEntity store = getExistStore(company, storeId);
			getBean(PictureLabelAction.class).removePictureLabels(store, labelIds);
			return;
		}
		getBean(PictureLabelAction.class).removePictureLabels(company, labelIds);
	}

	/**
	 * 加载门店或公司标签， 当storeId不为null时,companyId不为null时，移除门店标签，
	 * 当storeId为null,companyId不为null时移除公司标签
	 * 
	 * @param storeId
	 * @param companyId
	 * @param labelIds
	 */
	public List<PictureLabelEntity> loadPictureLabels(Integer storeId, Integer companyId, Collection<Long> labelIds) {
		Objects.requireNonNull(companyId, "入参companyId不能为空");
		if (CollectionUtils.isEmpty(labelIds))
			return Collections.EMPTY_LIST;
		CrmOrganizationEntity company = getExistCompany(companyId);
		CrmStoreEntity store = getExistStore(company, storeId);
		return getBean(PictureLabelAction.class).loadEnabledPictureLabels(company,store, labelIds);
	}

	/**
	 * 上传会员图片
	 * 
	 * @param companyId
	 * @param storeId
	 * @param memberId
	 * @param employeeId
	 * @param pictureDtos
	 */
	public void uploadMemberPictures(Integer companyId, Integer storeId, Integer memberId, Integer employeeId,
			List<PictureDTO> pictureDtos) {
		Objects.requireNonNull(memberId, "入参memberId不能为空");
		Objects.requireNonNull(employeeId, "入参employeeId不能为空");
		if (CollectionUtils.isEmpty(pictureDtos))
			return;
		Set<Long> labelIds = Sets.newHashSet();
		pictureDtos.stream().forEach(x -> labelIds.addAll(x.getLabelIds()));
		List<PictureLabelEntity> labels = loadPictureLabels(storeId, companyId, labelIds);
		Map<Long, PictureLabelEntity> labelMap = Maps.newHashMap();
		labels.stream().forEach(x -> labelMap.put(x.getId(), x));
		List<PictureEntity> pictures = Lists.newArrayListWithCapacity(pictureDtos.size());
		pictureDtos.stream().forEach(x -> {
			PictureEntity picture = new PictureEntity(x.getUrl(), x.getSize(), x.getDescription());
			x.getLabelIds().stream().forEach(y -> {
				picture.addLabel(labelMap.get(y));
				pictures.add(picture);
			});
		});
		CrmOrganizationEntity company = getExistCompany(companyId);
		Optional<CrmEmployeeEntity> employeeOpt = getBean(CrmEmployeeEntityAction.class).findById(company, employeeId);
		Preconditions.checkState(employeeOpt.isPresent(), String.format("公司[%s]无导购[%s]信息", companyId, employeeId));

		Optional<CrmMemberEntity> memberOpt = getBean(CrmMemberEntityAction.class).loadMemberByCompany(company,
				memberId);
		Preconditions.checkState(memberOpt.isPresent(), String.format("公司[%s]无会员[%s]信息", companyId, memberId));

		getBean(MemberPictureAction.class).addMemberPictures(employeeOpt.get(), memberOpt.get(), pictures);
	}
	
	//获取并检测已存在的会员
	private CrmMemberEntity getAndCheckMember(Integer companyId,Integer memberId) {
		Objects.requireNonNull(companyId,"入参companyId不能为空");
		Objects.requireNonNull(memberId, "入参memberId不能为空");
		CrmOrganizationEntity company = getExistCompany(companyId);
		return getExistMember(company, memberId);
	}
	
	//获取已存在的会员
	private CrmMemberEntity getExistMember(CrmOrganizationEntity company,Integer memberId) {
		Optional<CrmMemberEntity> memberOpt = getBean(CrmMemberEntityAction.class).loadMemberByCompany(company,
				memberId);
		Preconditions.checkState(memberOpt.isPresent(), String.format("公司[%s]无会员[%s]信息", company.getId(), memberId));
		return memberOpt.get();
	}
	/**
	 * 根据会员ID加载该会员的所有图片
	 * @param memberId
	 * @return
	 */
	public List<MemberPictureEntity> loadMemberPictures(Integer companyId,Integer memberId){
		return getBean(MemberPictureAction.class).loadMemberPictures(getAndCheckMember(companyId,memberId));
	}
	
	/**
	 * 移除会员的图片
	 * @param companyId
	 * @param memberId
	 * @param pictureIds
	 */
	public void removeMemberPictures(Integer companyId,Integer memberId,Collection<String> pictureIds) {
		if(CollectionUtils.isEmpty(pictureIds)) return;
		CrmMemberEntity member = getAndCheckMember(companyId,memberId);
		getBean(MemberPictureAction.class).removeMemberPictures(member, pictureIds);
	}
	
	/**
	 * 修改会员图片描述
	 * @param companyId
	 * @param memberId
	 * @param pictureId
	 * @param description
	 */
	public void modifyPictureDescription(Integer companyId,Integer memberId,String pictureId,String description) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(pictureId), "入参pictureId不能为空");
		CrmMemberEntity member = getAndCheckMember(companyId,memberId);
		getBean(MemberPictureAction.class).modifyDecription(member, pictureId, description);
	}
	
	/**
	 * 为图片添加多个标签
	 * @param companyId
	 * @param memberId
	 * @param pictureId
	 * @param labelIds
	 */
	public void addPictureLabels(Integer companyId,Integer storeId,Integer memberId,String pictureId,Collection<Long> labelIds) {
		if(CollectionUtils.isEmpty(labelIds)) return;
		Preconditions.checkArgument(!Strings.isNullOrEmpty(pictureId), "入参pictureId不能为空");
		CrmOrganizationEntity company = getExistCompany(companyId);
		CrmStoreEntity store = getExistStore(company, storeId);
		CrmMemberEntity member = getExistMember(company, memberId);
		List<PictureLabelEntity> labels = getBean(PictureLabelAction.class).loadEnabledPictureLabels(company, store, labelIds);
		getBean(MemberPictureAction.class).addLabels(member, pictureId, labels);
	}
	
	/**
	 * 移除图片多个标签
	 * @param compnayId
	 * @param storeId
	 * @param memberId
	 * @param pictureId
	 * @param labelIds
	 */
	public void removePictureLabels(Integer companyId,Integer storeId,Integer memberId,String pictureId,Collection<Long> labelIds) {
		if(CollectionUtils.isEmpty(labelIds)) return;
		Preconditions.checkArgument(!Strings.isNullOrEmpty(pictureId), "入参pictureId不能为空");
		CrmOrganizationEntity company = getExistCompany(companyId);
		CrmStoreEntity store = getExistStore(company, storeId);
		CrmMemberEntity member = getExistMember(company, memberId);
		List<PictureLabelEntity> labels = getBean(PictureLabelAction.class).loadEnabledPictureLabels(company, store, labelIds);
		getBean(MemberPictureAction.class).removeLabels(member, pictureId, labels);
	}
}
