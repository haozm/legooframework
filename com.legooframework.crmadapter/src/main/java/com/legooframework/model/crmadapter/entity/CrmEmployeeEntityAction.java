package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class CrmEmployeeEntityAction extends BaseEntityAction<CrmEmployeeEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CrmEmployeeEntityAction.class);

    public CrmEmployeeEntityAction() {
        super("CrmAdapterCache");
    }

    @SuppressWarnings("unchecked")
    public Optional<List<CrmEmployeeEntity>> loadAllByStore(CrmStoreEntity store) {
        Preconditions.checkNotNull(store, "所属门店不可以为空值...");
        final String cache_key = String.format("%s_bystore_%s_%s", getModelName(), store.getCompanyId(), store.getId());
        if (getCache().isPresent()) {
            List<CrmEmployeeEntity> employees = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(employees)) {
                if (logger.isDebugEnabled())
                    logger.debug(String.format("loadAllByStore(store::%s) from cache by %s", store.getName(), cache_key));
                return Optional.of(employees);
            }
        }

        Optional<JsonElement> payload = super.post(store.getCompanyId(), "crmbase.loadEmpsByStore", null, store.getCompanyId(),
                store.getId());

        Preconditions.checkState(payload.isPresent(), "门店 %s 无职员信息...", store.getName());
        List<CrmEmployeeEntity> emps = Lists.newArrayList();
        JsonArray jsonArray = payload.get().getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            emps.add(buildEmp(jsonElement.getAsJsonObject()));
        }
        if (getCache().isPresent() && CollectionUtils.isNotEmpty(emps))
            getCache().get().put(cache_key, emps);
        return Optional.ofNullable(CollectionUtils.isEmpty(emps) ? null : emps);
    }

    public Optional<CrmEmployeeEntity> findById(CrmOrganizationEntity company, Integer employeeId) {
        Preconditions.checkNotNull(company, "所属公司不可以为空值...");
        Preconditions.checkNotNull(employeeId, "employeeId 不可以为空值...");
        Optional<JsonElement> payload = post(company.getId(), "loadEmpsById", null, company.getId(), employeeId);
        Preconditions.checkState(payload.isPresent(), "职员id= %s 不存在...", employeeId);
        return Optional.of(buildEmp(payload.get().getAsJsonObject()));
    }

    public Optional<CrmEmployeeEntity> findByLoginName(CrmOrganizationEntity company, String loginName) {
        Preconditions.checkNotNull(company, "所属公司不可以为空值...");
        Preconditions.checkNotNull(loginName, "loginName 不可以为空值...");
        Optional<JsonElement> payload = post(company.getId(), "crmbase.loadEmpsByLoginName", null, company.getId(), loginName);
        Preconditions.checkState(payload.isPresent(), "职员 %s 不存在...", loginName);
        return Optional.of(buildEmp(payload.get().getAsJsonObject()));
    }

    private CrmEmployeeEntity buildEmp(JsonObject _json) {
        return new CrmEmployeeEntity(_json.get("id").getAsInt(),
                _json.get("cId").getAsInt(), _json.get("oId").getAsInt(), _json.get("sId").getAsInt(),
                _json.get("name").getAsString(),
                _json.get("roleIds").isJsonNull() ? null : _json.get("roleIds").getAsString());
    }

}
