package com.legooframework.model.upload.entity;

import java.util.List;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class NamespaceEntity{
	
	private Integer companyId;
	
	private String companyName;

	private String serverUrl = "http://upload-z0.qiniup.com/";
	
	private String domain;
	
	private String namespace;
	
	//公司短称
	private String nameShort;
	
	private final List<String> channels = Lists.newArrayList();
	
	

	public NamespaceEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	NamespaceEntity(Integer companyId, String companyName,String nameShort, String domain, String namespace, List<String> channels) {
		this.companyId = companyId;
		this.domain = domain;
		this.namespace = namespace;
		this.channels.addAll(channels);
		this.nameShort = nameShort;
		this.companyName = companyName;
	}

	NamespaceEntity(Integer companyId,String companyName,String nameShort,String domain, List<ChannelEntity> channels, String namespace) {
		this.companyId = companyId;
		this.domain = domain;
		this.namespace = namespace;
		channels.forEach(x -> this.channels.add(x.getId()));
		this.nameShort = nameShort;
		this.companyName = companyName;
	}
	
	public boolean modify(String domain,String namespace) {
		boolean modify = false;
		if(!Objects.equals(this.domain, domain)) {
			this.domain = domain;
			modify = true;
		}
		if(!Objects.equals(this.namespace, namespace)) {
			this.namespace = namespace;
			modify = true;
		}
		return modify;
	}
	
	public Integer getCompanyId() {
		return this.companyId;
	}

	public String getDomain() {
		return domain;
	}
	
	public String getHttpDomain() {
		return String.format("http://%s/", this.domain);
	}
	
	public String getNamespace() {
		return namespace;
	}

	public List<String> getChannels() {
		return channels;
	}
	
	public String getServerUrl() {
		return this.serverUrl;
	}

	
	public String getNameShort() {
		return nameShort;
	}

	protected Map<String, Object> toMap() {
		Map<String,Object> map = Maps.newHashMap();
		map.put("companyId", this.getCompanyId());
		map.put("domain", this.getDomain());
		map.put("namespace", this.namespace);
		map.put("channelIds", Joiner.on(",").join(this.channels));
		map.put("nameShort", this.nameShort);
		map.put("companyName", this.companyName);
		return map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((channels == null) ? 0 : channels.hashCode());
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
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
		NamespaceEntity other = (NamespaceEntity) obj;
		if (channels == null) {
			if (other.channels != null)
				return false;
		} else if (!channels.equals(other.channels))
			return false;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamespaceEntity [companyId=" + companyId + ", companyName=" + companyName + ", serverUrl=" + serverUrl
				+ ", domain=" + domain + ", namespace=" + namespace + ", nameShort=" + nameShort + ", channels="
				+ channels + "]";
	}

	
	
	
}
