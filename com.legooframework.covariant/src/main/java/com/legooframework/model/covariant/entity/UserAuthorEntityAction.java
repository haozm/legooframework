package com.legooframework.model.covariant.entity;

import com.google.common.base.Preconditions;
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

public class UserAuthorEntityAction extends BaseEntityAction<UserAuthorEntity> {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthorEntityAction.class);

    public UserAuthorEntityAction() {
        super(Constant.CACHE_ENTITYS);
    }

    public UserAuthorEntity loadUserById(Integer userId, Integer companyId) {
        String cache_key = String.format("EMP_USER_%d", userId);
        if (getCache().isPresent()) {
            UserAuthorEntity user = getCache().get().get(cache_key, UserAuthorEntity.class);
            if (user != null) return user;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("userId", userId);
        if (companyId != null) params.put("companyId", companyId);
        params.put("sql", "loadUserById");
        Optional<List<UserAuthorEntity>> list = findByParams(params);
        Preconditions.checkState(list.isPresent(), "不存在 userId= %s,companyId=%s 对应的用户...", userId, companyId);
        getCache().ifPresent(c -> c.put(cache_key, list.get().get(0)));
        return list.get().get(0);
    }

    @Override
    @Deprecated
    public UserAuthorEntity loadById(Object id) {
        throw new UnsupportedOperationException("不支持该方法，请调用loadUserById(Integer userId, Integer companyId) ....");
    }

    private Optional<List<UserAuthorEntity>> findByParams(Map<String, Object> params) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByParams(%s)", params));
        Optional<List<UserAuthorEntity>> listOpt = super.queryForEntities("query4list", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByParams(%s) res size %s", params, listOpt.map(List::size).orElse(0)));
        return listOpt;
    }

    @Override
    protected RowMapper<UserAuthorEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<UserAuthorEntity> {
        @Override
        public UserAuthorEntity mapRow(ResultSet res, int i) throws SQLException {
            return new UserAuthorEntity(res.getInt("id"), res);
        }
    }
}
