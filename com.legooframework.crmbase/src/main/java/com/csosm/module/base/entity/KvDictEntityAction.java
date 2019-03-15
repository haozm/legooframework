package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class KvDictEntityAction extends BaseEntityAction<KvDictEntity> {

    public KvDictEntityAction() {
        super("KvDictEntity", "defCache");
    }

    public Map<String, List<KvDictEntity>> loadByTypes(OrganizationEntity company, String... types) {
        Preconditions.checkState(getCache().isPresent(), "内存Cache 不存在... 加载字典异常");
        Map<String, List<KvDictEntity>> result = Maps.newHashMap();
        Optional<ListMultimap<String, KvDictEntity>> multimapOpt = selectAllByCom(company.getId());
        Preconditions.checkState(multimapOpt.isPresent(), "当前公司尚未初始化数据字典.....");
        for (String type : types) {
            if (multimapOpt.get().containsKey(type)) result.put(type, multimapOpt.get().get(type));
        }
        return result;
    }

    public Optional<List<KvDictEntity>> findByType(OrganizationEntity company, String type) {
        Preconditions.checkNotNull(company);
        Optional<ListMultimap<String, KvDictEntity>> multimapOptional = selectAllByCom(company.getId());
        if (!multimapOptional.isPresent()) return Optional.absent();
        return Optional.fromNullable(multimapOptional.get().containsKey(type) ? multimapOptional.get().get(type) : null);
    }

    public Optional<KvDictEntity> findByTypeWithId(OrganizationEntity company, String type, String keyId) {
        Preconditions.checkNotNull(company);
        Optional<ListMultimap<String, KvDictEntity>> multimapOptional = selectAllByCom(company.getId());
        if (!multimapOptional.isPresent() && !multimapOptional.get().containsKey(type)) return Optional.absent();
        List<KvDictEntity> list = multimapOptional.get().get(type);
        java.util.Optional<KvDictEntity> exits = list.stream().filter(x -> x.getKey().equals(keyId)).findFirst();
        return Optional.fromNullable(exits.isPresent() ? exits.get() : null);
    }

    public KvDictEntity loadByTypeWithId(OrganizationEntity company, String type, String keyId) {
        Preconditions.checkNotNull(company);
        Optional<ListMultimap<String, KvDictEntity>> multimapOptional = selectAllByCom(company.getId());
        Preconditions.checkState(multimapOptional.isPresent() && multimapOptional.get().containsKey(type),
                "不存在类型为%s对应的数据字典", type);
        List<KvDictEntity> list = multimapOptional.get().get(type);
        java.util.Optional<KvDictEntity> exits = list.stream().filter(x -> x.getKey().equals(keyId)).findFirst();
        Preconditions.checkState(exits.isPresent(), "不存在Type = %s 与 Id=%s 对应的数据...");
        return exits.get();
    }

    @SuppressWarnings("unchecked")
    private Optional<ListMultimap<String, KvDictEntity>> selectAllByCom(Integer companyId) {
        Preconditions.checkNotNull(companyId, "公司ID不不可以为空值...");
        final String cache_key = String.format("DCIT_ALL_%s", companyId);
        if (getCache().isPresent()) {
            ListMultimap<String, KvDictEntity> multimap = (ListMultimap<String, KvDictEntity>) getCache().get()
                    .getIfPresent(cache_key);
            if (multimap != null) return Optional.of(multimap);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        List<KvDictEntity> list = getJdbc().query(getExecSql("load_all_bycompany", null), params, new RowMapperImpl());
        ListMultimap<String, KvDictEntity> multimap = ArrayListMultimap.create();
        if (CollectionUtils.isNotEmpty(list)) {
            for (KvDictEntity $it : list) multimap.put($it.getType(), $it);
        }
        if (getCache().isPresent() && CollectionUtils.isNotEmpty(list)) {
            getCache().get().put(cache_key, multimap);
        }
        return Optional.fromNullable(CollectionUtils.isEmpty(list) ? null : multimap);
    }

    class RowMapperImpl implements RowMapper<KvDictEntity> {
        @Override
        public KvDictEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    @Override
    protected ResultSetExtractor<KvDictEntity> getResultSetExtractor() {
        return null;
    }

    private KvDictEntity buildByResultSet(ResultSet resultSet) throws SQLException {
        return new KvDictEntity(resultSet.getInt("id"),
                resultSet.getString("value"),
                resultSet.getString("name"),
                resultSet.getString("desc"),
                resultSet.getString("dictType"),
                resultSet.getInt("dictIndex"));
    }

}
