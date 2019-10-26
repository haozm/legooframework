package com.legooframework.model.crmadapter.entity;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CrmStoreEntityAction extends BaseEntityAction<CrmStoreEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CrmStoreEntityAction.class);

    public CrmStoreEntityAction() {
        super("CrmAdapterCache");
    }

    /**
     * 获取指定组织下的 所有门店信息
     *
     * @param organization 组织
     * @return
     */
    public Optional<List<CrmStoreEntity>> loadStoresByOrg(CrmOrganizationEntity organization) {
        List<CrmStoreEntity> stores = loadAllByCompany(organization);
        if (CollectionUtils.isEmpty(stores)) return Optional.empty();
        String code = organization.getCode();
        String code_ = String.format("%s_", organization.getCode());
        List<CrmStoreEntity> sub_list = stores.stream()
                .filter(s -> s.getOrgCode().equals(code) || s.getOrgCode().startsWith(code_))
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    public Optional<List<CrmStoreEntity>> findByIds(CrmOrganizationEntity company, Collection<Integer> storeIds) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(storeIds), "Collection<Integer> storeIds 不可以为空...");
        List<CrmStoreEntity> stores = loadAllByCompany(company);
        List<CrmStoreEntity> list = stores.stream().filter(x -> storeIds.contains(x.getId()))
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

    public Optional<CrmStoreEntity> findById(CrmOrganizationEntity company, Integer storeId) {
        Preconditions.checkNotNull(storeId, "Integer storeId 不可以为空...");
        List<CrmStoreEntity> stores = loadAllByCompany(company);
        return stores.stream().filter(x -> x.getId().equals(storeId)).findFirst();
    }

    public Optional<CrmStoreEntity> findByIdWitRest(CrmOrganizationEntity company, Integer storeId) {
        Preconditions.checkNotNull(storeId, "Integer storeId 不可以为空...");
        List<CrmStoreEntity> stores = loadAllByCompanyWithRest(company);
        return stores.stream().filter(x -> x.getId().equals(storeId)).findFirst();
    }

    @SuppressWarnings("unchecked")
    public List<CrmStoreEntity> loadAllByCompanyWithRest(CrmOrganizationEntity company) {
        Preconditions.checkNotNull(company, "公司不可以为空...");
        final String cache_key = String.format("%s_company_%s", getModelName(), company.getId());
        if (getCache().isPresent()) {
            List<CrmStoreEntity> entities = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(entities)) {
                if (logger.isDebugEnabled())
                    logger.debug("loadAllByCompany(store::%s) from cache by %s", company.getId(), cache_key);
                return entities;
            }
        }
        Optional<JsonElement> payload = super.postWithRest(company.getId(), "crmbase.loadAllStores", null, company.getId());
        if (!payload.isPresent()) return null;
        List<CrmStoreEntity> stores = Lists.newArrayList();
        JsonArray jsonArray = payload.get().getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject _json = jsonElement.getAsJsonObject();
            stores.add(new CrmStoreEntity(_json.get("id").getAsInt(),
                    _json.get("name").getAsString(), _json.get("orgCode").getAsString(),
                    _json.get("orgId").getAsInt(), _json.get("companyId").getAsInt(),
                    _json.get("companyName").getAsString()));
        }
        if (getCache().isPresent() && CollectionUtils.isNotEmpty(stores))
            getCache().get().put(cache_key, stores);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByCompanyId(company:%s) has store size is %s .", company.getId(),
                    stores.size()));
        return stores;
    }

    @SuppressWarnings("unchecked")
    public List<CrmStoreEntity> loadAllByCompany(CrmOrganizationEntity company) {
        Preconditions.checkNotNull(company, "公司不可以为空...");
        final String cache_key = String.format("%s_company_%s", getModelName(), company.getId());
        if (getCache().isPresent()) {
            List<CrmStoreEntity> entities = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(entities)) {
                if (logger.isDebugEnabled())
                    logger.debug("loadAllByCompany(store::%s) from cache by %s", company.getId(), cache_key);
                return entities;
            }
        }
        Optional<JsonElement> payload = super.post(company.getId(), "crmbase.loadAllStores", null, company.getId());
        if (!payload.isPresent()) return null;
        List<CrmStoreEntity> stores = Lists.newArrayList();
        JsonArray jsonArray = payload.get().getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject _json = jsonElement.getAsJsonObject();
            stores.add(new CrmStoreEntity(_json.get("id").getAsInt(),
                    _json.get("name").getAsString(), _json.get("orgCode").getAsString(),
                    _json.get("orgId").getAsInt(), _json.get("companyId").getAsInt(),
                    _json.get("companyName").getAsString()));
        }
        if (getCache().isPresent() && CollectionUtils.isNotEmpty(stores))
            getCache().get().put(cache_key, stores);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByCompanyId(company:%s) has store size is %s .", company.getId(),
                    stores.size()));
        return stores;
    }

}
