package com.legooframework.model.covariant.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MsgTemplateEnityAction extends BaseEntityAction<MsgTemplateEnity> {

    public MsgTemplateEnityAction() {
        super(Constant.CACHE_ENTITYS);
    }

    @Override
    public Optional<MsgTemplateEnity> findById(Object id) {
        final String cache_key = String.format("MSG_TEMP_BYID_%s", id);
        if (getCache().isPresent()) {
            MsgTemplateEnity cacheVal = getCache().get().get(cache_key, MsgTemplateEnity.class);
            if (cacheVal != null) return Optional.of(cacheVal);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("sql", "findById");
        params.put("msgTemplateId", id);
        Optional<List<MsgTemplateEnity>> list = findByParams(params);
        Optional<MsgTemplateEnity> optional = list.map(x -> x.get(0));
        getCache().ifPresent(c -> optional.ifPresent(opt -> c.put(cache_key, opt)));
        return optional;
    }

    public Optional<MsgTemplateEnity> findOneBirthCareTemplet4Store(StoEntity store) {
        Optional<List<MsgTemplateEnity>> temps = findAllBirthCareTemplet4Store(store);
        return temps.flatMap(x -> x.stream().filter(MsgTemplateEnity::isEnabled).findFirst());
    }

    public Optional<List<MsgTemplateEnity>> findAllBirthCareTemplet4Store(StoEntity store) {
        Map<String, Object> params = store.toParamMap();
        params.put("useType", MsgTemplateEnity.USETYPE_BIRTHDAYCARE);
        params.put("sql", "findAllBirthCareTemplet4Store");
        return findByParams(params);
    }

    Optional<List<MsgTemplateEnity>> findByParams(Map<String, Object> params) {
        return super.queryForEntities("query4list", params, getRowMapper());
    }

    @Override
    protected RowMapper<MsgTemplateEnity> getRowMapper() {
        return new RowMapperImpl();
    }


    private static class RowMapperImpl implements RowMapper<MsgTemplateEnity> {
        @Override
        public MsgTemplateEnity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new MsgTemplateEnity(resultSet.getInt("id"), resultSet);
        }
    }
}
