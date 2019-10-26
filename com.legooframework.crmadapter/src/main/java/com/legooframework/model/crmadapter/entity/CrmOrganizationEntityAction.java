package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class CrmOrganizationEntityAction extends BaseEntityAction<CrmOrganizationEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CrmOrganizationEntityAction.class);

    public CrmOrganizationEntityAction() {
        super(null);
    }

    public Optional<List<CrmOrganizationEntity>> loadAllCompanys() {
        Optional<JsonElement> payload = super.post(null, "crmbase.loadAllCompany", null, -1);
        if (!payload.isPresent()) return Optional.empty();
        List<CrmOrganizationEntity> companies = decodingCompanys(payload.get().getAsJsonArray());
        List<CrmOrganizationEntity> sub_coms = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(companies)) {
            companies.forEach(com -> {
                if (ArrayUtils.contains(new Integer[]{999, 100098}, com.getId()))
                    sub_coms.add(com);
            });
        }
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_coms) ? null : sub_coms);
    }

    public Optional<CrmOrganizationEntity> findCompanyById(Integer companyId) {
        try {
            Optional<JsonElement> payload = super.post(companyId, "crmbase.loadAllCompany", null, -1);
            if (!payload.isPresent()) return Optional.empty();
            List<CrmOrganizationEntity> companies = decodingCompanys(payload.get().getAsJsonArray());
            if (logger.isTraceEnabled())
                logger.trace(String.format("loadAllCompany(%s) res is %s", companyId, companies));
            return companies.stream().filter(x -> x.getId().equals(companyId))
                    .findFirst();
        } catch (Exception e) {
            logger.error(String.format("load Company Info has error....companyId= %s", companyId), e);
            throw new RuntimeException(e);
        }
    }

    public Optional<CrmOrganizationEntity> findCompanyByIdWithRest(Integer companyId) {
        try {
            Optional<JsonElement> payload = super.postWithRest(companyId, "crmbase.loadAllCompany", null, -1);
            if (!payload.isPresent()) return Optional.empty();
            List<CrmOrganizationEntity> companies = decodingCompanys(payload.get().getAsJsonArray());
            if (logger.isTraceEnabled())
                logger.trace(String.format("loadAllCompany(%s) res is %s", companyId, companies));
            return companies.stream().filter(x -> x.getId().equals(companyId))
                    .findFirst();
        } catch (Exception e) {
            logger.error(String.format("load Company Info has error....companyId= %s", companyId), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析 公司信息
     *
     * @param jsonComs JSON数据
     * @return List
     */
    private List<CrmOrganizationEntity> decodingCompanys(JsonArray jsonComs) {
        List<CrmOrganizationEntity> companies = Lists.newArrayList();
        for (JsonElement jsonElement : jsonComs) {
            JsonObject _json = jsonElement.getAsJsonObject();
            companies.add(new CrmOrganizationEntity(_json.get("id").getAsInt(),
                    _json.get("id").getAsInt(), _json.get("code").getAsString(), 1,
                    _json.get("name").getAsString(), _json.get("shortName").getAsString()));
        }
        return companies;
    }

    public Optional<CrmOrganizationEntity> loadOrganizationById(Integer companyId, Integer orgId) {
        List<CrmOrganizationEntity> organizations = loadOrganizations(companyId);
        if (CollectionUtils.isEmpty(organizations)) return Optional.empty();
        return organizations.stream().filter(x -> x.getId().equals(orgId)).findFirst();
    }

    public Optional<CrmOrganizationEntity> loadOrganization(CrmOrganizationEntity company, Integer orgId) {
        List<CrmOrganizationEntity> organizations = loadOrganizations(company.getId());
        if (CollectionUtils.isEmpty(organizations)) return Optional.empty();
        return organizations.stream().filter(x -> x.getId().equals(orgId)).findFirst();
    }

    public List<CrmOrganizationEntity> loadSubOrganizations(Integer companyId, Integer orgId) {
        List<CrmOrganizationEntity> organizations = loadOrganizations(companyId);
        Preconditions.checkState(CollectionUtils.isNotEmpty(organizations), "ID=%s 无下级组织...", companyId);
        Optional<CrmOrganizationEntity> sub_org = organizations.stream().filter(x -> x.getId().equals(orgId)).findFirst();
        Preconditions.checkState(sub_org.isPresent(), "ID=%s 对应的组织不存在...");
        List<CrmOrganizationEntity> sub_orgs = Lists.newArrayList();
        sub_orgs.add(sub_org.get());
        organizations.forEach(x -> {
            if (x.isSubOrg(sub_org.get())) sub_orgs.add(x);
        });
        return sub_orgs;
    }

    private List<CrmOrganizationEntity> loadOrganizations(Integer companyId) {
        Optional<JsonElement> payload = super.post(companyId, "crmbase.loadAllOrg", null, companyId);
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
        return organizations;
    }

}
