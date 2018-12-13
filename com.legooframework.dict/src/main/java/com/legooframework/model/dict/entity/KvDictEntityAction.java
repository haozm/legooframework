package com.legooframework.model.dict.entity;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KvDictEntityAction extends BaseEntityAction<KvDictEntity> {

    private static final Logger logger = LoggerFactory.getLogger(KvDictEntityAction.class);

    public KvDictEntityAction() {
        super("ForeverCache");
    }

    public void insert(String type, String value, String name, String desc, int index) {
        LoginContext loginContext = LoginContextHolder.get();
        Preconditions.checkArgument(!Strings.isNullOrEmpty(type));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(value));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));

        Optional<KvDictEntity> exits = findByValue(type, value);
        Preconditions.checkState(!exits.isPresent(), "存在Type=%s,Value=%s 对应的参数值，请检查输入.",
                type, value);
        KvDictEntity entity = new KvDictEntity(type, loginContext, value, name, desc, index);
        int result = super.update(getStatementFactory(), getModelName(), "insert", entity);
        Preconditions.checkState(1 == result);
        getCache().ifPresent(c -> c.evict(cacheByIdKey(entity.getType())));
    }

    public int editAction(KvDictEntity updateInstance) {
        Preconditions.checkNotNull(updateInstance);
        Optional<KvDictEntity> exits = findByValue(updateInstance.getType(), updateInstance.getValue());
        Preconditions.checkState(exits.isPresent(), "不存在Type=%s,Value=%s 对应的参数值，请检查输入.",
                updateInstance.getType(), updateInstance.getValue());
        if (exits.get().equals(updateInstance)) return 0;
        LoginContext loginContext = LoginContextHolder.get();
        updateInstance.setEditor(loginContext.getLoginId());
        int result = super.update(getStatementFactory(), getModelName(), "edit", updateInstance);
        Preconditions.checkState(1 == result);
        getCache().ifPresent(c -> c.evict(cacheByIdKey(updateInstance.getType())));
        return result;
    }

    public Optional<KvDictEntity> findById(String type, Object id) {
        Preconditions.checkNotNull(id);
        Optional<List<KvDictEntity>> entities = loadByType(type);
        if (!entities.isPresent()) return Optional.empty();
        return entities.get().stream().filter(x -> Objects.equal(x.getId(), id)).findFirst();
    }

    public Optional<KvDictEntity> findByValue(String type, String value) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(value));
        Optional<List<KvDictEntity>> entities = loadByType(type);
        if (!entities.isPresent()) return Optional.empty();
        return entities.get().stream().filter(x -> Objects.equal(x.getValue(), value)).findFirst();
    }

    @Override
    public Optional<KvDictEntity> findById(Object id) {
        throw new UnsupportedOperationException("please invoke method findById(type,id)");
    }

    public Optional<List<KvDictEntity>> findByType(String type) {
        if (Strings.isNullOrEmpty(type)) return Optional.empty();
        return loadByType(type);
    }

    @SuppressWarnings("unchecked")
    private Optional<List<KvDictEntity>> loadByType(String type) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(type), "入参 String type 不可以为空值.");
        final String cache_key = cacheByIdKey(type);
        if (getCache().isPresent()) {
            List<KvDictEntity> cacheValue = getCache().get().get(cache_key, List.class);
            if (!CollectionUtils.isEmpty(cacheValue)) {
                if (logger.isTraceEnabled())
                    logger.trace(String.format("Hit Cache by Key %s And return List<KvDictEntity>", cache_key));
                return Optional.of(cacheValue);
            }
        }

        Map<String, Object> params = Maps.newHashMap();
        params.put("type", type);
        Optional<List<KvDictEntity>> entities = queryForEntities(getStatementFactory(), getModelName(),
                "loadByType", params, getRowMapper());

        entities.ifPresent(e -> getCache().ifPresent(c -> c.put(cache_key, e)));
        return entities;
    }

    @Override
    protected RowMapper<KvDictEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<KvDictEntity> {
        @Override
        public KvDictEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new KvDictEntity(res.getInt("dictId"), res);
        }
    }
}
