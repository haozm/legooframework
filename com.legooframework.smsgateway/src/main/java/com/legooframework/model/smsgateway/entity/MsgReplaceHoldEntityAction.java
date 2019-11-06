package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MsgReplaceHoldEntityAction extends BaseEntityAction<MsgReplaceHoldEntity> {

    private static final Logger logger = LoggerFactory.getLogger(MsgReplaceHoldEntityAction.class);

    public MsgReplaceHoldEntityAction() {
        super("templatemgsCache");
    }

    private final String COUNT_SQL = "SELECT COUNT(id) FROM MSG_TEMPLATE_REPLACE WHERE delete_flag = 0 AND (field_tag = :fieldTag OR replace_token = :replaceToken) AND tenant_id = :tenantId";

    public void addReplaceHold(UserAuthorEntity user, String fieldTag, String replaceToken, String defaultValue,
                               TokenType tokenType, Map<String, String> enumMap) {
        MsgReplaceHoldEntity ins = null;
        switch (tokenType) {
            case DATE:
                ins = MsgReplaceHoldEntity.createDate(user, fieldTag, replaceToken, defaultValue);
            case ENUM:
                ins = MsgReplaceHoldEntity.createEnum(user, fieldTag, replaceToken, enumMap);
            case STRING:
                ins = MsgReplaceHoldEntity.createString(user, fieldTag, replaceToken, defaultValue);
            default:
                break;
        }
        Map<String, Object> params = ins.toParamMap();
        long count = Objects.requireNonNull(getNamedParameterJdbcTemplate()).queryForObject(COUNT_SQL, params, Long.class);
        Preconditions.checkState(count == 0L, "存在重复的数据 %s,请检查...", ins);
        super.updateAction(ins, "addReplaceHold");
        final String cacheKey = String.format("%s_load_all", getModelName());
        getCache().ifPresent(c -> c.evict(cacheKey));
    }

    public MsgReplaceHoldList loadByUser(UserAuthorEntity user) {
        Optional<List<MsgReplaceHoldEntity>> all = loadAll();
        if (!all.isPresent()) return new MsgReplaceHoldList(user, null);
        List<MsgReplaceHoldEntity> sub_list = all.get().stream().filter(x -> x.isMyCompany(user)).collect(Collectors.toList());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByUser(%s) values is %s", user.getTenantId(),
                    CollectionUtils.isEmpty(sub_list) ? null : sub_list));
        return new MsgReplaceHoldList(user, sub_list);
    }

    public MsgReplaceHoldList loadByCompany(OrgEntity company) {
        Optional<List<MsgReplaceHoldEntity>> all = loadAll();
        if (!all.isPresent()) return new MsgReplaceHoldList(company, null);
        List<MsgReplaceHoldEntity> sub_list = all.get().stream().filter(x -> x.isMyCompany(company)).collect(Collectors.toList());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByCompany(%s) values is %s", company.getId(),
                    CollectionUtils.isEmpty(sub_list) ? null : sub_list));
        return new MsgReplaceHoldList(company, sub_list);
    }

    private Optional<List<MsgReplaceHoldEntity>> loadAll() {
        final String cacheKey = String.format("%s_load_all", getModelName());
        if (getCache().isPresent()) {
            @SuppressWarnings("unchecked")
            List<MsgReplaceHoldEntity> values = getCache().get().get(cacheKey, List.class);
            if (CollectionUtils.isNotEmpty(values)) return Optional.of(values);
        }
        Optional<List<MsgReplaceHoldEntity>> list = super.queryForEntities("loadAll", null, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAll() size is %s", list.map(List::size).orElse(0)));
        getCache().ifPresent(c -> list.ifPresent(x -> c.put(cacheKey, x)));
        return list;
    }

    @Override
    protected RowMapper<MsgReplaceHoldEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<MsgReplaceHoldEntity> {
        @Override
        public MsgReplaceHoldEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new MsgReplaceHoldEntity(res.getLong("id"), res);
        }
    }
}
