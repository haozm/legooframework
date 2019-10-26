package com.legooframework.model.upload.entity;

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.upload.util.AESUtil;
import com.legooframework.model.upload.util.PathHelper;
import com.legooframework.model.upload.util.Templates;

public class FilePath {

	private String key;

	private final String namespace;

	private final String domain;
	
	private String nameShort;
	
	public FilePath(String key, String namespace, String domain,String nameShort) {
		super();
		Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "入参key不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(namespace), "入参namespace不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(domain), "入参domain不能为空");
		this.key = key;
		this.namespace = namespace;
		this.domain = domain;
		this.nameShort = nameShort;
	}

	public FilePath execute(Map<String, Object> dataModel) {
		if (dataModel == null)
			return this;
		Handler handler = new Handler(key, namespace, dataModel);
		return new FilePath(handler.getKey(), handler.getNamespace(),domain,nameShort);

	}

	class Handler {

		private String key;

		private String namespace;
		
		private Map<String, Object> dataModel;

		Handler(String key, String namespace,Map<String, Object> dataModel) {
			this.dataModel = dataModel;
			this.dataModel.putAll(SystemVariable.toMap());
			this.key = PathHelper.replaceBlank((key.replaceAll("#", "")));
			this.namespace = PathHelper.replaceBlank((namespace.replaceAll("#", "")));
		}

		private String parse(String str) {
			if (this.dataModel.isEmpty())
				return str;
			List<String> vars = PathHelper.getPathVars(str);
			if (vars.isEmpty())
				return str;
			Preconditions.checkState(this.dataModel.keySet().containsAll(vars),
					String.format("定义路径参数%s与传递的参数%s不一致", vars, dataModel.keySet()));
			return Templates.execute(Templates.newTemplate(str), dataModel);
		}

		public String getKey() {
			return parse(this.key);
		}

		public String getNamespace() {
			return parse(this.namespace);
		}
		
		

	}

	public String getKey() {
		return key;
	}

	public String getEncryptKey(String fileName) {
		String key = String.format("%s/%s/%s", Strings.isNullOrEmpty(this.nameShort)?"default":this.nameShort,AESUtil.encrypt(this.key), fileName);
		return key;
	}

	public String getEncryptKey() {
		String key = String.format("%s/%s/", this.nameShort,AESUtil.encrypt(this.key));
		return key;
	}

	public String getEncryptUrl(String fileName) {
		return String.format("%s%s", this.domain, fileName);
	}

	public String getEncryptUrl() {
		return String.format("%s%s", this.domain, getEncryptKey());
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getDomain() {
		return domain.trim();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
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
		FilePath other = (FilePath) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
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
		return "FilePath [key=" + key + ", namespace=" + namespace + ", domain=" + domain + "]";
	}

}
