package com.legooframework.model.rfm.entity;

import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.MemberEntity;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MemberRFMEntityAction extends BaseEntityAction<MemberRFMEntity> {

    private static final Logger logger = LoggerFactory.getLogger(MemberRFMEntityAction.class);
    private static Comparator<MemberRFMEntity> ordering = Comparator
            .comparingLong(rfm -> rfm.getCreateTime().getTime());


    public MemberRFMEntityAction() {
        super("MemberRFMEntity", null);
    }

    public Optional<MemberRFMEntity> findCurMember(MemberEntity member) {
        return findByMember(member.getCompanyId(), member.getId());
    }

    /**
     * 按照 时间 倒叙输出
     *
     * @param member
     * @return
     */
    public Optional<List<MemberRFMEntity>> findAllMember(MemberEntity member) {
        Optional<List<MemberRFMEntity>> list = findAllByMember(member.getCompanyId(), member.getId());
        if (!list.isPresent()) return Optional.empty();
        List<MemberRFMEntity> exits = Lists.newArrayList();
        list.ifPresent(x -> x.forEach(v -> {
            if (!exits.contains(v)) exits.add(v);
        }));
        return Optional.of(exits);
    }


    Optional<MemberRFMEntity> findByMember(Integer companyId, Integer memberId) {
        Preconditions.checkNotNull(companyId, "查询RFM值 公司ID不可以为空...");
        Preconditions.checkNotNull(memberId, "查询RFM值 会员不可以为空...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("memberId", memberId);
        try {
            MemberRFMEntity res = getJdbc().queryForObject(getExecSql("findCurMemberRFM", null), params, new RowMapperImpl());
            if (logger.isDebugEnabled())
                logger.debug(String.format("findByMember(%s,%s) res= %s", companyId, memberId, res));
            return Optional.ofNullable(res);
        } catch (EmptyResultDataAccessException e) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("findByMember(%s,%s) res= %s", companyId, memberId, null));
            return Optional.empty();
        }
    }

    Optional<List<MemberRFMEntity>> findAllByMember(Integer companyId, Integer memberId) {
        Preconditions.checkNotNull(companyId, "查询RFM值 公司ID不可以为空...");
        Preconditions.checkNotNull(memberId, "查询RFM值 会员不可以为空...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("memberId", memberId);
        List<MemberRFMEntity> res = getJdbc().query(getExecSql("findAllMemberRFM", null), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findAllByMember(%s,%s) res= %s", companyId, memberId,
                    CollectionUtils.isEmpty(res) ? 0 : res.size()));
        return Optional.ofNullable(CollectionUtils.isEmpty(res) ? null : res);
    }

    @Override
    protected ResultSetExtractor<MemberRFMEntity> getResultSetExtractor() {
        return null;
    }

    class RowMapperImpl implements RowMapper<MemberRFMEntity> {
        @Override
        public MemberRFMEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    private MemberRFMEntity buildByResultSet(ResultSet resultSet) throws SQLException {
//        Integer id, Integer companyId, Integer memberId,
//                    int recencyStore, int frequencyStore, int monetaryStore, int recencyCom,
//                    int frequencyCom, int monetaryCom,
//                    Date createTime
        return new MemberRFMEntity(0, resultSet.getInt("companyId"), resultSet.getInt("memberId"),
                resultSet.getInt("recencyStore"), resultSet.getInt("frequencyStore"), resultSet.getInt("monetaryStore"),
                resultSet.getInt("recencyCom"), resultSet.getInt("frequencyCom"), resultSet.getInt("monetaryCom"),
                resultSet.getDate("createTime"));
    }
}
