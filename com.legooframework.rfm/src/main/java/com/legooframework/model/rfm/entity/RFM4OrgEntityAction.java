package com.legooframework.model.rfm.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.SystemlogEntity;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RFM4OrgEntityAction extends BaseEntityAction<RFM4OrgEntity> {

    private static final Logger logger = LoggerFactory.getLogger(RFM4OrgEntityAction.class);

    public RFM4OrgEntityAction() {
        super("RFM4OrgEntity", null);
    }

    public void savaOrUpdateStoreRFM(LoginUserContext user, StoreEntity store, int valType,
                                     int r1, int r2, int r3, int r4,
                                     int f1, int f2, int f3, int f4,
                                     int m1, int m2, int m3, int m4) {
        RFM4OrgEntity storeRfm = new RFM4OrgEntity(user, store, new RVal(r1, r2, r3, r4),
                new FVal(f1, f2, f3, f4), new MVal(m1, m2, m3, m4), valType);
        Optional<RFM4OrgEntity> extis = findById(store.getCompanyId().or(-1), -1, store.getId());
        if (extis.isPresent() && extis.get().equals(storeRfm)) return;
        getJdbc().update(getExecSql("savaOrUpdateRFM", null), storeRfm.toMap());
        logProxy(SystemlogEntity.update(this.getClass(), "savaOrUpdateStoreRFM",
                String.format("修改或新增门店RFM %s", storeRfm.toString()), "RFM操作"));
    }

    public void batchReWriteStoreRFM(final LoginUserContext user, final int valType,
                                     final int r1, final int r2, final int r3, final int r4,
                                     final int f1, final int f2, final int f3, final int f4,
                                     final int m1, final int m2, final int m3, final int m4,
                                     Collection<StoreEntity> stores) {
//        company_id, store_id,
//        r_v1, r_v2, r_v3, r_v4,
//        f_v1, f_v2, f_v3, f_v4,
//        m_v1, m_v2, m_v3, m_v4,
//        val_type, tenant_id, creator,      createTime,         editor
        getJdbcTemplate().batchUpdate(getExecSql("batchRewriteStoreRFM", null), stores, 512, (ps, st) -> {
            ps.setObject(1, st.getCompanyId().or(-1));
            ps.setObject(2, st.getId());

            ps.setObject(3, r1);
            ps.setObject(4, r2);
            ps.setObject(5, r3);
            ps.setObject(6, r4);

            ps.setObject(7, f1);
            ps.setObject(8, f2);
            ps.setObject(9, f3);
            ps.setObject(10, f4);

            ps.setObject(11, m1);
            ps.setObject(12, m2);
            ps.setObject(13, m3);
            ps.setObject(14, m4);

            ps.setObject(15, valType);
            ps.setObject(16, st.getCompanyId().or(-1));
            ps.setObject(17, user.getUserId());
        });
        List<Integer> ids = stores.stream().map(BaseEntity::getId).collect(Collectors.toList());
        logProxy(SystemlogEntity.update(this.getClass(), "batchReWriteStoreRFM",
                String.format("批量重写RFM %s", ids), "RFM操作"));
        if (logger.isDebugEnabled())
            logger.debug("batchReWriteStoreRFM(....,store's size is %s)", stores.size());
    }

    public void savaOrUpdateComOrOrgRFM(LoginUserContext user, OrganizationEntity org, int valType,
                                        int r1, int r2, int r3, int r4,
                                        int f1, int f2, int f3, int f4,
                                        int m1, int m2, int m3, int m4) {
        RFM4OrgEntity comRfm = new RFM4OrgEntity(user, org, new RVal(r1, r2, r3, r4),
                new FVal(f1, f2, f3, f4),
                new MVal(m1, m2, m3, m4), valType);
        Optional<RFM4OrgEntity> extis = loadComOrOrgRFM(org);
        if (extis.isPresent() && extis.get().equals(comRfm)) return;
        getJdbc().update(getExecSql("savaOrUpdateRFM", null), comRfm.toMap());
        logProxy(SystemlogEntity.update(this.getClass(), "savaOrUpdateCompanyRFM",
                String.format("修改或新增公司RFM %s", comRfm), "RFM操作"));
    }

    public Optional<List<RFM4OrgEntity>> loadAllStoreRFM(OrganizationEntity company) {
        Preconditions.checkNotNull(company, "入参 OrganizationEntity company 不可以空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", company.getId());
        List<RFM4OrgEntity> res = getJdbc().query(getExecSql("loadAllStoreEFM", null), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllStoreRFM(%s) res size is %s", company.getId(),
                    CollectionUtils.isEmpty(res) ? 0 : res.size()));
        return Optional.ofNullable(CollectionUtils.isEmpty(res) ? null : res);
    }

    public Optional<RFM4OrgEntity> loadStoreRFM(StoreEntity store) {
        Preconditions.checkNotNull(store, "入参 StoreEntity store 不可以空值...");
        return findById(store.getCompanyId().or(-1), -1, store.getId());
    }

    public Optional<RFM4OrgEntity> loadComOrOrgRFM(OrganizationEntity com) {
        Preconditions.checkNotNull(com, "入参OrganizationEntity com不可以空值...");
        if (com.isCompany()) return findById(com.getId(), -1, -1);
        return findById(com.getMyCompanyId(), com.getId(), -1);
    }

    private Optional<RFM4OrgEntity> findById(Integer companyId, Integer orgId, Integer storeId) {
        Preconditions.checkNotNull(companyId, "入参Integer companyId 不可以空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("orgId", orgId);
        try {
            RFM4OrgEntity res = getJdbc().queryForObject(getExecSql("findById", null), params, new RowMapperImpl());
            if (logger.isDebugEnabled())
                logger.debug(String.format("findById(%s,%s,%s) res= %s", companyId, orgId, storeId, res));
            return Optional.ofNullable(res);
        } catch (EmptyResultDataAccessException e) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("findById(%s,%s,%s) res= %s", companyId, orgId, storeId, null));
            return Optional.empty();
        }
    }

    @Override
    protected ResultSetExtractor<RFM4OrgEntity> getResultSetExtractor() {
        return null;
    }

    class RowMapperImpl implements RowMapper<RFM4OrgEntity> {
        @Override
        public RFM4OrgEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    private RFM4OrgEntity buildByResultSet(ResultSet resultSet) throws SQLException {
//        Object createUserId, Date createTime, Integer companyId, Integer storeId, int valType,
//        int r1, int r2, int r3, int r4,
//        int f1, int f2, int f3, int f4,
//        int m1, int m2, int m3, int m4
        return new RFM4OrgEntity(
                resultSet.getObject("creatorId"), resultSet.getDate("createTime"),
                resultSet.getInt("companyId"), resultSet.getInt("orgId"), resultSet.getInt("storeId"),
                resultSet.getInt("type"),
                resultSet.getInt("rV1"), resultSet.getInt("rV2"), resultSet.getInt("rV3"),
                resultSet.getInt("rV4"),
                resultSet.getInt("fV1"), resultSet.getInt("fV2"), resultSet.getInt("fV3"),
                resultSet.getInt("fV4"),
                resultSet.getInt("mV1"), resultSet.getInt("mV2"), resultSet.getInt("mV3"),
                resultSet.getInt("mV4"));
    }
}
