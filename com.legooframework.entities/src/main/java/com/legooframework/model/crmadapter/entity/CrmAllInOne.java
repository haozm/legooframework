package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.Map;

public class CrmAllInOne {

    private final String companyName;
    private final String storeName;
    private final String employeeName;
    private final boolean memberInfo;
    private final List<Map<String, Object>> memberMaps;

    public CrmAllInOne(CrmOrganizationEntity company, CrmStoreEntity store, CrmEmployeeEntity employee,
                       List<CrmMemberEntity> members) {
        this.companyName = company.getName();
        this.storeName = store.getName();
        this.memberInfo = CollectionUtils.isNotEmpty(members);
        this.employeeName = employee == null ? "店长" : employee.getUserName();
        if (this.memberInfo) {
            this.memberMaps = Lists.newArrayList();
            members.forEach(m -> {
                Map<String, Object> _m = Maps.newHashMap();
                _m.put("memberId", m.getId());
                _m.put("memberName", m.getName());
                _m.put("phoneNo", m.getPhoneNo().orElse("137"));
                this.memberMaps.add(_m);
            });
        } else {
            this.memberMaps = null;
        }
    }

    public Map<String, String> toStoreMap() {
        Map<String, String> params = Maps.newHashMap();
        params.put("公司名称", companyName);
        params.put("门店名称", storeName);
        return params;
    }

    public List<Map<String, String>> toMemberMaps() {
        List<Map<String, String>> maps = Lists.newArrayList();
        Preconditions.checkState(memberInfo);
        this.memberMaps.forEach(x -> {
            Map<String, String> _item = Maps.newHashMap();
            _item.put("公司名称", companyName);
            _item.put("门店名称", storeName);
            _item.put("导购姓名", employeeName);
            _item.put("会员姓名", MapUtils.getString(x, "memberName"));
            _item.put("phoneNo", MapUtils.getString(x, "phoneNo"));
            _item.put("memberId", MapUtils.getString(x, "memberId"));
            maps.add(_item);
        });
        return maps;
    }
}
