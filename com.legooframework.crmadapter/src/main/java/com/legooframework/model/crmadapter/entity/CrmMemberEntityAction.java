package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
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

public class CrmMemberEntityAction extends BaseEntityAction<CrmMemberEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CrmMemberEntityAction.class);

    public CrmMemberEntityAction() {
        super("CrmAdapterCache");
    }

    /**
     * @param store
     * @return
     */
    public Optional<List<CrmMemberEntity>> loadAllByStore(CrmStoreEntity store) {
        Preconditions.checkNotNull(store, "会员所属门店不可以为空...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId());
        Optional<List<CrmMemberEntity>> members = super.queryForEntities("loadAllByStore", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByStore(%s) size is %s", store.getId(), members.map(List::size).orElse(0)));
        return members;
    }

    public Optional<List<CrmMemberEntity>> loadAllEnbaledByStore(CrmStoreEntity store) {
        Optional<List<CrmMemberEntity>> list = loadAllByStore(store);
        return list.map(crmMembers -> crmMembers.stream().
                filter(CrmMemberEntity::isEffectiveFlag).collect(Collectors.toList()));
    }

    public Optional<List<CrmMemberEntity>> loadByCompany(CrmOrganizationEntity company, Collection<Integer> memberIds) {
        Preconditions.checkNotNull(company);
        if (CollectionUtils.isEmpty(memberIds)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("memberIds", memberIds);
        params.put("companyId", company.getId());
        Optional<List<CrmMemberEntity>> members = super.queryForEntities("loadByCompany", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByCompany(%s) size is %s", company.getId(), members.map(List::size).orElse(0)));
        return members;
    }

    public Optional<CrmMemberEntity> loadMemberByCompany(CrmOrganizationEntity company, Integer memberId) {
        if (memberId == null || memberId <= 0) return Optional.empty();
        Preconditions.checkNotNull(company);
        Map<String, Object> params = Maps.newHashMap();
        params.put("memberId", memberId);
        params.put("companyId", company.getId());
        Optional<CrmMemberEntity> member = super.queryForEntity("loadMemberByCompany", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadMemberByCompany(%s,%s) return is %s", company.getId(), memberId, member.orElse(null)));
        return member;
    }


    @Override
    protected RowMapper<CrmMemberEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<CrmMemberEntity> {
        @Override
        public CrmMemberEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new CrmMemberEntity(resultSet.getInt("id"), resultSet);
        }
    }
}
