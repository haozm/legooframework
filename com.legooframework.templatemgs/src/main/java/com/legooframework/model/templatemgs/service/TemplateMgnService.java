package com.legooframework.model.templatemgs.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.core.web.TreeNode;
import com.legooframework.model.core.web.TreeUtil;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.templatemgs.entity.TemplateClassifyEntityAction;

import java.util.List;
import java.util.Optional;

public class TemplateMgnService extends BaseService {

    public TreeNode loadTreeNodeByCompany(Integer companyId) {
        Preconditions.checkNotNull(companyId, "入参 Integer companyId 不可以为空值...");
        Optional<CrmOrganizationEntity> company = getCompanyAction().findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "Id = %s 对应的公司不存在...", companyId);
        Optional<List<TreeNode>> treeNodes = getClassifyAction().loadTreeNodeByCompany(company.get());
        final TreeNode root = new TreeNode("0000", "0000", company.get().getName(), null);
        treeNodes.ifPresent(x -> TreeUtil.buildTree(root, x));
        return root;
    }

    @Override
    protected Bundle getLocalBundle() {
        return getBean("templatemgsBundle", Bundle.class);
    }

    private CrmOrganizationEntityAction getCompanyAction() {
        return getBean(CrmOrganizationEntityAction.class);
    }

    private TemplateClassifyEntityAction getClassifyAction() {
        return getBean(TemplateClassifyEntityAction.class);
    }
}
