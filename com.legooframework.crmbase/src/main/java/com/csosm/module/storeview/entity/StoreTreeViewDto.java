package com.csosm.module.storeview.entity;

import com.csosm.commons.entity.TreeNodeDto;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StoreTreeViewDto extends TreeNodeDto {

    StoreTreeViewDto(StoreViewEntity entity) {
        super(entity.getId(), entity.getParentId().orNull(), entity.getNodeName());
        Map<String, Object> map = Maps.newHashMap();
        map.put("nodeDesc", entity.getNodeDesc());
        map.put("type", 1);
        map.put("rawId", entity.getId());
        map.put("hasStores", entity.hasStores());
        map.put("storeIds", entity.hasStores() ? entity.getStoreIds() : null);
        map.put("root", entity.isRoot());
        setAttachData(map);
    }

    StoreTreeViewDto(StoreEntity entity) {
        super(entity.getId(), null, entity.getName());
        Map<String, Object> map = Maps.newHashMap();
        map.put("type", 2);
        map.put("rawId", entity.getId());
        map.put("oldStoreId", entity.getOldStoreId());
        map.put("hasStores", false);
        map.put("root", false);
        setAttachData(map);
    }

    public boolean isRoot() {
        return MapUtils.getBoolean(getAttachData(), "root");
    }

    @Override
    public boolean isMyChild(TreeNodeDto child) {
        return !Objects.equals(child.getId(), child.getPid()) && super.isMyChild(child);
    }

    boolean hasStores() {
        return MapUtils.getBooleanValue(getAttachData(), "hasStores");
    }

    @SuppressWarnings("unchecked")
    public void addStoreNode(List<StoreEntity> stores) {
        if (CollectionUtils.isEmpty(stores) || !hasStores()) return;
        Collection<Integer> storeIds = (Collection<Integer>) MapUtils.getObject(getAttachData(), "storeIds");
        stores.forEach(st -> {
            if (storeIds.contains(st.getId())) addChildNotCheck(new StoreTreeViewDto(st));
        });
    }
}
