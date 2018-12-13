package com.legooframework.model.organization.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.dict.dto.KvDictDto;
import com.legooframework.model.dict.dto.KvTypeDictDto;
import com.legooframework.model.dict.event.DictEventFactory;
import com.legooframework.model.organization.entity.CompanyEntity;
import com.legooframework.model.organization.entity.CompanyEntityAction;
import com.legooframework.model.organization.entity.StoreEntity;
import com.legooframework.model.organization.entity.StoreEntityAction;
import com.legooframework.model.organization.event.OrgEventFactory;

import java.util.List;
import java.util.Optional;

public class CompanyService extends OrgService {

    // 公司注册门店
    public Long addStore(Long companyId, String storeCode, String fullName, String shortName,
                         String businessLicense, String detailAddress, String legalPerson,
                         String contactNumber, String remark, String storeType) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fullName), "门店全程不可以为空.");
        Preconditions.checkNotNull(companyId, "公司编码不可以为空.");
        LoginContextHolder.get();
        Optional<KvTypeDictDto> type_opt = getEventBus()
                .sendAndReceive(DictEventFactory.loadDictByTypeEvent(getLocalBundle(),
                        StoreEntity.TYPE_DICT), KvTypeDictDto.class);
        Preconditions.checkState(type_opt.isPresent(), "数据异常，门店状态 %s 对应的数据字典不存在.",
                StoreEntity.TYPE_DICT);
        Optional<KvDictDto> type_dict = type_opt.get().valueOf(storeType);
        Preconditions.checkState(type_dict.isPresent(), "非法的门店状态取值 %s", storeType);

        Optional<CompanyEntity> company = getBean(CompanyEntityAction.class).findById(companyId);
        Preconditions.checkArgument(company.isPresent(), "编码 %s 对应的公司不存在.", companyId);
        Long storeId = getBean(StoreEntityAction.class).insert(storeCode, fullName, shortName,
                businessLicense, detailAddress, legalPerson, contactNumber, remark, type_dict.get());
        StoreEntity store = getBean(StoreEntityAction.class).loadById(storeId);
        getBean(CompanyEntityAction.class).addStore(company.get().getId(), store);
        getEventBus().postEvent(OrgEventFactory.companyAddStoreEvent(store, company.get()));
        return storeId;
    }

    // 获取指定公司的名下全部门店
    public Optional<List<StoreEntity>> loadAllStoreByCompany(Long companyId) {
        Preconditions.checkNotNull(companyId, "公司编码ID不可以为空.");
        Optional<CompanyEntity> company = getBean(CompanyEntityAction.class).findById(companyId);
        Preconditions.checkArgument(company.isPresent(), "Id= %s 对应的公司不存在.", companyId);
        return getBean(StoreEntityAction.class).loadAllByCompany(company.get());
    }

}
