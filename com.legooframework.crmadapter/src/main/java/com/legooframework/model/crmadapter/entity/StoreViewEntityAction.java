package com.legooframework.model.crmadapter.entity;


import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 门店任意虚拟分组 实体建模
 */
public class StoreViewEntityAction extends BaseEntityAction<StoreViewEntity> {

    private static final Logger logger = LoggerFactory.getLogger(StoreViewEntityAction.class);

    public StoreViewEntityAction() {
        super(null);
    }

    public List<StoreViewEntity> loadRecharStoreView(Integer companyId) {
//        final String cache_key = String.format("%s_orgs_%s", getModelName(), companyId);
//        if (getCache().isPresent()) {
//            List<CrmOrganizationEntity> organizations = getCache().get().get(cache_key, List.class);
//            if (!CollectionUtils.isEmpty(organizations)) {
//                if (logger.isDebugEnabled())
//                    logger.debug("loadOrganizations() from cache by %s,size is %s", cache_key, organizations.size());
//                return organizations;
//            }
//        }

        Optional<JsonElement> payload = super.post(companyId, "loadAllReChargeStoreView", null, companyId);
        if (!payload.isPresent()) return null;
        List<StoreViewEntity> storeViews = Lists.newArrayList();
        JsonArray jsonArray = payload.get().getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject _json = jsonElement.getAsJsonObject();
            String _storeIds = _json.get("sIds").getAsString();
            List<Integer> storeIds = Stream.of(StringUtils.split(_storeIds, ','))
                    .mapToInt(Integer::valueOf).boxed().collect(Collectors.toList());
            //String id, Integer treeType, String pid, String nodeName, List<Integer> storeIds, Integer companyId
            storeViews.add(new StoreViewEntity(_json.get("id").getAsString(), 2, _json.get("pId").getAsString(),
                    _json.get("name").getAsString(), storeIds, _json.get("").getAsInt()));
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadRecharStoreView(%s) res is %s", companyId, storeViews.size()));
        return storeViews;
    }


}
