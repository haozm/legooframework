package com.legooframework.model.statistical.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StatisticalRefEntityAction extends BaseEntityAction<StatisticalRefEntity> {

    private static final Logger logger = LoggerFactory.getLogger(StatisticalRefEntityAction.class);

    public StatisticalRefEntityAction() {
        super(null);
    }

    Optional<List<StatisticalRefEntity>> find4CompanyRange(UserAuthorEntity user) {
        Optional<List<StatisticalRefEntity>> list = findByCompany(user);
        if (!list.isPresent()) return Optional.empty();
        List<StatisticalRefEntity> sub_list = list.get().stream().filter(StatisticalRefEntity::isCompanyRange)
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    Optional<List<StatisticalRefEntity>> findByCompany(UserAuthorEntity user) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", user.getCompanyId());
        params.put("sql", "findByCompany");
        return findByParams(params);
    }

    private Optional<List<StatisticalRefEntity>> findByParams(Map<String, Object> params) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByParams(%s)", params));
        Optional<List<StatisticalRefEntity>> listOpt = super.queryForEntities("query4list", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByParams(%s) res size %s", params, listOpt.map(List::size).orElse(0)));
        return listOpt;
    }

    @Override
    protected RowMapper<StatisticalRefEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<StatisticalRefEntity> {
        @Override
        public StatisticalRefEntity mapRow(ResultSet res, int i) throws SQLException {
            return new StatisticalRefEntity(res.getInt("id"), res);
        }
    }
}
