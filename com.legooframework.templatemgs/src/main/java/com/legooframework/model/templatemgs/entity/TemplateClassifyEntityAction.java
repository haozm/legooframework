package com.legooframework.model.templatemgs.entity;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.web.TreeNode;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TemplateClassifyEntityAction extends BaseEntityAction<TemplateClassifyEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TemplateClassifyEntityAction.class);

    public TemplateClassifyEntityAction() {
        super("templatemgsCache");
    }

    @SuppressWarnings("unchecked")
    public Optional<List<TemplateClassifyEntity>> loadByCompany(CrmOrganizationEntity company) {
        Preconditions.checkNotNull(company, "入参 CrmOrganizationEntity company 不可以为空值");
        Preconditions.checkState(company.isCompany(), "入参 CrmOrganizationEntity 需为公司... 而实际为 %s ", company);
        final String cacheKey = String.format("%s_tree_%s", getModelName(), company.getId());
        if (getCache().isPresent()) {
            List<TemplateClassifyEntity> temps = getCache().get().get(cacheKey, List.class);
            if (CollectionUtils.isNotEmpty(temps)) return Optional.of(temps);
        }
        Optional<List<TemplateClassifyEntity>> res = loadByCompany(company.getId());
        getCache().ifPresent(c -> res.ifPresent(l -> c.put(cacheKey, l)));
        return res;
    }

    public Optional<List<TreeNode>> loadTreeNodeByCompany(CrmOrganizationEntity company) {
        Optional<List<TemplateClassifyEntity>> temps = loadByCompany(company);
        if (!temps.isPresent()) return Optional.empty();
        List<TreeNode> treeNodes = Lists.transform(temps.get(), new FunctionImpl());
        return Optional.ofNullable(CollectionUtils.isEmpty(treeNodes) ? null : treeNodes);
    }

    private Optional<List<TemplateClassifyEntity>> loadByCompany(Integer companyId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        Optional<List<TemplateClassifyEntity>> res = super.queryForEntities("loadByCompany", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByCompany(%s) return %s", companyId, res.orElse(null)));
        return res;
    }

    class FunctionImpl implements Function<TemplateClassifyEntity, TreeNode> {
        @Override
        public TreeNode apply(TemplateClassifyEntity input) {
            return input.treeNode();
        }
    }

    @Override
    protected RowMapper<TemplateClassifyEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<TemplateClassifyEntity> {
        @Override
        public TemplateClassifyEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new TemplateClassifyEntity(res.getString("id"), res);
        }
    }
}
