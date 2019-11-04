package com.legooframework.model.smsresult.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SMSBlackListEntityAction extends BaseEntityAction<SMSBlackListEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SMSBlackListEntityAction.class);

    public SMSBlackListEntityAction() {
        super(null);
    }

    public void batchInsert(Collection<SMSBlackListEntity> smsBlackList) {
        if (CollectionUtils.isEmpty(smsBlackList)) return;
        super.batchInsert("batchReplace", smsBlackList);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(smsBlackList) is %s", smsBlackList));
    }

    public Optional<List<SMSBlackListEntity>> loadByInterval(LocalDateTime data_start, LocalDateTime date_end,
                                                             Collection<Integer> companyIds) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("date_start", data_start.toDate());
        params.put("date_end", date_end.toDate());
        params.put("companyIds", companyIds);
        Optional<List<SMSBlackListEntity>> lists = super.queryForEntities("loadByInterval", params, getRowMapper());
        if (lists.isPresent() && logger.isDebugEnabled())
            logger.debug(String.format("loadByInterval(%s,%s) res %s", data_start, date_end, lists));
        return lists;
    }

    @Override
    protected RowMapper<SMSBlackListEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SMSBlackListEntity> {
        @Override
        public SMSBlackListEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSBlackListEntity(res.getString("id"), res);
        }
    }
}
