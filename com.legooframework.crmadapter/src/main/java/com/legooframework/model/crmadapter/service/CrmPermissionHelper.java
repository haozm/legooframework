package com.legooframework.model.crmadapter.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.Authenticationor;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrmPermissionHelper extends BundleService {

    public CrmStoreEntity loadStoreInfo(Map<String, String> requestBody, HttpServletRequest request) {
        LoginContext user = LoginContextHolder.get();
        Integer companyId = user.getTenantId().intValue();
        Optional<CrmOrganizationEntity> companyOpt = getOrgAction().findCompanyById(companyId);
        Preconditions.checkState(companyOpt.isPresent(), "不存在Id=%s 对应的公司...", companyId);
        Integer storeId = null;
        if (user.isStoreManager() || user.isShoppingGuide()) {
            storeId = user.getStoreId();
        } else {
            storeId = MapUtils.getInteger(requestBody, "storeId", -1);
        }
        Preconditions.checkState(storeId != -1, "ID=%s 对应的门店不存在...", storeId);
        Optional<CrmStoreEntity> store = getStoreAction().findById(companyOpt.get(), storeId);
        Preconditions.checkState(store.isPresent(), "缺失ID=%s 对应的门店定义...", storeId);
        return store.get();
    }

    /**
     * 鉴权操作
     *
     * @param user nihao
     * @return NCNC
     */
    public CrmOrganizationEntity loadCompanyByUser(LoginContext user) {
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class)
                .findCompanyById(user.getTenantId().intValue());
        Preconditions.checkState(company.isPresent(), "用户%s 对应的公司 %s 对应的公司不存在...", user.getLoginName(),
                user.getTenantId().intValue());
        return company.get();
    }

    /**
     * 鉴权操作
     *
     * @param requestBody 门店ID  当前为店长或者导购时 可忽略
     * @return request
     */
    public Authenticationor authentication(Map<String, Object> requestBody) {
        LoginContext user = LoginContextHolder.get();
        Integer companyId = user.getTenantId().intValue();
        Optional<CrmOrganizationEntity> companyOpt = getOrgAction().findCompanyById(companyId);
        Preconditions.checkState(companyOpt.isPresent(), "不存在Id=%s 对应的公司...", companyId);
        if (user.isStoreManager() || user.isShoppingGuide()) {
            Integer storeId = user.getStoreId();
            Optional<CrmStoreEntity> store = getStoreAction().findById(companyOpt.get(), storeId);
            Preconditions.checkState(store.isPresent(), "缺失ID=%s 对应的门店定义...", storeId);
            return new Authenticationor(user, companyOpt.get(), store.get());
        }
        Integer storeId = MapUtils.getInteger(requestBody, "storeId", -1);
        String storeIds_str = MapUtils.getString(requestBody, "storeIds", null);
        if (storeId != -1) {
            Preconditions.checkState(user.getStoreIds().isPresent() && user.getStoreIds().get().contains(storeId),
                    "当前登陆用户%s无此门店查看权限...", user.getLoginName());
            Optional<CrmStoreEntity> store = getStoreAction().findById(companyOpt.get(), storeId);
            Preconditions.checkState(store.isPresent(), "缺失ID=%s 对应的门店定义...", storeId);
            return new Authenticationor(user, companyOpt.get(), store.get());
        } else if (Strings.isNullOrEmpty(storeIds_str)) {
            return new Authenticationor(user, companyOpt.get(), user.getStoreIds().orElse(null));
        } else {
            String[] storeIds = StringUtils.split(storeIds_str, ',');
            List<Integer> storeId_list = Stream.of(storeIds).map(Integer::new).collect(Collectors.toList());
            Preconditions.checkState(user.getStoreIds().isPresent() && user.getStoreIds().get().containsAll(storeId_list),
                    "鉴权失败,用户越权访问门店信息....");
            return new Authenticationor(user, companyOpt.get(), storeId_list);
        }
    }

}
