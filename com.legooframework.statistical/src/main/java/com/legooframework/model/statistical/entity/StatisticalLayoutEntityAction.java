package com.legooframework.model.statistical.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StatisticalLayoutEntityAction extends BaseEntityAction<StatisticalLayoutEntity> {

    private static final Logger logger = LoggerFactory.getLogger(StatisticalLayoutEntityAction.class);

    public StatisticalLayoutEntityAction() {
        super(null);
    }

    private Optional<StatisticalLayoutEntity> matchedLayout(UserAuthorEntity user, List<StatisticalLayoutEntity> layouts) {
        if (layouts.size() == 1) return Optional.of(layouts.get(0));
        Optional<StatisticalLayoutEntity> matched = layouts.stream().filter(x -> x.matchByUser(user)).findFirst();
        if (matched.isPresent()) return matched;
        return layouts.stream().filter(StatisticalLayoutEntity::isCompanyRange).findFirst();
    }

    public Optional<StatisticalLayoutEntity> loadPageByUser(UserAuthorEntity user, String layoutType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", user.getCompanyId());
        params.put("layoutType", layoutType);
        params.put("sql", "loadPageByUser");
        Optional<List<StatisticalLayoutEntity>> list = findByParams(params);
        if (!list.isPresent()) return Optional.empty();
        return matchedLayout(user, list.get());
    }

    public Optional<StatisticalLayoutEntity> loadSubPageByUser(UserAuthorEntity user, String layoutType, String statisticalId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", user.getCompanyId());
        params.put("layoutType", layoutType);
        params.put("statisticalId", statisticalId);
        params.put("sql", "loadSubPageByUser");
        Optional<List<StatisticalLayoutEntity>> list = findByParams(params);
        if (!list.isPresent()) return Optional.empty();
        return matchedLayout(user, list.get());
    }

    private Optional<List<StatisticalLayoutEntity>> findByParams(Map<String, Object> params) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByParams(%s)", params));
        Optional<List<StatisticalLayoutEntity>> listOpt = super.queryForEntities("query4list", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByParams(%s) res size %s", params, listOpt.map(List::size).orElse(0)));
        return listOpt;
    }


    @Override
    protected RowMapper<StatisticalLayoutEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<StatisticalLayoutEntity> {
        @Override
        public StatisticalLayoutEntity mapRow(ResultSet res, int i) throws SQLException {
            return new StatisticalLayoutEntity(res.getInt("id"), res);
        }
    }
}
