package com.legooframework.model.insurance.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BankCardEntityAction extends BaseEntityAction<BankCardEntity> {

    private static final Logger logger = LoggerFactory.getLogger(BankCardEntityAction.class);
    private SimpleJdbcInsert jdbcInsert;

    public BankCardEntityAction() {
        super(null);
    }

    public BankCardEntity insert(MemberEntity member, String bankType, String account) {
        BankCardEntity entity = new BankCardEntity(member, bankType, account);
        Optional<BankCardEntity> exits = super.queryForEntity("findByAccount", entity.toParamMap(), getRowMapper());
        if (exits.isPresent()) {
            Preconditions.checkState(exits.get().getMemberId().equals(member.getId()), "数据异常，卡号对应的户主不一致...");
            if (logger.isDebugEnabled())
                logger.debug(String.format("Insert 银行卡信息含有 %s,忽略本次新增,直接返回.", account));
            return exits.get();
        }
        this.insert(entity);
        //super.updateAction(, "insert");
        if (logger.isDebugEnabled())
            logger.debug(String.format("新增银行卡%s成功.", entity));
        return entity;
    }

    public Optional<List<BankCardEntity>> findByMember(MemberEntity member) {
        Preconditions.checkNotNull(member);
        Map<String, Object> params = Maps.newHashMap();
        params.put("memberId", member.getId());
        Optional<List<BankCardEntity>> reslist = queryForEntities("findByMember", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByMember(%s) and res = %s", member.getName(), reslist.orElse(null)));
        return reslist;
    }

    private void insert(BankCardEntity ins) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", ins.getId());
        params.put("member_id", ins.getMemberId());
        params.put("bank_type", ins.getBankType());
        params.put("account", ins.getAccount());
        this.jdbcInsert.execute(params);
    }

    @Override
    protected void initTemplateConfig() {
        super.initTemplateConfig();
        this.jdbcInsert = new SimpleJdbcInsert(getJdbcTemplate()).withTableName("INSURANCE_BANK_CARD");
        this.jdbcInsert.usingColumns("id", "member_id", "bank_type", "account");
        this.jdbcInsert.compile();
    }

    @Override
    protected RowMapper<BankCardEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<BankCardEntity> {
        @Override
        public BankCardEntity mapRow(ResultSet res, int i) throws SQLException {
            return new BankCardEntity(res.getInt("id"), res);
        }
    }
}
