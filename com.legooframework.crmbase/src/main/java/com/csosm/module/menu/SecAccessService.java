package com.csosm.module.menu;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.RoleEntityAction;
import com.csosm.module.base.entity.RoleSet;
import com.csosm.module.menu.entity.ResEntity;
import com.csosm.module.menu.entity.ResourceDto;
import com.csosm.module.menu.entity.ResourcesFactory;
import com.google.common.base.Preconditions;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

// 安全模块访问相关服务
public class SecAccessService extends AbstractBaseServer {

    // 获取指定一批角色对应的资源数
    public Optional<ResourceDto> loadResByAccount(LoginUserContext loginUser) {
        Preconditions.checkNotNull(loginUser, "登陆账户信息不可以为空值....");
        Preconditions.checkState(loginUser.getCompany().isPresent(), "登陆用户缺少公司信息....");
        RoleSet roleSet = loginUser.getRoleSet();
        // long companyId = loginUser.getCompany().get().getId().longValue();
        if (roleSet.hasAdminRole()) {
            return getBean(ResourcesFactory.class).getAllReource(1L);
        } else if (roleSet.hasDBARole()) {
            return getBean(ResourcesFactory.class).getAllReource(-1L);
        }
        Set<String> resourIds = roleSet.getAllResources();
        if (CollectionUtils.isEmpty(resourIds)) return Optional.empty();
        return getBean(ResourcesFactory.class).getSubReource(1L, resourIds);
    }

    /**
     * @param roleId      角色ID
     * @param resourceIds 资源集合
     */
    public void authorized(OrganizationEntity company, Integer roleId, String... resourceIds) {
        Preconditions.checkNotNull(roleId, "入参 角色ID roleId 不可以为空.");
        if (ArrayUtils.isEmpty(resourceIds)) {
            getBean(RoleEntityAction.class).clearAuthors(roleId, company);
        } else {
            Optional<List<ResEntity>> reses = getBean(ResourcesFactory.class).getPageRes(1L, resourceIds);
            if (reses.isPresent()) {
                getBean(RoleEntityAction.class).authorized(roleId, reses.get(), company);
            } else {
                getBean(RoleEntityAction.class).clearAuthors(roleId, company);
            }
        }
    }

}
