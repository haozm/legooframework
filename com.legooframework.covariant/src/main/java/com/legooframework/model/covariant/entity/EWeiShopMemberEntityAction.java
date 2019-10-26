package com.legooframework.model.covariant.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EWeiShopMemberEntityAction extends BaseEntityAction<EWeiShopMemberEntity> {

    private static final Logger logger = LoggerFactory.getLogger(EWeiShopMemberEntityAction.class);

    public EWeiShopMemberEntityAction() {
        super(null);
    }

    public Optional<EWeiShopMemberEntity> findByOpenId(String openId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("openId", openId);
        params.put("sql", "findByOpenid");
        Optional<List<EWeiShopMemberEntity>> users = findByParams(params);
        return users.map(x -> x.get(0));
    }

    public Optional<EWeiShopMemberEntity> findByMember(MemberEntity member) {
        Map<String, Object> params = member.toParamMap();
        params.put("sql", "findByMember");
        Optional<List<EWeiShopMemberEntity>> users = findByParams(params);
        return users.map(x -> x.get(0));
    }

    private Optional<List<EWeiShopMemberEntity>> findByParams(Map<String, Object> params) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByParams(%s)", params));
        return super.queryForEntities("query4list", params, getRowMapper());
    }

    @Override
    protected RowMapper<EWeiShopMemberEntity> getRowMapper() {
        return new ShopMemberRowMapper();
    }

    private static class ShopMemberRowMapper implements RowMapper<EWeiShopMemberEntity> {
        @Override
        public EWeiShopMemberEntity mapRow(ResultSet res, int i) throws SQLException {
            return new EWeiShopMemberEntity(res.getInt("id"), res);
        }
    }
}
