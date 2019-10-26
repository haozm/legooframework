package com.legooframework.model.insurance.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class MemberEntityAction extends BaseEntityAction<MemberEntity> {

    private static final Logger logger = LoggerFactory.getLogger(MemberEntityAction.class);

    public MemberEntityAction() {
        super(null);
    }

    public Optional<MemberEntity> findByIDCard(String cardID) {
        Preconditions.checkState(!Strings.isNullOrEmpty(cardID), "身份证不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("cardId", cardID);
        Optional<MemberEntity> optional = super.queryForEntity("findByIDcard", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByIDCard(%s) return %s", cardID, optional.orElse(null)));
        return optional;
    }

    public MemberEntity insert(String name, String cardId, String phone, String mobile, LocalDate birthday, int sex,
                               int education, int height, int weight, String familyAddr, String workAddr, String email) {
        MemberEntity example = new MemberEntity(name, cardId, phone, mobile, birthday, sex, education,
                height, weight, familyAddr, workAddr, email);
        Optional<MemberEntity> exits = super.queryForEntity("findByIDcard", example.toParamMap(), getRowMapper());
        if (exits.isPresent()) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("存在会员%s,忽略本次新增,直接返回.", example));
            return exits.get();
        }
        super.updateAction(example, "insert");
        if (logger.isDebugEnabled())
            logger.debug(String.format("新增会员%s成功.", example));
        return example;
    }

    public void change(Integer memberId, String name, String cardId, String phone, String mobile, LocalDate birthday,
                       int sex, int education, int height, int weight, String familyAddr, String workAddr, String email) {
        Optional<MemberEntity> example = findById(memberId);
        Preconditions.checkState(example.isPresent(), "不存在ID=%s 对应的用户信息...");
        Optional<MemberEntity> clone = example.get().change(name, cardId, phone, mobile, birthday, sex,
                education, height, weight, familyAddr, workAddr, email);
        clone.ifPresent(x -> {
            super.updateAction(x, "update");
            if (logger.isDebugEnabled())
                logger.debug(String.format("编辑会员%s成功.", example));
        });
    }

    @Override
    protected RowMapper<MemberEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<MemberEntity> {
        @Override
        public MemberEntity mapRow(ResultSet res, int i) throws SQLException {
            return new MemberEntity(res.getInt("id"), res);
        }
    }
}
