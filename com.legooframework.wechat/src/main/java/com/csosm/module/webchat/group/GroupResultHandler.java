package com.csosm.module.webchat.group;

import com.csosm.module.base.entity.EmployeeEntity;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class GroupResultHandler {

    private GroupResultHandler() {
        throw new RuntimeException();
    }

    public static List<Map<String, Object>> createGuideRolesResult(List<GroupEmployeesDTO> GroupGuideDtos, Optional<List<EmployeeEntity>> empsOpt) {
        List<Map<String, Object>> retMaps = Lists.newArrayList();
        for (GroupEmployeesDTO group : GroupGuideDtos) {
            Map<String, Object> retMap = Maps.newHashMap();
            retMap.put("id", group.getGroupId());
            retMap.put("label", group.getGroupName());
            List<String> guideIds = group.getGuideIds();
            List<Map<String, Object>> views = Lists.newArrayListWithCapacity(empsOpt.get().size());
            if (empsOpt.isPresent()) {
                for (EmployeeEntity emp : empsOpt.get()) {
                    Map<String, Object> map = Maps.newHashMap();
                    if (!emp.isEnabled())
                        continue;
                    map.put("id", emp.getId());
                    map.put("name", emp.getUserName());
                    if (guideIds.contains(emp.getId().toString())) {
                        map.put("ischeck", true);
                    } else {
                        map.put("ischeck", false);
                    }
                    views.add(map);
                }
            }
            retMap.put("items", views);
            retMaps.add(retMap);
        }
        return retMaps;
    }
}
