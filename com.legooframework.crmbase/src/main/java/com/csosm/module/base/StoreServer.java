package com.csosm.module.base;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.OrganizationEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class StoreServer extends AbstractBaseServer {

	private static final Logger logger = LoggerFactory.getLogger(StoreServer.class);
	
	/**
	 * 新增门店
	 * @param loginUser
	 * @param parentId
	 * @param name
	 * @param phone
	 * @param type
	 * @param status
	 * @param address
	 */
	public void saveStore(LoginUserContext loginUser, Integer parentId, String name, String phone, Integer type,
			Integer state, String address) {
		Objects.requireNonNull(loginUser, "当前登录用户[loginUser]不能为空");
		Preconditions.checkState(loginUser.getRoleSet().hasAdminRole(), "登录用户无权限新增门店");
		Objects.requireNonNull(parentId, "父组织[parentId]不能为空");
		Optional<OrganizationEntity> companyOpt = loginUser.getCompany();
		Preconditions.checkState(companyOpt.isPresent(), "登录用户无公司信息");
		OrganizationEntity parentOrg = getBean(OrganizationEntityAction.class).loadById(parentId);
		getBean(StoreEntityAction.class).saveStore(loginUser,companyOpt.get(), parentOrg, name, phone, type, state, address);
	}
	/**
	 * 
	 * @param loginUser
	 * @param storeId
	 * @param name
	 * @param phone
	 * @param type
	 * @param status
	 * @param address
	 * @param hiddenPhone
	 */
	public void editStore(LoginUserContext loginUser, Integer storeId, String name, String phone, Integer type,
			Integer state, String address, Integer hiddenPhone) {
		Objects.requireNonNull(loginUser, "当前登录用户[loginUser]不能为空");
		Preconditions.checkState(!loginUser.getRoleSet().hasShoppingGuideRole(), "登录用户无权限编辑门店信息");
		Objects.requireNonNull(storeId,"门店[storeId]不能为空");
		StoreEntity store = getBean(StoreEntityAction.class).loadById(storeId);
		getBean(StoreEntityAction.class).editStore(loginUser,store, name, phone, type, state, address, hiddenPhone);
	}

	public void switchStore(LoginUserContext loginUser,Integer parentId,Integer storeId) {
		Objects.requireNonNull(loginUser, "当前登录用户[loginUser]不能为空");
		Preconditions.checkState(loginUser.getRoleSet().hasAdminRole(), "登录用户无权限新增门店");
		Objects.requireNonNull(parentId,"父组织[parentId]不能为空");
		Objects.requireNonNull(storeId,"门店[storeId]不能为空");
		OrganizationEntity parentOrg = getBean(OrganizationEntityAction.class).loadById(parentId);
		StoreEntity store = getBean(StoreEntityAction.class).loadById(storeId);
		getBean(StoreEntityAction.class).switchStore(parentOrg, store);
	}
}
