package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SMSBlackListEntityAction extends BaseEntityAction<SMSBlackListEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SMSBlackListEntityAction.class);

    public SMSBlackListEntityAction() {
        super("smsGateWayCache");
    }

    public void uneffective(String phoneNo, final Integer companyId) {
//        Preconditions.checkArgument(!Strings.isNullOrEmpty(phoneNo), "手机号码不可以为空值...");
//        LoginContext user = LoginContextHolder.get();
//        Optional<SMSBlackListEntity> optional = findById(phoneNo, companyId);
//        if (optional.isPresent()) {
//            Optional<SMSBlackListEntity> clone = optional.get().uneffective();
//            clone.ifPresent(c -> {
//                super.updateAction(c, "uneffective");
//                evict(companyId);
//            });
//        } else {
//            SMSBlackListEntity entity = SMSBlackListEntity.effectiveInstance(phoneNo, false, null, user);
//            super.updateAction(entity, "insert");
//            evict(companyId);
//        }
    }

    public void effective(String phoneNo, final Integer companyId) {
//        Preconditions.checkArgument(!Strings.isNullOrEmpty(phoneNo), "手机号码不可以为空值...");
//        LoginContext user = LoginContextHolder.get();
//        Optional<SMSBlackListEntity> optional = findById(phoneNo, companyId);
//        if (optional.isPresent()) {
//            if (!optional.get().isDisable()) {
//                super.updateAction(optional.get(), "delete");
//                evict(companyId);
//                return;
//            }
//            Optional<SMSBlackListEntity> clone = optional.get().effective();
//            clone.ifPresent(c -> {
//                super.updateAction(c, "effective");
//                evict(companyId);
//            });
//        } else {
//            SMSBlackListEntity entity = SMSBlackListEntity.effectiveInstance(phoneNo, false, null, user);
//            super.updateAction(entity, "insert");
//            evict(companyId);
//        }
    }

    public void diabled(Collection<SMSBlackListEntity> entities) {
        if (CollectionUtils.isEmpty(entities)) return;
        Optional<List<SMSBlackListEntity>> black_list = loadAll();
        if (black_list.isPresent()) {
            List<SMSBlackListEntity> not_exits = entities.stream().filter(x -> !black_list.get().contains(x))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(not_exits)) {
                batchInsert("batchInsert", not_exits);
                evict();
                if (logger.isDebugEnabled())
                    logger.debug(String.format("diabled() size is %s", not_exits.size()));
            }
        } else {
            batchInsert("batchInsert", entities);
            evict();
            if (logger.isDebugEnabled())
                logger.debug(String.format("diabled() size is %s", entities.size()));
        }
    }

    public Optional<List<Integer>> loadSMSCompanys() {
        List<Integer> list = getJdbcTemplate().queryForList("SELECT DISTINCT(company_id) FROM SMS_TRANSPORT_LOG",
                Integer.class);
        return Optional.of(CollectionUtils.isEmpty(list) ? null : list);
    }

    public DateTime[] getLastSyncTime() {
        String query_sql = "SELECT createTime FROM SMS_LAST_SYNC_DATE where sync_type='SYNC_SMS_BLACK' ORDER BY id DESC LIMIT 1";
        DateTime start_date = new DateTime(getJdbcTemplate().queryForObject(query_sql, Date.class));
        final DateTime end_date = DateTime.now();
        String insert_sql = "INSERT INTO SMS_LAST_SYNC_DATE (sync_type, createTime) VALUES('SYNC_SMS_BLACK', ?)";
        getJdbcTemplate().update(insert_sql, ps -> ps.setObject(1, end_date.toDate()));
        return new DateTime[]{start_date, end_date};
    }

    private void evict() {
        getCache().ifPresent(c -> c.evict(String.format("%s_company_%s", getModelName(), "all")));
    }

    @SuppressWarnings("unchecked")
    public Optional<List<SMSBlackListEntity>> loadAll() {
        final String cache_key = String.format("%s_company_%s", getModelName(), "all");
        if (getCache().isPresent()) {
            List<SMSBlackListEntity> list = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(list)) return Optional.of(list);
        }
        Optional<List<SMSBlackListEntity>> list = super.queryForEntities("loadAll", null, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAll() size is %s", list.map(List::size).orElse(0)));
        getCache().ifPresent(c -> list.ifPresent(l -> c.put(cache_key, l)));
        return list;
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
