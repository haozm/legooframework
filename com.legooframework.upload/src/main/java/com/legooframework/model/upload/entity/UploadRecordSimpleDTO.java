package com.legooframework.model.upload.entity;

import java.util.Map;

import com.google.common.collect.Maps;

public class UploadRecordSimpleDTO {

    private String domain;

    private String upToken;

    private String path;

    public UploadRecordSimpleDTO(String domain, String upToken, String path) {
        super();
        this.domain = domain;
        this.upToken = upToken;
        this.path = path;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUpToken() {
        return upToken;
    }

    public void setUpToken(String upToken) {
        this.upToken = upToken;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((upToken == null) ? 0 : upToken.hashCode());
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
        UploadRecordSimpleDTO other = (UploadRecordSimpleDTO) obj;
        if (domain == null) {
            if (other.domain != null)
                return false;
        } else if (!domain.equals(other.domain))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (upToken == null) {
            if (other.upToken != null)
                return false;
        } else if (!upToken.equals(other.upToken))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "UploadRecordSimpleDTO [domain=" + domain + ", upToken=" + upToken + ", path=" + path + "]";
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("domain", this.domain);
        data.put("upToken", this.upToken);
        data.put("path", this.path);
        return data;
    }

}
