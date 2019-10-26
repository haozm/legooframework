package com.legooframework.model.crmadapter.entity;

import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class UploadInformationAction extends BaseEntityAction<CrmOrganizationEntity> {

    protected UploadInformationAction() {
        super(null);
    }

    public Map<String, Object> getUploadInfo(Integer companyId) {
        final String cache_key = String.format("%s_com_%s", getModelName(), companyId);
        if (getCache().isPresent()) {
            Map<String, Object> result = getCache().get().get(cache_key, Map.class);
            return result;
        }
        Optional<JsonElement> payload = super.post(companyId, "loadUploadInfo", null, companyId);
        if (payload.isPresent()) {
            Gson gson = new Gson();
            Map<String, Object> infoMap = gson.fromJson(payload.get().getAsJsonObject(), Map.class);
            if (getCache().isPresent()) getCache().get().put(cache_key, infoMap);
            return infoMap;
        }
        return null;
    }
}
