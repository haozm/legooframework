package com.legooframework.model.upload.entity;

import java.util.Map;
import com.google.common.collect.Maps;
import com.legooframework.model.upload.decoder.Decoder;
import com.legooframework.model.upload.decoder.DecoderFactory;

public class ChannelEntity {
	
	private String id;
	
	private final String name;

	private String fileType;

	private Decoder decoder;

	private String path;
	
	
	ChannelEntity(String id, String name, String fileType, Decoder decoder, String path) {
		this.id = id;
		this.name = name;
		this.fileType = fileType;
		this.decoder = decoder;
		this.path = path;
	}

	ChannelEntity(String id, String name, String path) {
		this.id = id;
		this.name = name;
		this.path = path;
	}
	
	public String getId() {
		return this.id;
	}

	public String getFileType() {
		return fileType;
	}

	public Decoder getDecoder() {
		return decoder;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	protected Map<String, Object> toMap() {
		Map<String,Object> map = Maps.newHashMap();
		map.put("id", this.id);
		map.put("name", this.name);
		map.put("fileType", this.fileType);
		map.put("path", this.path);
		map.put("decoder", DecoderFactory.getDecoderName(this.decoder));
		return map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((decoder == null) ? 0 : decoder.hashCode());
		result = prime * result + ((fileType == null) ? 0 : fileType.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChannelEntity other = (ChannelEntity) obj;
		if (decoder == null) {
			if (other.decoder != null)
				return false;
		} else if (!decoder.equals(other.decoder))
			return false;
		if (fileType == null) {
			if (other.fileType != null)
				return false;
		} else if (!fileType.equals(other.fileType))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChannelEntity [id=" + id + ", name=" + name + ", fileType=" + fileType + ", decoder=" + decoder
				+ ", path=" + path + "]";
	}
	

}
