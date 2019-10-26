package com.legooframework.model.picturemgn.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;

public class PictureEntity extends BaseEntity<String> implements BatchSetter{
	
	private final static String THUMBNAIL_FORMAT = "";
	//完整图片地址
	private final String url;
	//图片缩略图
	private final String thumbnailUrl;
	//图片大小
	private final Long size;
	//图片标签
	private final Set<Long> labelIds = Sets.newHashSet();
	//图片描述
	private String description;
	//图片是否已删除
	private boolean deleteFlag = false;
	//图片是否已回收
	private boolean recoveryFlag = false;
	
	public PictureEntity(String url, Long size, String description) {
		super(UUID.randomUUID().toString().replaceAll("-", ""));
		this.url = url;
		this.thumbnailUrl = String.format("%s?%s", url,THUMBNAIL_FORMAT);
		this.size = size;
		this.description = description;
	}
	
	public PictureEntity(String id,ResultSet res) {
		super(id,res);
		try {
		this.url = ResultSetUtil.getString(res, "url");
		this.thumbnailUrl = ResultSetUtil.getString(res, "thumbnailUrl");
		this.size = ResultSetUtil.getObject(res, "size", Long.class);
		this.description = ResultSetUtil.getString(res, "description");
		List<String> list = Splitter.on(",").splitToList(ResultSetUtil.getString(res, "labelIds"));
		this.labelIds.addAll(list.stream().map(x -> Long.parseLong(x)).collect(Collectors.toSet()));
		}catch (SQLException e) {
			throw new RuntimeException("Restore PictureEntity has SQLException", e);
		}		
	}
	
	
	
	@Override
	public Map<String, Object> toParamMap(String... excludes) {
		Map<String, Object> paramMap = super.toParamMap(excludes);
		paramMap.put("pictureId", this.getId());
		paramMap.put("url", this.url);
		paramMap.put("thumbnailUrl", this.thumbnailUrl);
		paramMap.put("size", this.size);
		paramMap.put("description", this.description);
		String labelIds = Joiner.on(",").join(this.labelIds);
		paramMap.put("labelIds",labelIds);
		return paramMap;
	}


	public String getUrl() {
		return url;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public Long getSize() {
		return size;
	}

	public String getDescription() {
		return description;
	}
	
	//判断标签是否已存在
	public boolean hasLabels(Collection<PictureLabelEntity> labels) {
		Set<Long> labelIds = labels.stream().map(x -> x.getId()).collect(Collectors.toSet());
		if(this.labelIds.containsAll(labelIds)) return true;
		return false;
	}
	
	//添加标签
	public void addLabel(PictureLabelEntity label) {
		if(null == label) return ;
		this.labelIds.add(label.getId());
	}
	
	//移除标签
	public void removeLabel(PictureLabelEntity label) {
		if(null == label) return ;
		if(!this.labelIds.contains(label.getId())) return ;
		this.labelIds.remove(label.getId());
	}
	
	//添加多个标签
	public void addLabels(Collection<PictureLabelEntity> labels) {
		if(CollectionUtils.isEmpty(labels)) return ;
		labels.stream().forEach(x -> this.labelIds.add(x.getId()));
	}
	
	//移除多个标签
	public void removeLabels(Collection<PictureLabelEntity> labels) {
		if(CollectionUtils.isEmpty(labels)) return ;
		Set<Long> labelIds = labels.stream().map(x -> x.getId()).collect(Collectors.toSet());
		this.labelIds.removeAll(labelIds);
	}
	
	//判断描述是否相同
	public boolean isSameDescription(String description) {
		return Objects.equals(this.description, description);
	}
	
	//修改标签
	public PictureEntity modifyDescription(String description) {
		PictureEntity clone = (PictureEntity) this.cloneMe();
		clone.description = description;
		return clone;
	}

	@Override
	public String toString() {
		return "PictureEntity [url=" + url + ", thumbnailUrl=" + thumbnailUrl + ", size=" + size + ", labelIds="
				+ labelIds + ", description=" + description + ", deleteFlag=" + deleteFlag + ", recoveryFlag="
				+ recoveryFlag + "]";
	}

	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		ps.setObject(1, this.getId());
		ps.setObject(2, this.url);
		ps.setObject(3, this.thumbnailUrl);
		ps.setObject(4, this.size);
		ps.setObject(5, this.description);
		String labels = Joiner.on(",").join(labelIds);
		ps.setObject(6, labels);
	}
	
}
