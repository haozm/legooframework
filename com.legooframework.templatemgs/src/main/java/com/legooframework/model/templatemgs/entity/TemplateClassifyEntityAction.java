package com.legooframework.model.templatemgs.entity;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TemplateClassifyEntityAction extends BaseEntityAction<TemplateClassifyEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TemplateClassifyEntityAction.class);

    public TemplateClassifyEntityAction() {
        super("templatemgsCache");
    }

    public Optional<TemplateClassifyEntity> loadById(String id) {
        LoginContext user = LoginContextHolder.get();
        Optional<List<TemplateClassifyEntity>> list = loadByCompany(user.getTenantId().intValue());
        return list.flatMap(templateClassifyEntities -> templateClassifyEntities.stream()
                .filter(x -> x.getId().equals(id)).findFirst());
    }

    public Optional<List<TemplateClassifyEntity>> loadBySubClassifies(TemplateClassifyEntity parent) {
        LoginContext user = LoginContextHolder.get();
        Optional<List<TemplateClassifyEntity>> all_nodes = loadByCompany(user.getTenantId().intValue());
        if (!all_nodes.isPresent()) return Optional.empty();
        List<TemplateClassifyEntity> sub_list = all_nodes.get().stream().filter(x -> x.isMyFather(parent))
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    public Optional<List<TreeNode>> loadTreeNodeByCompany(CrmOrganizationEntity company) {
        Optional<List<TemplateClassifyEntity>> temps = loadByCompany(company.getId());
        if (!temps.isPresent()) return Optional.empty();
        List<TreeNode> treeNodes = Lists.transform(temps.get(), new FunctionImpl());
        return Optional.ofNullable(CollectionUtils.isEmpty(treeNodes) ? null : treeNodes);
    }
    
    public void addTemplateClassfy(TemplateClassifyEntity template) {
    	Objects.requireNonNull(template);
    	String sql = getStatementFactory().getExecSql("TemplateClassifyEntity", "insert", null);
    	int result = getNamedParameterJdbcTemplate().update(sql, template.toMap());
    	Preconditions.checkState(1 == result, String.format("添加模板类型失败【%s】", template.toString()));
    	if(getCache().isPresent()) getCache().get().clear();
    }
    
    private Optional<List<TemplateClassifyEntity>> loadByCompany(Integer companyId) {
        final String cacheKey = String.format("%s_tree_%s", getModelName(), companyId);
        if (getCache().isPresent()) {
            @SuppressWarnings("unchecked")
            List<TemplateClassifyEntity> temps = getCache().get().get(cacheKey, List.class);
            if (CollectionUtils.isNotEmpty(temps)) return Optional.of(temps);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        Optional<List<TemplateClassifyEntity>> res = super.queryForEntities("loadByCompany", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByCompany(%s) return %s", companyId, res.map(List::size).orElse(0)));
        getCache().ifPresent(c -> res.ifPresent(l -> c.put(cacheKey, l)));
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
