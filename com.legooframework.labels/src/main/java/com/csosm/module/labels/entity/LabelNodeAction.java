package com.csosm.module.labels.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LabelNodeAction extends BaseEntityAction<LabelNodeEntity> {

    private static final Logger logger = LoggerFactory.getLogger(LabelNodeAction.class);

    private static Splitter.MapSplitter splitter = Splitter.on(',').withKeyValueSeparator('=');

    @Override
    public Optional<LabelNodeEntity> findById(Object id) {
        throw new UnsupportedOperationException("实体LabelNodeEntity不支持该操作...");
    }

    public Optional<LabelNodeEntity> findByMixId(Object id, StoreEntity store, OrganizationEntity company) {
        if (null == id) return Optional.absent();
        String id_str = String.valueOf(id);
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        if (StringUtils.startsWith(id_str, "100") || StringUtils.equals("200", id_str)) {
            params.put("storeId", -1);
            Preconditions.checkNotNull(company, "公司不可以为空....");
            params.put("companyId", company.getId());
        } else if (StringUtils.startsWith(id_str, "200")) {
            Preconditions.checkNotNull(store, "门店不可以为空....");
            params.put("storeId", store.getId());
            Preconditions.checkState(store.getCompanyId().isPresent());
            params.put("companyId", store.getCompanyId().get());
        }
        try {
            LabelNodeEntity res = getJdbc().queryForObject(getExecSql("findByMixId", null), params, new RowMapperImpl());
            return Optional.fromNullable(res);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.absent();
        }
    }

    public Optional<List<LabelNodeEntity>> loadEnabledByCompany(OrganizationEntity company) {
        Preconditions.checkNotNull(company, "OrganizationEntity company 不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", company.getId());
        params.put("storeId", -1);
        params.put("enbale", 1);
        List<LabelNodeEntity> exits = getJdbc().query(getExecSql("load_by_com", null), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(exits) ? null : exits);
    }

    public Optional<List<LabelNodeEntity>> loadEnabledByStore(StoreEntity store) {
        Preconditions.checkNotNull(store, "StoreEntity store 不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", store.getCompanyId().or(-1));
        params.put("storeId", store.getId());
        params.put("enbale", 1);
        List<LabelNodeEntity> exits = getJdbc().query(getExecSql("load_by_store", null), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(exits) ? null : exits);
    }

    public Optional<List<LabelNodeEntity>> loadAllByCompany(OrganizationEntity company) {
        Preconditions.checkNotNull(company, "OrganizationEntity company 不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", company.getId());
        params.put("storeId", -1);
        List<LabelNodeEntity> exits = getJdbc().query(getExecSql("load_by_com", null), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(exits) ? null : exits);
    }

    public Long addStoreLabel(StoreEntity store, String name, String desc) {
        Preconditions.checkNotNull(store, "门店不可以为空");
        Optional<List<LabelNodeEntity>> exits = loadEnabledByStore(store);
        long label_id = 200100L;
        if (exits.isPresent()) {
            for (LabelNodeEntity x : exits.get()) {
                Preconditions.checkArgument(!StringUtils.equals(name, x.getName()), "当前门店存在%s的标签定义...", name);
            }
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().or(-1));
        Long max_id = getJdbc().queryForObject(
                "SELECT MAX(id) FROM user_label_tree WHERE company_id = :companyId AND store_id = :storeId", params, Long.class);
        label_id = Longs.max(label_id, max_id == null ? 200100L : max_id);
        LabelNodeEntity new_instance = LabelNodeEntity.txtLabel(label_id + 1, 200L, null, store, name, desc);
        int res = getJdbc().update(getExecSql("insert", null), new_instance.toMap());
        Preconditions.checkState(1 == res, "持久化标签节点 %s 失败", new_instance);
        return new_instance.getId();
    }

    public Long insertTxtLabel(Long pId, OrganizationEntity company, StoreEntity store, String name, String desc) {
        Optional<LabelNodeEntity> parent = findByMixId(pId, store, company);
        if (100L == pId.longValue() && !parent.isPresent()) {
            LabelNodeEntity labelNode = LabelNodeEntity.txtRootLabel(company);
            getJdbc().update(getExecSql("insert", null), labelNode.toMap());
            parent = Optional.of(labelNode);
        }
        Preconditions.checkState(parent.isPresent(), "id=%s对应的节点信息不存在...", pId);
        LabelNodeEntity new_instance = LabelNodeEntity.txtLabel(parent.get().getNextChildId(),
                parent.get(), company, store, name, desc);
        LabelNodeEntity exits = getJdbc().query(getExecSql("exits_by_com", null), new_instance.toMap(), getResultSetExtractor());
        Preconditions.checkState(null == exits, "已经存在%s对应的自定义标签", name);
        int res = getJdbc().update(getExecSql("insert", null), new_instance.toMap());
        Preconditions.checkState(1 == res, "持久化标签节点 %s 失败", new_instance);
        return new_instance.getId();
    }

    public Long insertNumRangeLabel(Long pId, OrganizationEntity company, StoreEntity store, String name, String desc, Double mix, Double max) {
        Optional<LabelNodeEntity> parent = findByMixId(pId, store, company);
        Preconditions.checkState(parent.isPresent(), "id=%s对应的节点信息不存在...", pId);
        LabelNodeEntity new_instance = LabelNodeEntity.numRangeLabel(parent.get().getNextChildId(),
                parent.get(), company, store, name, mix, max, desc);
        int res = getJdbc().update(getExecSql("insert", null), new_instance.toMap());
        Preconditions.checkState(1 == res, "持久化标签节点 %s 失败", new_instance);
        return new_instance.getId();
    }

    public Long insertDateRangeLabel(Long pId, OrganizationEntity company, StoreEntity store, String name, String desc, DateTime mix, DateTime max) {
        Optional<LabelNodeEntity> parent = findByMixId(pId, store, company);
        Preconditions.checkState(parent.isPresent(), "id=%s对应的节点信息不存在...", pId);
        LabelNodeEntity new_instance = LabelNodeEntity.dateTimeRangeLabel(parent.get().getNextChildId(),
                parent.get(), company, store, name, mix, max, desc);
        int res = getJdbc().update(getExecSql("insert", null), new_instance.toMap());
        Preconditions.checkState(1 == res, "持久化标签节点 %s 失败", new_instance);
        return new_instance.getId();
    }

    public Long insertValueLabel(Long pId, OrganizationEntity company, StoreEntity store, String name, Object value, String desc) {
        Preconditions.checkNotNull(value, "标签值不可以为空...");
        Optional<LabelNodeEntity> parent = findByMixId(pId, store, company);
        Preconditions.checkState(parent.isPresent(), "id=%s对应的节点信息不存在...", pId);
        LabelNodeEntity new_instance;
        if (NumberUtils.isNumber(value.toString())) {
            new_instance = LabelNodeEntity.numberLabel(parent.get().getNextChildId(),
                    parent.get(), company, store, name, Double.valueOf(value.toString()), desc);
        } else {
            try {
                Date date = DateUtils.parseDate(value.toString(), "yyyy-MM-dd HH:mm:ss");
                new_instance = LabelNodeEntity.dateTimeLabel(parent.get().getNextChildId(),
                        parent.get(), company, store, name, new DateTime(date), desc);
            } catch (Exception e) {
                logger.error(String.format("DateUtils.parseDate(%s, yyyy-MM-dd HH:mm:ss) has error", value));
                throw new RuntimeException(e);
            }
        }
        int res = getJdbc().update(getExecSql("insert", null), new_instance.toMap());
        Preconditions.checkState(1 == res, "持久化标签节点 %s 失败", new_instance);
        return new_instance.getId();
    }

    public LabelNodeAction() {
        super("LabelNodeEntity", null);
    }

    public void enabled(Long label_id, LoginUserContext user) {
        Preconditions.checkNotNull(label_id, "Long lobelId 不可以为空值...");
        Optional<LabelNodeEntity> exit = findByMixId(label_id, user.getStore().orNull(), user.getCompany().orNull());
        Preconditions.checkState(exit.isPresent(), "Id=%s 对应的实体不存在...");
        Optional<LabelNodeEntity> clone = exit.get().enabled();
        if (clone.isPresent()) {
            getJdbc().update(getExecSql("update_status", null), clone.get().toMap());
        }
    }

    public void disbaled(Long label_id, LoginUserContext user) {
        Preconditions.checkNotNull(label_id, "Long lobelId 不可以为空值...");
        Optional<LabelNodeEntity> exit = findByMixId(label_id, user.getStore().orNull(), user.getCompany().orNull());
        Preconditions.checkState(exit.isPresent(), "Id=%s 对应的实体不存在...");
        Optional<LabelNodeEntity> clone = exit.get().disabled();
        if (clone.isPresent()) {
            getJdbc().update(getExecSql("update_status", null), clone.get().toMap());
        }
    }

    public void change(Long label_id, String name, String desc, LoginUserContext user) {
        Preconditions.checkNotNull(label_id, "Long lobelId 不可以为空值...");
        Optional<LabelNodeEntity> exit = findByMixId(label_id, user.getStore().orNull(), user.getCompany().orNull());
        Preconditions.checkState(exit.isPresent(), "Id=%s 对应的实体不存在...");
        Optional<LabelNodeEntity> clone = exit.get().changeName(name, desc);
        if (clone.isPresent()) {
            getJdbc().update(getExecSql("update_txt_base", null), clone.get().toMap());
        }
    }

    public void delete(Long label_id, LoginUserContext user) {
        Preconditions.checkNotNull(label_id, "Long lobelId 不可以为空值...");
        Optional<LabelNodeEntity> exit = findByMixId(label_id, user.getStore().orNull(), user.getCompany().orNull());
        Preconditions.checkState(exit.isPresent(), "Id=%s 对应的实体不存在...");
        Preconditions.checkState(!exit.get().hasChild(), "当前节点有下级节点，不允许删除...");
        getJdbc().update(getExecSql("delete", null), exit.get().toMap());
    }

    @Override
    protected ResultSetExtractor<LabelNodeEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<LabelNodeEntity> {

        @Override
        public LabelNodeEntity extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            return resultSet.next() ? buildByResultSet(resultSet) : null;
        }
    }

    private LabelNodeEntity buildByResultSet(ResultSet resultSet) {
        try {
            String ctx = resultSet.getString("labelCtx");
            return new LabelNodeEntity(resultSet, Strings.isNullOrEmpty(ctx) ? null : splitter.split(ctx));
        } catch (Exception e) {
            logger.error("还原 LabelNodeEntity from db has error...", e);
            throw new RuntimeException(e);
        }
    }

    class RowMapperImpl implements RowMapper<LabelNodeEntity> {
        @Override
        public LabelNodeEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            try {
                String ctx = resultSet.getString("labelCtx");
                return new LabelNodeEntity(resultSet, Strings.isNullOrEmpty(ctx) ? null : splitter.split(ctx));
            } catch (Exception e) {
                logger.error("还原 LabelNodeEntity from db has error...", e);
                throw new RuntimeException(e);
            }

        }
    }

}
