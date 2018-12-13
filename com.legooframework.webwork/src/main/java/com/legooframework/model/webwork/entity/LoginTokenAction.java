package com.legooframework.model.webwork.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class LoginTokenAction extends BaseEntityAction<LoginTokenEntity> {

    public LoginTokenAction() {
        super("LoingTokenCache");
    }

    @Override
    protected void cacheEntity(LoginTokenEntity entity) {
        super.cacheEntity(entity);
        if (entity == null) return;
        getCache().ifPresent(c -> c.put(String.format("%s_full_%s", getModelName(), entity.getFullToken()), entity));
    }

    @Override
    protected void evictEntity(LoginTokenEntity entity) {
        super.evictEntity(entity);
        if (entity == null) return;
        getCache().ifPresent(c -> c.evict(String.format("%s_full_%s", getModelName(), entity.getFullToken())));
    }

    public String insert(LoginTokenEntity loginToken) {
        Preconditions.checkNotNull(loginToken, "入参LoginTokenEntity loginToken不可以为空...");
        Optional<LoginTokenEntity> exits_token = findLastOnlineTokenByAccount(loginToken.getAccountNo());
        if (exits_token.isPresent() && exits_token.get().equals(loginToken)) {
            return exits_token.get().getId();
        }
        int res = super.updateAction(loginToken, "insert");
        Preconditions.checkState(1 == res, "持久化 %s 失败...", loginToken);
        return loginToken.getId();
    }

    public Optional<LoginTokenEntity> findLastOnlineTokenByAccount(String accountNo) {
        if (Strings.isNullOrEmpty(accountNo)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("accountNo", accountNo);
        Optional<LoginTokenEntity> opt = super.queryForEntity("findLastOnlineTokenByAccount", params, getRowMapper());
        getCache().ifPresent(c -> opt.ifPresent(this::cacheEntity));
        opt.ifPresent(LoginTokenEntity::remarksLastTime);
        return opt;
    }

    private Optional<LoginTokenEntity> findByToken(String fullToken) {
        if (Strings.isNullOrEmpty(fullToken)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("fullToken", fullToken);
        Optional<LoginTokenEntity> opt = super.queryForEntity("findByToken", params, getRowMapper());
        getCache().ifPresent(c -> opt.ifPresent(this::cacheEntity));
        return opt;
    }

    public Optional<LoginTokenEntity> findOnlineByToken(String token_val) {
        Optional<LoginContext> loginContext = LoginContextHolder.getIfExits();
        if (!loginContext.isPresent()) LoginContextHolder.setAnonymousCtx();
        Optional<LoginTokenEntity> token = findById(token_val);
        if (!token.isPresent()) token = findByToken(token_val);
        if (token.isPresent()) {
            if (token.get().isLogout()) return Optional.empty();
            return token;
        }
        return Optional.empty();
    }

    public void logout(String token) {
        Optional<LoginTokenEntity> exits = findById(token);
        if (!exits.isPresent()) return;
        if (exits.get().isLogout()) return;
        super.updateAction(exits.get(), "logout");
        evictEntity(exits.get());
    }

    public int totalOnlineNum() {
        Optional<Integer> onlineNum = queryForObject(getStatementFactory(), getModelName(), "totalOnlineNum",
                null, Integer.class);
        return onlineNum.isPresent() ? onlineNum.get() : 0;
    }

    @Override
    protected RowMapper<LoginTokenEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<LoginTokenEntity> {
        @Override
        public LoginTokenEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new LoginTokenEntity(res.getString("id"), res);
        }
    }

}
