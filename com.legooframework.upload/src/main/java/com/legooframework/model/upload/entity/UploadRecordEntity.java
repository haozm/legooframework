package com.legooframework.model.upload.entity;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;
/**
 * update 22:00
 *
 * @author bear
 */
public class UploadRecordEntity {

	private String id;

	private String orginKey;

	private String orginName;

	private String orginPath;

	private String qiniuKey;

	private String qiniuDomain;

	private String qiniuNamespace;

	private String qiniuPath;

	private String upToken;

	private Date createTime;

	public UploadRecordEntity(String orginKey, String orginName, String orginPath, String qiniuKey, String qiniuDomain,
			String qiniuNamespace, String qiniuPath, String upToken) {
		this.orginKey = orginKey;
		this.orginName = orginName;
		this.orginPath = orginPath;
		this.qiniuKey = qiniuKey;
		this.qiniuDomain = qiniuDomain;
		this.qiniuNamespace = qiniuNamespace;
		this.qiniuPath = qiniuPath;
		this.upToken = upToken;
	}

	public String getId() {
		return id;
	}

	public String getOrginKey() {
		return orginKey;
	}

	public String getOrginName() {
		return orginName;
	}

	public String getOrginPath() {
		return orginPath;
	}

	public String getQiniuKey() {
		return qiniuKey;
	}

	public String getQiniuDomain() {
		return qiniuDomain;
	}

	public String getQiniuNamespace() {
		return qiniuNamespace;
	}

	public String getQiniuPath() {
		return qiniuPath;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public String getUpToken() {
		return this.upToken;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = Maps.newHashMap();
		map.put("id", this.id);
		map.put("orginKey", this.orginKey);
		map.put("orginName", this.orginName);
		map.put("orginPath", this.orginPath);
		map.put("qiniuKey", this.qiniuKey);
		map.put("qiniuDomain", this.qiniuDomain);
		map.put("qiniuNamespace", this.qiniuNamespace);
		map.put("qiniuPath", this.qiniuPath);
		map.put("upToken", this.upToken);
		return map;
	}

}
