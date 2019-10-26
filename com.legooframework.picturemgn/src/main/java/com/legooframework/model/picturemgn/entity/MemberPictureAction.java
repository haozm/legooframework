package com.legooframework.model.picturemgn.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.crmadapter.entity.CrmEmployeeEntity;
import com.legooframework.model.crmadapter.entity.CrmMemberEntity;

public class MemberPictureAction extends BaseEntityAction<MemberPictureEntity>{

	protected MemberPictureAction() {
		super(null);
		// TODO Auto-generated constructor stub
	}
	
	//检验会员
	private void memberCheck(CrmMemberEntity member) {
		Objects.requireNonNull(member, "会员member不能为空");
	}
	
	//检验会员并转换为Map
	private Map<String,Object> memberCheckAndToMap(CrmMemberEntity member) {
		memberCheck(member);
		Map<String,Object> params = Maps.newHashMap();
		params.put("memberId", member.getId());
		params.put("storeId", member.getStoreId());
		params.put("companyId", member.getCompanyId());
		return params;
	}
	/**
	 * 导购给会员添加多个会员图片
	 * @param employee 导购
	 * @param member 会员
	 * @param pictures 图片集合
	 * @return
	 */
	public List<Long> addMemberPictures(CrmEmployeeEntity employee,CrmMemberEntity member,Collection<PictureEntity> pictures){
		Objects.requireNonNull(employee,"入参employee不能为空");
		memberCheck(member);
		if(CollectionUtils.isEmpty(pictures)) return Collections.EMPTY_LIST;
		List<MemberPictureEntity> members = pictures.stream().map(x -> {
			return new MemberPictureEntity(member,employee,x);
		}).collect(Collectors.toList());
		batchInsert("batch_insert_picture",pictures);
		batchInsert("batch_insert_member_picture", members);
		return null;
	}
	
	/**
	 * 移除会员多个会员图片
	 * @param member 会员
	 * @param pictureIds 会员图片集合
	 */
	public void removeMemberPictures(CrmMemberEntity member,Collection<String> pictureIds) {
		if(CollectionUtils.isEmpty(pictureIds)) return ;
		Map<String, Object> params = memberCheckAndToMap(member);
		params.put("pictureIds", pictureIds);
		updateAction("remove_pictures", params);
		updateAction("remove_member_pictures", params);
	}
	
	/**
	 * 查询会员指点图片ID的会员图片
	 * @param member
	 * @param pictureId
	 * @return
	 */
	public Optional<MemberPictureEntity> findMemberPicture(CrmMemberEntity member,String pictureId){
		Objects.requireNonNull(member,"入参member不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(pictureId), "入参ID不能为空");
		List<MemberPictureEntity> pictures = loadMemberPictures(member, Lists.newArrayList(pictureId));
		if(pictures.size() != 1) return Optional.empty();
		return Optional.of(pictures.get(0));
	}
	
	/**
	 * 查询会员指点图片ID的会员图片，如无，报错
	 * @param member
	 * @param pictureId
	 * @return
	 */
	public MemberPictureEntity loadMemberPicture(CrmMemberEntity member,String pictureId){
		Objects.requireNonNull(member,"入参member不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(pictureId), "入参ID不能为空");
		Optional<MemberPictureEntity> pictureOpt = findMemberPicture(member, pictureId);
		Preconditions.checkState(pictureOpt.isPresent(), String.format("不存在会员[%s]对应的图片[%s]", member.getId(),pictureId));
		return pictureOpt.get();
	}
	
	/**
	 * 根据会员加载该会员的图片
	 * @param member
	 */
	public List<MemberPictureEntity> loadMemberPictures(CrmMemberEntity member) {
		Map<String, Object> params = memberCheckAndToMap(member);
		Optional<List<MemberPictureEntity>> mpicturesOpt = queryForEntities("query_member_pictures", params, getRowMapper());
		if(!mpicturesOpt.isPresent()) return Collections.EMPTY_LIST;
		return mpicturesOpt.get();
	}
	
	/**
	 * 查询会员的指定图片ID的会员图片
	 * @param member
	 * @param pictureIds
	 * @return
	 */
	public List<MemberPictureEntity> loadMemberPictures(CrmMemberEntity member,Collection<String> pictureIds){
		if(CollectionUtils.isEmpty(pictureIds)) return Collections.EMPTY_LIST;
		Map<String, Object> params = memberCheckAndToMap(member);
		params.put("pictureIds", pictureIds);
		Optional<List<MemberPictureEntity>> mpicturesOpt = queryForEntities("query_member_pictures", params, getRowMapper());
		if(!mpicturesOpt.isPresent()) return Collections.EMPTY_LIST;
		return mpicturesOpt.get();
	}
	/**
	 * 修改会员图片描述
	 * @param member 会员
	 * @param pictureId 图片ID
	 * @param decription 描述
	 */
	public void modifyDecription(CrmMemberEntity member,String pictureId,String description) {
		MemberPictureEntity picture = loadMemberPicture(member, pictureId);
		if(picture.isSameDescription(description)) return ;
		MemberPictureEntity changer = picture.modifyDescription(description);
		updateAction(changer, "update_picture_description");
	}
	
	/**
	 * 添加多个会员图片标签
	 * @param member
	 * @param pictureIds
	 * @param label
	 */
	public void addLabels(CrmMemberEntity member,String pictureId,Collection<PictureLabelEntity> labels) {
		MemberPictureEntity picture = loadMemberPicture(member, pictureId);
		if(picture.hasLabels(labels)) return ;
		picture.addLabels(labels);
		updateAction(picture, "update_picture_labels");
	}
	
	/**
	 * 移除多个会员图片标签
	 * @param member
	 * @param pictureId
	 * @param labels
	 */
	public void removeLabels(CrmMemberEntity member,String pictureId,Collection<PictureLabelEntity> labels) {
		MemberPictureEntity picture = loadMemberPicture(member, pictureId);
		picture.removeLabels(labels);
		updateAction(picture, "update_picture_labels");
	}
	
	@Override
	protected RowMapper<MemberPictureEntity> getRowMapper() {
		return new RowMapperImpl();
	}

	class RowMapperImpl implements RowMapper<MemberPictureEntity>{

		@Override
		public MemberPictureEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new MemberPictureEntity(rs);
		}
		
	}
}
