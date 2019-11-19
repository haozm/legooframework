package com.legooframework.model.smsgateway.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.TreeNodeDto;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.Objects;

public class RechargeTreeDto extends TreeNodeDto {

    public RechargeTreeDto(OrgEntity entity) {
        super(entity.getId(), null, entity.getName());
        Map<String, Object> map = Maps.newHashMap();
        map.put("nodeDesc", entity.getName());
        map.put("type", 0);
        map.put("rawId", entity.getId());
        map.put("hasStores", false);
        map.put("storeIds", null);
        map.put("root", true);
        setAttachData(map);
    }

    public RechargeTreeDto(RechargeBalanceEntity entity, Object pid) {
        super(entity.getId(), pid, entity.getGroupName());
        Map<String, Object> map = Maps.newHashMap();
        map.put("nodeDesc", entity.getGroupName());
        map.put("type", 1);
        map.put("rawId", entity.getId());
        map.put("hasStores", true);
        map.put("storeIds", entity.getStoreIds());
        map.put("root", false);
        setAttachData(map);
    }

    public RechargeTreeDto(StoEntity entity, Object pid) {
        super(entity.getId(), pid, entity.getName());
        Map<String, Object> map = Maps.newHashMap();
        map.put("type", 2);
        map.put("rawId", entity.getId());
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

}
