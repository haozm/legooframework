package com.legooframework.model.picturemgn.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;

public class PictureDTO {
	
	
	private String url;
	
	private String size;

	private String labelIds;
	
	private String description;

	public PictureDTO(String url, String size, String labelIds, String description) {
		super();
		this.url = url;
		this.size = size;
		this.labelIds = labelIds;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Long> getLabelIds() {
		List<String> list = Splitter.on(",").splitToList(labelIds);
		return list.stream().map(x -> Long.parseLong(x)).collect(Collectors.toList());
	}

	public void setLabelIds(String labelIds) {
		this.labelIds = labelIds;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Long getSize() {
		return Long.parseLong(size);
	}

	public void setSize(String size) {
		this.size = size;
	}
	
	
	
	
}
