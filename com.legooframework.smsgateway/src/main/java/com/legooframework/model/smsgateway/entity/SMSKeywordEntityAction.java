package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SMSKeywordEntityAction extends BaseEntityAction<SMSKeywordEntity> {

    public SMSKeywordEntityAction() {
        super("smsFixedCache");
    }

    public void saveOrUpdate(String keyword, boolean enbaled) {
        Optional<List<SMSKeywordEntity>> keywords = findAll();
        if (keywords.isPresent()) {
            Optional<SMSKeywordEntity> keyword_entity = keywords.get().stream().filter(x -> x.equalsByKeyword(keyword)).findFirst();
            if (keyword_entity.isPresent()) {
                keyword_entity = keyword_entity.get().enabled(enbaled);
                keyword_entity.ifPresent(x -> {
                    super.updateAction(x, "update");
                    getCache().ifPresent(c -> c.evict(String.format("%s_%s", getModelName(), "all")));
                });
            } else {
                SMSKeywordEntity kw = new SMSKeywordEntity(keyword, enbaled);
                super.updateAction(kw, "insert");
                getCache().ifPresent(c -> c.evict(String.format("%s_%s", getModelName(), "all")));
            }
        } else {
            SMSKeywordEntity kw = new SMSKeywordEntity(keyword, enbaled);
            super.updateAction(kw, "insert");
            getCache().ifPresent(c -> c.evict(String.format("%s_%s", getModelName(), "all")));
        }
    }

    public Optional<List<SMSKeywordEntity>> findEnableds() {
        Optional<List<SMSKeywordEntity>> keywords = findAll();
        if (keywords.isPresent()) {
            List<SMSKeywordEntity> sub_list = keywords.get().stream().filter(SMSKeywordEntity::isEnabled).collect(Collectors.toList());
            return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
        }
        return Optional.empty();
    }

    private Optional<List<SMSKeywordEntity>> findAll() {
        Optional<List<SMSKeywordEntity>> list = super.queryForEntities("findByAll", null, getRowMapper());
        getCache().ifPresent(c -> list.ifPresent(l -> c.put(String.format("%s_%s", getModelName(), "all"), l)));
        return list;
    }

    @Override
    protected RowMapper<SMSKeywordEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SMSKeywordEntity> {
        @Override
        public SMSKeywordEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSKeywordEntity(res.getLong("id"), res);
        }
    }
}
