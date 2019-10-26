package com.legooframework.model.covariant.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MemberEntityAction extends BaseEntityAction<MemberEntity> {

    public MemberEntityAction() {
        super(null);
    }

    public Optional<List<MemberEntity>> findByShoppingGuide(EmpEntity shoppingGuide) {
        Map<String, Object> params = shoppingGuide.toParamMap();
        params.put("sql", "findByShoppingGuide");
        return findByParams(params);
    }

    public Optional<MemberEntity> findByWxUser(WxUserEntity wxUser) {
        Map<String, Object> params = wxUser.toParamMap();
        params.put("sql", "findByWxUser");
        Optional<List<MemberEntity>> exits = findByParams(params);
        return exits.map(x -> x.get(0));
    }

    public Optional<List<MemberEntity>> findByStore(StoEntity store) {
        Map<String, Object> params = store.toParamMap();
        params.put("sql", "findByStore");
        return findByParams(params);
    }

    public Optional<List<MemberEntity>> findByIds(Collection<Integer> ids) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("memberIds", ids);
        params.put("sql", "findByIds");
        return findByParams(params);
    }

    @Override
    public Optional<MemberEntity> findById(Object id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("memberId", id);
        params.put("sql", "findById");
        Optional<List<MemberEntity>> exits = findByParams(params);
        return exits.map(x -> x.get(0));
    }

    private Optional<List<MemberEntity>> findByParams(Map<String, Object> params) {
        return super.queryForEntities("query4list", params, getRowMapper());
    }

    @Override
    protected RowMapper<MemberEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<MemberEntity> {
        @Override
        public MemberEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new MemberEntity(resultSet.getInt("id"), resultSet);
        }
    }
}
