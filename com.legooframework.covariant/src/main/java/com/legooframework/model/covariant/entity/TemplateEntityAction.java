package com.legooframework.model.covariant.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TemplateEntityAction extends BaseEntityAction<TemplateEntity> {

    public TemplateEntityAction() {
        super(Constant.CACHE_ENTITYS);
    }

    static Comparator<TemplateEntity> COMPARATOR = Comparator.comparingInt(o -> o.isComRange() ? 1 : 0);

    public Optional<TemplateEntity> findByStoreWithClassifies(StoEntity store, String classifies) {
        Preconditions.checkNotNull(store, "门店信息不可为空值");
        Preconditions.checkNotNull(classifies, "模板分配不可为空值");
        Optional<List<TemplateEntity>> all_list = findAll();
        if (!all_list.isPresent()) return Optional.empty();
        List<TemplateEntity> list = all_list.get().stream().filter(UseRangeEntity::isEnabled).filter(x -> x.isStoreWithCompany(store))
                .filter(x -> x.isClassifies(classifies)).sorted(COMPARATOR).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) return Optional.empty();
        return Optional.of(list.get(0));
    }

    public Optional<List<Integer>> loadEnabledCompany(String classifies) {
        Preconditions.checkNotNull(classifies, "模板分配不可为空值");
        Optional<List<TemplateEntity>> all_list = findAll();
        if (!all_list.isPresent()) return Optional.empty();
        List<TemplateEntity> list = all_list.get().stream().filter(UseRangeEntity::isEnabled).filter(UseRangeEntity::isComRange)
                .filter(x -> x.isClassifies(classifies)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) return Optional.empty();
        List<Integer> comIds = list.stream().map(UseRangeEntity::getCompanyId).collect(Collectors.toList());
        return Optional.of(comIds);
    }


    @SuppressWarnings("unchecked")
    Optional<List<TemplateEntity>> findAll() {
        final String cache_key = "TEMPLATE_CACHE_ALL";
        if (getCache().isPresent()) {
            Object cacheVal = getCache().get().get(cache_key, Object.class);
            if (cacheVal != null) return Optional.of((List<TemplateEntity>) cacheVal);
        }
        Optional<List<TemplateEntity>> list = super.queryForEntities("query4list", null, getRowMapper());
        getCache().ifPresent(c -> list.ifPresent(l -> c.put(cache_key, l)));
        return list;
    }

    @Override
    protected RowMapper<TemplateEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<TemplateEntity> {
        @Override
        public TemplateEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new TemplateEntity(resultSet.getLong("id"), resultSet);
        }
    }
}
