package com.legooframework.model.templatemgs.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginUser;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.core.web.TreeNode;
import com.legooframework.model.core.web.TreeUtil;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.templatemgs.entity.HolidayEntity;
import com.legooframework.model.templatemgs.entity.HolidayEntityAction;

public class HolidayService extends BundleService {

	public List<HolidayEntity> loadHolidays(LoginContext loginUser) {
		Optional<CrmOrganizationEntity> companyOpt = getBean(CrmOrganizationEntityAction.class)
				.findCompanyById(loginUser.getTenantId().intValue());
		Preconditions.checkArgument(companyOpt.isPresent(), "当前登录用户无公司信息");
		if (loginUser.isManager())
			return getBean(HolidayEntityAction.class).loadSystemHolidays();
		if (loginUser.isAreaManagerRole()|| loginUser.isBoss() || loginUser.isRegediter()) {
			return getBean(HolidayEntityAction.class).loadCompanyHolidays(companyOpt.get());
		}
		if (loginUser.isStoreManager()) {
			Optional<CrmStoreEntity> storeOpt = getBean(CrmStoreEntityAction.class).findById(companyOpt.get(),
					loginUser.getStoreId());
			Preconditions.checkArgument(storeOpt.isPresent(), "当前登录用户无门店信息");
			return getBean(HolidayEntityAction.class).loadStoreHolidays(storeOpt.get());
		}
		return Collections.EMPTY_LIST;
	}

	public List<TreeNode> loadTreeNode(LoginContext user) {
		List<HolidayEntity> holidays = loadHolidays(user);
		return holidays.stream().map(x -> {
			return new TreeNode(String.valueOf(x.getId()),"2000",x.getName(),null);
		}).collect(Collectors.toList());
	}

}
