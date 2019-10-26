package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CrmMemberEntityAction extends BaseEntityAction<CrmMemberEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CrmMemberEntityAction.class);

    public CrmMemberEntityAction() {
        super(null);
    }

    /**
     * @param store mendain
     * @return lishi
     */
    public Optional<List<CrmMemberEntity>> loadAllByStore(CrmStoreEntity store) {
        Preconditions.checkNotNull(store, "会员所属门店不可以为空...");
        Optional<JsonElement> payload = super.post(store.getCompanyId(), "crmbase.loadMembersByStore", null, "bystore",
                store.getCompanyId(), store.getId());
        if (!payload.isPresent()) return Optional.empty();
        List<CrmMemberEntity> members = Lists.newArrayList();
        JsonArray jsonArray = payload.get().getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            members.add(build(jsonElement.getAsJsonObject()));
        }
        return Optional.of(members);
    }

    private CrmMemberEntity build(JsonObject _json) {
        String mobile = _json.get("ph").getAsString();
        String sId = _json.get("sId").getAsString();
        int gId = _json.get("gId").getAsInt();
        return new CrmMemberEntity(_json.get("id").getAsInt(),
                _json.get("nm").getAsString(),
                StringUtils.equals("NULL", mobile) ? null : mobile,
                _json.get("bdt").getAsInt(),
                _json.get("bd").getAsString(),
                _json.get("lbd").getAsString(),
                _json.get("cId").getAsInt(),
                _json.get("cnm").getAsString(),
                StringUtils.equals("-1", sId) ? null : Integer.valueOf(sId),
                _json.get("snm").getAsString(),
                gId == -1 ? null : gId,
                gId == -1 ? null : _json.get("gnm").getAsString());
    }

    public Optional<List<CrmMemberEntity>> loadByCompany(CrmOrganizationEntity company, Collection<Integer> memberIds) {
        Preconditions.checkNotNull(company);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(memberIds), "入参 memberIds 不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("memberIds", Joiner.on(',').join(memberIds));
        Optional<JsonElement> payload = super.post(company.getId(), "crmbase.loadMembersByIds", params, "loadMembersByIds",
                company.getId());
        if (!payload.isPresent()) return Optional.empty();
        List<CrmMemberEntity> members = Lists.newArrayList();
        JsonArray jsonArray = payload.get().getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            members.add(build(jsonElement.getAsJsonObject()));
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByCompany(%s) size is %s", company.getId(), members.size()));
        return Optional.of(members);
    }

    public Optional<CrmMemberEntity> loadMemberByCompany(CrmOrganizationEntity company, Integer memberId) {
        if (memberId == null || memberId <= 0) return Optional.empty();
        Preconditions.checkNotNull(company);
        Optional<List<CrmMemberEntity>> members = loadByCompany(company, Sets.newHashSet(memberId));
        return members.map(crmMemberEntities -> crmMemberEntities.get(0));
    }

}
