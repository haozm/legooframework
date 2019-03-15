package com.csosm.module.base.entity;

import com.csosm.commons.entity.TreeNodeDto;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 系统默认组织架构树+实体店 建模
 */

public class OrgTreeViewDto extends TreeNodeDto {

    OrgTreeViewDto(OrganizationEntity org) {
        super(String.format("org_%s", org.getId()), String.format("org_%s", org.getParentId()), org.getName());
        Map<String, Object> attachData = Maps.newHashMap();
        attachData.put("rawId", org.getId());
        attachData.put("rawPid", org.getParentId());
        attachData.put("type", 1);
        attachData.put("level", org.getLevel());
        attachData.put("code", Splitter.on("_").splitToList(org.getCode()));
        setAttachData(attachData);
    }
    
    
    public int orgLevel() {
        Preconditions.checkState(isOrg());
        return MapUtils.getIntValue(getAttachData(), "level");
    }


    public Optional<OrgTreeViewDto> findOrgByRawId(Object id) {
        if (isStore()) return Optional.absent();
        if (Objects.equal(getAttachData().get("rawId"), id)) return Optional.of(this);
        if (!CollectionUtils.isEmpty(getChildren())) {
            for (TreeNodeDto $it : getChildren()) {
                Optional<OrgTreeViewDto> _res = ((OrgTreeViewDto) $it).findOrgByRawId(id);
                if (_res.isPresent()) return _res;
            }
        }
        return Optional.absent();
    }

    public Optional<OrgTreeViewDto> findByRawId(Object id) {
        if (Objects.equal(getAttachData().get("rawId"), id)) return Optional.of(this);
        if (!CollectionUtils.isEmpty(getChildren())) {
            for (TreeNodeDto $it : getChildren()) {
                Optional<OrgTreeViewDto> _res = ((OrgTreeViewDto) $it).findOrgByRawId(id);
                if (_res.isPresent()) return _res;
            }
        }
        return Optional.absent();
    }

    public Optional<List<OrgTreeViewDto>> loadDirectSubNode() {
        if (CollectionUtils.isEmpty(getChildren())) return Optional.absent();
        List<OrgTreeViewDto> _list = Lists.newArrayListWithCapacity(getChildren().size());
        for (TreeNodeDto $it : getChildren()) _list.add((OrgTreeViewDto) $it);
        return Optional.of(_list);
    }

    public boolean isOrg() {
        return 1 == MapUtils.getIntValue(getAttachData(), "type", -1);
    }

    public boolean isStore() {
        return 2 == MapUtils.getIntValue(getAttachData(), "type", -1);
    }
    
    public boolean isEmployee() {
    	return 3 == MapUtils.getIntValue(getAttachData(), "type", -1);
    }
    
    OrgTreeViewDto(StoreEntity store) {
        super(String.format("str_%s", store.getId()),
                String.format("org_%s", store.getOrganizationId().orNull()), store.getName());
        Map<String, Object> attachData = Maps.newHashMap();
        attachData.put("rawId", store.getId());
        attachData.put("rawPid", store.getOrganizationId().orNull());
        attachData.put("type", 2);
        attachData.put("oldStoreId", store.getOldStoreId().orNull());
        setAttachData(attachData);
    }
    
    OrgTreeViewDto(EmployeeEntity employee){
    	super(String.format("emp_%s", employee.getId()),
    			String.format("org_%s", employee.getStoreId().isPresent()?employee.getStoreId().get():
    				employee.getOrganizationId().isPresent()?employee.getOrganizationId().get():
    					employee.getCompanyId().isPresent()?employee.getCompanyId().get():""),employee.getUserName());
    	Map<String,Object> attachData = Maps.newHashMap();
    	 attachData.put("rawId", employee.getId());
         attachData.put("rawPid", employee.getStoreId().isPresent()?employee.getStoreId().get():
				employee.getOrganizationId().isPresent()?employee.getOrganizationId().get():
					employee.getCompanyId().isPresent()?employee.getCompanyId().get():"");
         attachData.put("type", 3);
         attachData.put("empId", employee.getId());
         attachData.put("empName", employee.getUserName());
         attachData.put("companyId", employee.getCompanyId().isPresent()?employee.getCompanyId().get():"");
         attachData.put("orgId", employee.getOrganizationId().isPresent()?employee.getOrganizationId().get():"");
         attachData.put("storeId", employee.getStoreId().isPresent()?employee.getStoreId().get():"");
         attachData.put("roleIds", employee.getRoleIds().isPresent()?Joiner.on(",").join(employee.getRoleIds().get()):"");
         setAttachData(attachData);
    }
    
}
