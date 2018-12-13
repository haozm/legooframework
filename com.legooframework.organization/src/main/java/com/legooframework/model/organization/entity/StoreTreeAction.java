package com.legooframework.model.organization.entity;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StoreTreeAction extends BaseEntityAction<StoreTreeEntity> {

    private static final Logger logger = LoggerFactory.getLogger(StoreTreeAction.class);

    public StoreTreeAction() {
        super("OrganizationCache");
    }

    public Long addNode(StoreTreeEntity parent, String nodeName, int nodeSeq) {
        Preconditions.checkNotNull(parent, "上级ID不可以为空...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(nodeName), "节点名称不可以为空...");
        LoginContext loginUser = LoginContextHolder.get();
        idGenerator++;
        long id = idGenerator;
        StoreTreeEntity entity = new StoreTreeEntity(id, loginUser, parent, nodeName, nodeSeq);
        Optional<List<StoreTreeEntity>> exits = queryForEntities("exits", entity.toParamMap(), getRowMapper());
        Preconditions.checkState(!exits.isPresent(), "当前节点已经存在%s对应的节点定义... 不可以重复添加.");
        int res = updateAction(entity, "insert");
        Preconditions.checkState(1 == res, "持久化新增节点%s失败...", entity);
        final String cache_key = String.format("%s_tree_%s", getModelName(), loginUser.getTenantId());
        getCache().ifPresent(c -> c.evict(cache_key));
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s 新增下级节点 %s 成功 ....", parent.getNodeName(), entity));
        return entity.getId();
    }

    public boolean changeNode(Long id, String nodeName, int nodeSeq) {
        Preconditions.checkNotNull(id, "节点ID不可以为空...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(nodeName), "节点名称不可以为空...");
        Optional<StoreTreeEntity> entity = findById(id);
        Preconditions.checkState(entity.isPresent(), "id=%s 对应的节点不存在....");
        Optional<StoreTreeEntity> clone = entity.get().change(nodeName, nodeSeq);
        if (clone.isPresent()) {
            Optional<List<StoreTreeEntity>> exits = queryForEntities("exits", clone.get().toParamMap(), getRowMapper());
            Preconditions.checkState(!exits.isPresent(), "当前节点已经存在%s对应的节点定义... 不可以重复添加.");
            int res = updateAction(clone.get(), "change");
            Preconditions.checkState(1 == res, "修改节点%s失败...", entity);
            final String cache_key = String.format("%s_tree_%s", getModelName(), entity.get().getTenantId());
            getCache().ifPresent(c -> c.evict(cache_key));
        }
        return false;
    }

    public boolean removeNode(Long id) {
        Preconditions.checkNotNull(id, "节点ID不可以为空...");
        Optional<StoreTreeEntity> entity = findById(id);
        if (!entity.isPresent()) return true;
        Preconditions.checkState(!entity.get().exitsStores(), "当前节点下含有门店，无法删除....");
        Optional<List<StoreTreeEntity>> entities = loadAllByTenantId(LoginContextHolder.get().getTenantId());
        if (entities.isPresent()) {
            Optional<StoreTreeEntity> child = entities.get().stream().filter(x -> x.isParent(entity.get())).findFirst();
            Preconditions.checkState(!child.isPresent(), "当前节点含有下级节点，无法删除...");
        }
        int res = updateAction(entity.get(), "remove");
        Preconditions.checkState(1 == res, "修改节点%s失败...", entity);
        final String cache_key = String.format("%s_tree_%s", getModelName(), entity.get().getTenantId());
        getCache().ifPresent(c -> c.evict(cache_key));
        return true;
    }

    public boolean addStores(Long id, Collection<StoreEntity> stores) {
        Preconditions.checkNotNull(id, "节点ID不可以为空...");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(stores), "待添加的门店列表不可以为空...");
        Optional<StoreTreeEntity> entity = findById(id);
        Preconditions.checkState(entity.isPresent(), "ID=%s对应的节点实体不存在....", id);
        Optional<StoreTreeEntity> clone = entity.get().addStores(stores);
        if (clone.isPresent()) {
            int res = updateAction(clone.get(), "update_stores");
            Preconditions.checkState(1 == res, "添加门店持久化%s失败...", entity);
            final String cache_key = String.format("%s_tree_%s", getModelName(), entity.get().getTenantId());
            getCache().ifPresent(c -> c.evict(cache_key));
            return true;
        }
        return false;
    }

    public boolean removeStores(Long id, Collection<StoreEntity> stores) {
        Preconditions.checkNotNull(id, "节点ID不可以为空...");
        Optional<StoreTreeEntity> entity = findById(id);
        Preconditions.checkState(entity.isPresent(), "ID=%s对应的节点实体不存在....", id);
        Optional<StoreTreeEntity> clone = entity.get().removeStores(stores);
        if (clone.isPresent()) {
            int res = updateAction(clone.get(), "update_stores");
            Preconditions.checkState(1 == res, "删除门店持久化%s失败...", entity);
            final String cache_key = String.format("%s_tree_%s", getModelName(), entity.get().getTenantId());
            getCache().ifPresent(c -> c.evict(cache_key));
            return true;
        }
        return false;
    }

    @Override
    public Optional<StoreTreeEntity> findById(Object id) {
        LoginContext loginUser = LoginContextHolder.get();
        Optional<List<StoreTreeEntity>> entities = loadAllByTenantId(loginUser.getTenantId());
        if (entities.isPresent())
            return entities.get().stream().filter(x -> Objects.equal(x.getId(), id)).findFirst();
        return Optional.empty();
    }

    public Optional<List<StoreTreeEntity>> loadAll(CompanyEntity company) {
        Preconditions.checkNotNull(company);
        return loadAllByTenantId(company.getId());
    }

    @SuppressWarnings("unchecked")
    private Optional<List<StoreTreeEntity>> loadAllByTenantId(Long tenantId) {
        Preconditions.checkNotNull(tenantId);
        final String cache_key = String.format("%s_tree_%s", getModelName(), tenantId);
        if (getCache().isPresent()) {
            Optional<List<StoreTreeEntity>> cache_val = getCache().map(c -> c.get(cache_key, List.class));
            if (cache_val.isPresent()) {
                if (logger.isTraceEnabled())
                    logger.trace(String.format("Hit cache_key = %s From Cache", cache_key));
                return cache_val;
            }
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("tenantId", tenantId);
        Optional<List<StoreTreeEntity>> nodes = super.queryForEntities("loadAll", params, getRowMapper());
        nodes.ifPresent(d -> getCache().ifPresent(c -> c.put(cache_key, d)));
        return nodes;
    }

    @Override
    protected RowMapper<StoreTreeEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<StoreTreeEntity> {
        @Override
        public StoreTreeEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new StoreTreeEntity(res.getLong("id"), res);
        }
    }

    private long idGenerator;

    public void init() {
        long max_id = queryForLong("SELECT MAX(id) FROM org_store_tree ", 100000L);
        this.idGenerator = max_id + 1;
    }
}
