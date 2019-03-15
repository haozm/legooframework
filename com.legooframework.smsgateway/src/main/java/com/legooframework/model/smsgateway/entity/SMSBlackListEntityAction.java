package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SMSBlackListEntityAction extends BaseEntityAction<SMSBlackListEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SMSBlackListEntityAction.class);

    public SMSBlackListEntityAction() {
        super("smsGateWayCache");
    }

    public void uneffective(String phoneNo, final Integer companyId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(phoneNo), "手机号码不可以为空值...");
        LoginContext user = LoginContextHolder.get();
        Optional<SMSBlackListEntity> optional = findById(phoneNo, companyId);
        if (optional.isPresent()) {
            Optional<SMSBlackListEntity> clone = optional.get().uneffective();
            clone.ifPresent(c -> {
                super.updateAction(c, "uneffective");
                evict(companyId);
            });
        } else {
            SMSBlackListEntity entity = SMSBlackListEntity.effectiveInstance(phoneNo, false, null, user);
            super.updateAction(entity, "insert");
            evict(companyId);
        }
    }

    public void effective(String phoneNo, final Integer companyId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(phoneNo), "手机号码不可以为空值...");
        LoginContext user = LoginContextHolder.get();
        Optional<SMSBlackListEntity> optional = findById(phoneNo, companyId);
        if (optional.isPresent()) {
            if (!optional.get().isDisable()) {
                super.updateAction(optional.get(), "delete");
                evict(companyId);
                return;
            }
            Optional<SMSBlackListEntity> clone = optional.get().effective();
            clone.ifPresent(c -> {
                super.updateAction(c, "effective");
                evict(companyId);
            });
        } else {
            SMSBlackListEntity entity = SMSBlackListEntity.effectiveInstance(phoneNo, false, null, user);
            super.updateAction(entity, "insert");
            evict(companyId);
        }
    }

    public void diabled(Collection<String> phoneNos, Integer companyId) {
        if (CollectionUtils.isEmpty(phoneNos)) return;
        Optional<List<SMSBlackListEntity>> optional = findByIds(phoneNos, companyId);
        if (optional.isPresent()) {
            List<String> exit_ids = optional.get().stream().map(BaseEntity::getId).collect(Collectors.toList());
            List<String> ids = optional.get().stream().map(SMSBlackListEntity::disabled)
                    .collect(Collectors.toList()).stream().filter(Optional::isPresent).map(Optional::get)
                    .map(BaseEntity::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(ids)) {
                Map<String, Object> params = Maps.newHashMap();
                params.put("ids", ids);
                params.put("companyId", companyId);
                super.updateAction("disabled", params);
            }
            List<String> insertIds = phoneNos.stream().filter(x -> !exit_ids.contains(x)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(insertIds)) {
                List<SMSBlackListEntity> entities = insertIds.stream().map(x -> SMSBlackListEntity.disableInstance(x, true, companyId))
                        .collect(Collectors.toList());
                batchInsert("batchInsert", entities);
            }
            evict(companyId);
        } else {
            List<SMSBlackListEntity> entities = phoneNos.stream().map(x -> SMSBlackListEntity.disableInstance(x, true, companyId))
                    .collect(Collectors.toList());
            batchInsert("batchInsert", entities);
            evict(companyId);
        }
    }

    private Optional<List<SMSBlackListEntity>> findByIds(Collection<String> phoneNos, Integer companyId) {
        Optional<List<SMSBlackListEntity>> bks = loadByCompanyId(companyId);
        if (!bks.isPresent()) return Optional.empty();
        List<SMSBlackListEntity> sublist = bks.get().stream().filter(x -> phoneNos.contains(x.getId()))
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sublist) ? null : sublist);
    }

    private Optional<SMSBlackListEntity> findById(String id, Integer companyId) {
        Optional<List<SMSBlackListEntity>> bks = loadByCompanyId(companyId);
        return bks.flatMap(bk -> bk.stream()
                .filter(x -> StringUtils.equals(x.getId(), id)).findFirst());
    }

    private void evict(final Integer companyId) {
        getCache().ifPresent(c -> c.evict(String.format("%s_company_%s", getModelName(), companyId)));
    }

    @SuppressWarnings("unchecked")
    public Optional<List<SMSBlackListEntity>> loadByCompanyId(Integer companyId) {
        final String cache_key = String.format("%s_company_%s", getModelName(), companyId);
        if (getCache().isPresent()) {
            List<SMSBlackListEntity> list = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(list)) return Optional.of(list);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        Optional<List<SMSBlackListEntity>> list = super.queryForEntities("loadAllByCompany", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByCompany(%s) size is %s", companyId, list.map(List::size).orElse(0)));
        getCache().ifPresent(c -> list.ifPresent(l -> c.put(cache_key, l)));
        return list;
    }

    @Override
    protected RowMapper<SMSBlackListEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<SMSBlackListEntity> {
        @Override
        public SMSBlackListEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSBlackListEntity(res.getString("id"), res);
        }
    }
}
