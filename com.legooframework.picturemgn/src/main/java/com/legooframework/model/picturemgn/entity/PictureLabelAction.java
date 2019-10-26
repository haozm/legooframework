package com.legooframework.model.picturemgn.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.crmadapter.entity.CrmEmployeeEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;

public class PictureLabelAction extends BaseEntityAction<PictureLabelEntity> {

	private static Logger logger = LoggerFactory.getLogger(PictureLabelAction.class);

	protected PictureLabelAction() {
		super(null);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 检查store
	 * 
	 * @param store
	 *            待检查门店
	 */
	private void storeCheck(CrmStoreEntity store) {
		Objects.requireNonNull(store, "入参store不能为空");
	}

	/**
	 * 检验门店并转换为MAP
	 * 
	 * @param store
	 */
	private Map<String, Object> storeCheckAndToMap(CrmStoreEntity store) {
		storeCheck(store);
		Map<String, Object> params = Maps.newHashMap();
		params.put("storeId", store.getId());
		return params;
	}

	/**
	 * 检查公司是否合法
	 * 
	 * @param company
	 */
	private void companyCheck(CrmOrganizationEntity company) {
		Objects.requireNonNull(company, "入参company不能为空");
	}

	/**
	 * 检验公司并转换为MAP
	 * 
	 * @param company
	 * @return
	 */
	private Map<String, Object> companyCheckAndToMap(CrmOrganizationEntity company) {
		companyCheck(company);
		Map<String, Object> params = Maps.newHashMap();
		params.put("companyId", company.getId());
		return params;
	}

	/**
	 * 判断门店是否存在有相同名称的标签
	 * 
	 * @param store
	 * @param name
	 * @return
	 */
	private boolean existLabel(CrmStoreEntity store,CrmOrganizationEntity company, String name) {
		if (Strings.isNullOrEmpty(name))
			throw new IllegalArgumentException("入参name不能为空");
		Map<String, Object> params = Maps.newHashMap();
		if(null != store) params.putAll(storeCheckAndToMap(store));
		if(null != company) params.putAll(companyCheckAndToMap(company));
		params.put("name", name);
		String sql = getStatementFactory().getExecSql(getModelName(), "count_label_name", params);
		Integer result = getNamedParameterJdbcTemplate().queryForObject(sql, params, Integer.class);
		return result > 0 ? true : false;
	}

	/**
	 * 加载当前登录用户可使用标签
	 * 
	 * @param user
	 * @return
	 */
	public List<PictureLabelEntity> loadEnabledPictureLabel(CrmEmployeeEntity employee) {
		Objects.requireNonNull(employee, "入参employee不能为空");
		Map<String, Object> params = Maps.newHashMap();
		params.put("companyId", employee.getCompanyId());
		if (employee.getStoreId().isPresent())
			params.put("storeId", employee.getStoreId().get());
		return loadEnabledPictureLabel(params);
	}

	/**
	 * 加载门店可使用的标签
	 * 
	 * @param store
	 *            门店
	 * @return
	 */
	public List<PictureLabelEntity> loadEnabledPictureLabel(CrmStoreEntity store) {
		Objects.requireNonNull(store, "入参store不能为空");
		return loadEnabledPictureLabel(storeCheckAndToMap(store));
	}

	/**
	 * 加载公司可使用的标签
	 * 
	 * @param company
	 * @return
	 */
	public List<PictureLabelEntity> loadEnabledPictureLabel(CrmOrganizationEntity company) {
		Objects.requireNonNull(company, "入参company不能为空");
		return loadEnabledPictureLabel(companyCheckAndToMap(company));
	}

	/**
	 * 根据条件查询可使用标签
	 * 
	 * @param params
	 * @return
	 */
	private List<PictureLabelEntity> loadEnabledPictureLabel(Map<String, Object> params) {
		Optional<List<PictureLabelEntity>> labelsOpt = queryForEntities("query_labels", params, getRowMapper());
		if (!labelsOpt.isPresent())
			return Collections.EMPTY_LIST;
		// TODO SQL 语句未写
		return labelsOpt.get();
	}
	
	/**
	 * 根据标签ID集合加载门店下对应的图片标签
	 * @param store 门店 
	 * @param labelIds 标签ID集合
	 * @return
	 */
	public List<PictureLabelEntity> loadEnabledPictureLabels(CrmStoreEntity store,Collection<Long> labelIds){
		Map<String, Object> params = storeCheckAndToMap(store);
		if(CollectionUtils.isEmpty(labelIds)) return Collections.EMPTY_LIST;
		params.put("labelIds", labelIds);
		Optional<List<PictureLabelEntity>> resultsOpt = queryForEntities("load_label_by_ids", params, getRowMapper());
		if(!resultsOpt.isPresent()) return Collections.EMPTY_LIST;
		return resultsOpt.get();
	}
	
	/**
	 * 根据标签ID集合加载公司或门店下对应的图片标签
	 * @param company 公司
	 * @param store 门店
	 * @param labelIds 标签ID集合
	 * @return
	 */
	public List<PictureLabelEntity> loadEnabledPictureLabels(CrmOrganizationEntity company,CrmStoreEntity store,Collection<Long> labelIds){
		Map<String, Object> params = companyCheckAndToMap(company);
		params.putAll(storeCheckAndToMap(store));
		params.put("labelIds", labelIds);
		Optional<List<PictureLabelEntity>> resultsOpt = queryForEntities("load_label_by_ids", params, getRowMapper());
		if(!resultsOpt.isPresent()) return Collections.EMPTY_LIST;
		return resultsOpt.get();
	}
	/**
	 * 获取门店下，id为labelId的图片标签
	 * @param store门店
	 * @param labelId 标签ID
	 * @return
	 */
	public PictureLabelEntity loadEnabledPictureLabel(CrmStoreEntity store,Long labelId) {
		Objects.requireNonNull(labelId, "入参labelId不能为空");
		List<PictureLabelEntity> labels = loadEnabledPictureLabels(store,Lists.newArrayList(labelId));
		Preconditions.checkState(labels.isEmpty(), String.format("门店[%s]无ID[%s]对应的图片标签", store.getId(),labelId));
		Preconditions.checkState(labels.size() == 1 , String.format("门店[%s]存在[%s]条ID[%s]对应的图片标签", store.getId(),labels.size(),labelId));
		return labels.get(0);
	}
	
	/**
	 * 根据ID查询公司下的多个图片标签
	 * @param company 公司
	 * @param labelIds 标签ID集合
	 * @return
	 */
	public List<PictureLabelEntity> loadEnabledPictureLabels(CrmOrganizationEntity company,Collection<Long> labelIds) {
		Map<String, Object> params = companyCheckAndToMap(company);
		if(CollectionUtils.isEmpty(labelIds)) return Collections.EMPTY_LIST;
		params.put("labelIds", labelIds);
		Optional<List<PictureLabelEntity>> resultsOpt = queryForEntities("load_label_by_ids", params, getRowMapper());
		if(!resultsOpt.isPresent()) return Collections.EMPTY_LIST;
		return resultsOpt.get();
	}
	/**
	 * 获取门店下，id为labelId的图片标签
	 * @param store门店
	 * @param labelId 标签ID
	 * @return
	 */
	public PictureLabelEntity loadEnabledPictureLabel(CrmOrganizationEntity company,Long labelId) {
		Objects.requireNonNull(labelId, "入参labelId不能为空");
		List<PictureLabelEntity> labels = loadEnabledPictureLabels(company,Lists.newArrayList(labelId));
		Preconditions.checkState(!labels.isEmpty(), String.format("公司[%s]无ID[%s]对应的图片标签", company.getId(),labelId));
		Preconditions.checkState(labels.size() == 1 , String.format("公司[%s]存在[%s]条ID[%s]对应的图片标签", company.getId(),labels.size(),labelId));
		return labels.get(0);
	}
	/**
	 * 如果公司根图片标签不存在则创建
	 * @param company 公司
	 */
	private PictureLabelEntity addRootPictureLabelIfAbsent(CrmOrganizationEntity company) {
		Map<String, Object> params = companyCheckAndToMap(company);
		Optional<PictureLabelEntity> labelOpt = queryForEntity("query_root_label", params, getRowMapper());
		if(labelOpt.isPresent()) return labelOpt.get();
		PictureLabelEntity rootLabel = PictureLabelEntity.newRootPictureLabel(company);
		updateAction(rootLabel, "insert_label");
		return rootLabel;
	}
	/**
	 * 如果门店根图片标签不存在则创建
	 * @param store 门店
	 */
	private PictureLabelEntity addRootPictureLabelIfAbsent(CrmStoreEntity store) {
		Map<String, Object> params = storeCheckAndToMap(store);
		Optional<PictureLabelEntity> labelOpt = queryForEntity("query_root_label", params, getRowMapper());
		if(labelOpt.isPresent()) return labelOpt.get();
		PictureLabelEntity rootLabel = PictureLabelEntity.newRootPictureLabel(store);
		updateAction(rootLabel, "insert_label");
		return rootLabel;
	}
	
	/**
	 * 获取门店父标签
	 * @param store
	 * @param pid
	 * @return
	 */
	private PictureLabelEntity getParentLabel(CrmStoreEntity store, Long pid) {
		PictureLabelEntity parent = null;
		if(null == pid || PictureLabelEntity.isStoreRootLabel(pid)) {
			parent = addRootPictureLabelIfAbsent(store);
		}else {
			parent = loadEnabledPictureLabel(store, pid);
		}
		return parent;
	}
	
	/**
	 * 获取公司父标签
	 * @param company
	 * @param pid
	 * @return
	 */
	private PictureLabelEntity getParentLabel(CrmOrganizationEntity company, Long pid) {
		PictureLabelEntity parent = null;
		if(null == pid || PictureLabelEntity.isCompanyRootLabel(pid)) {
			parent = addRootPictureLabelIfAbsent(company);
		}else {
			parent = loadEnabledPictureLabel(company, pid);
		}
		return parent;
	}
	/**
	 * 添加门店永久有效的图片标签
	 * 
	 * @param store
	 *            门店
	 * @param name
	 *            名称
	 * @param desc
	 *            描述
	 * @return
	 */
	public Long addPictureLabel(CrmStoreEntity store, Long pid,String name, String desc) {
		Objects.requireNonNull(store, "入参store不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参name不能为空");
		Preconditions.checkState(!existLabel(store, null, name), String.format("门店[%s] 已存在名为[%s]的标签", store.getId(),name));
		PictureLabelEntity label = getParentLabel(store,pid).nextStorePictureLabel(name, desc);
		updateAction(label, "insert_label");
		return label.getId();
	}
	
	/**
	 * 添加门店有时间限制的图片标签
	 * @param store
	 * @param pid
	 * @param name
	 * @param desc
	 * @param minDate
	 * @param maxDate
	 * @return
	 */
	public Long addPictureLabel(CrmStoreEntity store, Long pid,String name, String desc,Date minDate,Date maxDate) {
		Objects.requireNonNull(store, "入参store不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参name不能为空");
		Preconditions.checkState(!existLabel(store, null, name), String.format("门店[%s] 已存在名为[%s]的标签", store.getId(),name));
		PictureLabelEntity label = getParentLabel(store,pid).nextStorePictureLabel(name, desc, minDate, maxDate);
		updateAction(label, "insert_label");
		return label.getId();
	}
	/**
	 * 添加公司图片标签
	 * 
	 * @param company
	 *            公司
	 * @param name
	 *            名称
	 * @param desc
	 *            描述
	 * @return
	 */
	public Long addPictureLabel(CrmOrganizationEntity company,Long pid, String name, String desc) {
		Objects.requireNonNull(company, "入参company不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参name不能为空");
		Preconditions.checkState(!existLabel(null, company, name), String.format("公司[%s] 已存在名为[%s]的标签", company.getId(),name));
		PictureLabelEntity label = getParentLabel(company,pid).nextCompanyPictureLabel(name, desc);
		updateAction(label, "insert_label");
		return label.getId();
	}
	/**
	 * 添加公司有时间限制的图片标签
	 * @param company
	 * @param pid
	 * @param name
	 * @param desc
	 * @param minDate
	 * @param maxDate
	 * @return
	 */
	public Long addPictureLabel(CrmOrganizationEntity company,Long pid, String name, String desc,Date minDate,Date maxDate) {
		Objects.requireNonNull(company, "入参company不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参name不能为空");
		Preconditions.checkState(!existLabel(null, company, name), String.format("公司[%s] 已存在名为[%s]的标签", company.getId(),name));
		PictureLabelEntity label = getParentLabel(company,pid).nextCompanyPictureLabel(name, desc, minDate, maxDate);
		updateAction(label, "insert_label");
		return label.getId();
	}

	/**
	 * 启用多个图片标签
	 * 
	 * @param ids
	 *            图片标签ID集合
	 */
	public void enablePictureLabels(Collection<Long> ids) {
		if (CollectionUtils.isEmpty(ids))
			return;
		// TODO
	}

	/**
	 * 禁用多个图片标签
	 * 
	 * @param ids
	 *            图片标签ID集合
	 */
	public void disablePictureLabels(Collection<Long> ids) {
		if (CollectionUtils.isEmpty(ids))
			return;
		// TODO
	}

	/**
	 * 移除公司或门店多个图片标签
	 * @param company
	 * @param store
	 * @param ids
	 */
	public void removePictureLabels(CrmOrganizationEntity company,CrmStoreEntity store,Collection<Long> ids) {
		if (CollectionUtils.isEmpty(ids))
			return;
		Map<String,Object> params = companyCheckAndToMap(company);
		params.putAll(storeCheckAndToMap(store));
		String sql = getStatementFactory().getExecSql("PictureLabelEntity", "delete_labels_by_ids", params);
		getNamedParameterJdbcTemplate().update(sql, params);
	}
	/**
	 * 移除门店多个图片标签
	 * 
	 * @param ids
	 *            图片标签集合
	 */
	public void removePictureLabels(CrmStoreEntity store,Collection<Long> ids) {
		if (CollectionUtils.isEmpty(ids))
			return;
		Map<String,Object> params = storeCheckAndToMap(store);
		params.put("labelIds", ids);
		String sql = getStatementFactory().getExecSql("PictureLabelEntity", "delete_labels_by_ids", params);
		getNamedParameterJdbcTemplate().update(sql, params);
	}
	
	/**
	 * 移除公式多个图片标签
	 * @param company
	 * @param ids
	 */
	public void removePictureLabels(CrmOrganizationEntity company,Collection<Long> ids) {
		if (CollectionUtils.isEmpty(ids))
			return;
		Map<String,Object> params = companyCheckAndToMap(company);
		params.put("labelIds", ids);
		String sql = getStatementFactory().getExecSql("PictureLabelEntity", "delete_labels_by_ids", params);
		getNamedParameterJdbcTemplate().update(sql, params);
	}

	@Override
	protected RowMapper<PictureLabelEntity> getRowMapper() {
		return new RowMapperImpl();
	}

	class RowMapperImpl implements RowMapper<PictureLabelEntity>{

		@Override
		public PictureLabelEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new PictureLabelEntity(rs.getLong("id"),rs);
		}
		
	}
}
