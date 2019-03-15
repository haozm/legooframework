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

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CrmOrganizationEntityAction extends BaseEntityAction<CrmOrganizationEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CrmOrganizationEntityAction.class);

    public CrmOrganizationEntityAction() {
        super("CrmAdapterCache");
    }

    public Optional<CrmOrganizationEntity> findCompanyById(Integer companyId) {
        final String cache_key = String.format("%s_com_%s", getModelName(), companyId);
        if (getCache().isPresent()) {
            CrmOrganizationEntity company = getCache().get().get(cache_key, CrmOrganizationEntity.class);
            if (company != null) return Optional.of(company);
        }
        Optional<JsonElement> payload = super.post(getTenantsRouteFactory().getUrl(companyId, "loadAllCompany"),
                null, -1);
        if (!payload.isPresent()) return Optional.empty();
        List<CrmOrganizationEntity> companies = Lists.newArrayList();
        JsonArray jsonArray = payload.get().getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject _json = jsonElement.getAsJsonObject();
            companies.add(new CrmOrganizationEntity(_json.get("id").getAsInt(),
                    _json.get("id").getAsInt(), _json.get("code").getAsString(), 1,
                    _json.get("name").getAsString(), _json.get("shortName").getAsString()));
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllCompany(%s) res is %s", companyId, companies));

        Optional<CrmOrganizationEntity> company = companies.stream().filter(x -> x.getId().equals(companyId))
                .findFirst();
        company.ifPresent(com -> getCache().ifPresent(c -> c.put(cache_key, com)));
        return company;
    }

    public Optional<CrmOrganizationEntity> loadOrganizationById(Integer companyId, Integer orgId) {
        List<CrmOrganizationEntity> organizations = loadOrganizations(companyId);
        return organizations.stream().filter(x -> x.getId().equals(orgId)).findFirst();
    }

    public Optional<CrmOrganizationEntity> loadOrganization(CrmOrganizationEntity company, Integer orgId) {
        List<CrmOrganizationEntity> organizations = loadOrganizations(company.getId());
        return organizations.stream().filter(x -> x.getId().equals(orgId)).findFirst();
    }

    public List<CrmOrganizationEntity> loadSubOrganizations(Integer companyId, Integer orgId) {
        List<CrmOrganizationEntity> organizations = loadOrganizations(companyId);
        Optional<CrmOrganizationEntity> sub_org = organizations.stream().filter(x -> x.getId().equals(orgId)).findFirst();
        Preconditions.checkState(sub_org.isPresent(), "ID=%s 对应的组织不存在...");
        List<CrmOrganizationEntity> sub_orgs = Lists.newArrayList();
        sub_orgs.add(sub_org.get());
        organizations.forEach(x -> {
            if (x.isSubOrg(sub_org.get())) sub_orgs.add(x);
        });
        return sub_orgs;
    }

    @SuppressWarnings("unchecked")
    List<CrmOrganizationEntity> loadOrganizations(Integer companyId) {
        final String cache_key = String.format("%s_orgs_%s", getModelName(), companyId);
        if (getCache().isPresent()) {
            List<CrmOrganizationEntity> organizations = getCache().get().get(cache_key, List.class);
            if (!CollectionUtils.isEmpty(organizations)) {
                if (logger.isDebugEnabled())
                    logger.debug("loadOrganizations() from cache by %s,size is %s", cache_key, organizations.size());
                return organizations;
            }
        }

        Optional<JsonElement> payload = super.post(getTenantsRouteFactory().getUrl(companyId, "loadAllOrg"),
                null, companyId);
        if (!payload.isPresent()) return null;
        List<CrmOrganizationEntity> organizations = Lists.newArrayList();
        JsonArray jsonArray = payload.get().getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject _json = jsonElement.getAsJsonObject();
            organizations.add(new CrmOrganizationEntity(_json.get("id").getAsInt(),
                    _json.get("companyId").getAsInt(), _json.get("code").getAsString(), 2,
                    _json.get("name").getAsString(),
                    _json.get("shortName").isJsonNull() ? null : _json.get("shortName").getAsString()));
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadOrganizations(%s) res is %s", companyId, organizations.size()));
        if (CollectionUtils.isNotEmpty(organizations))
            getCache().ifPresent(c -> c.put(cache_key, organizations));
        return organizations;
    }

}
