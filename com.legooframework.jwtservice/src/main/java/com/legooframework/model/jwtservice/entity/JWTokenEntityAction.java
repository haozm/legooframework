package com.legooframework.model.jwtservice.entity;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.codec.binary.Base64;
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
import java.util.concurrent.TimeUnit;

public class JWTokenEntityAction extends BaseEntityAction<JWTokenEntity> {

    private static final Logger logger = LoggerFactory.getLogger(JWTokenEntityAction.class);

    private final Cache<String, JWTokenEntity> tokenCache;

    public JWTokenEntityAction() {
        super(null);
        this.tokenCache = Caffeine.newBuilder().initialCapacity(512)
                .maximumSize(2048).expireAfterAccess(4, TimeUnit.HOURS)
                .removalListener(new RemovalListenerImpl()).build();
    }

//    String loginByWeb(String loginName, String host) {
//        return insert(loginName, host, 1);
//    }
//
//    String loginByMobile(String loginName, String host) {
//        return insert(loginName, host, 2);
//    }

    public Optional<JWToken> touched(String loginToken) {
        Optional<JWTokenEntity> clone = loadJWToken(loginToken);
        clone.ifPresent(JWTokenEntity::touched);
        return clone.map(JWTokenEntity::getJwToken);
    }

    public void destroy() {
        Map<String, JWTokenEntity> map = tokenCache.asMap();
        List<JWTokenEntity> list = Lists.newArrayList();
        map.forEach((k, v) -> {
            if (v.isExpired()) {
                v.expired();
                list.add(v);
            }
        });
        if (CollectionUtils.isNotEmpty(list)) {
            super.batchUpdate("batch_logout", (ps, token) -> {
                ps.setObject(1, LocalDateTime.now().toDate());
                ps.setObject(2, token.getRemark());
                ps.setObject(3, token.getId());
            }, list);
        }
    }

    @Override
    public Optional<JWTokenEntity> findById(Object id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        return super.queryForEntity("findById", params, getRowMapper());
    }

    public void logout(String loginToken) {
        Optional<JWTokenEntity> exits = loadJWToken(loginToken);
        if (exits.isPresent() && exits.get().isLogout()) return;
        final String cache_key = String.format("%s_token_%s", getModelName(), loginToken);
        exits.ifPresent(x -> {
            x.logout();
            updateToken(x);
            tokenCache.invalidate(cache_key);
        });
    }

    public String insert(String loginName, String host, int channel) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(loginName));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(host));
        JWTokenEntity jwTokenEntity = new JWTokenEntity(loginName, host, channel);
        super.updateAction(jwTokenEntity, "insert");
        if (logger.isDebugEnabled())
            logger.debug(String.format("insert(%s,%s,%s) ok,Token:%s", loginName, host, channel, jwTokenEntity.getLoginToken()));
        loadJWToken(jwTokenEntity.getLoginToken());
        return jwTokenEntity.getLoginToken();
    }

    private void updateToken(JWTokenEntity jwTokenEntity) {
        if (jwTokenEntity == null) return;
        super.updateAction(jwTokenEntity, "update");
        if (logger.isDebugEnabled())
            logger.debug(String.format("updateToken(%s) ", jwTokenEntity));
    }

    public Optional<List<JWTokenEntity>> loadEnabledTokenByLoginName(String loginName, int channel) {
        if (Strings.isNullOrEmpty(loginName)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("loginName", loginName);
        params.put("loginChannel", channel);
        Optional<List<JWTokenEntity>> jwTokens = super.queryForEntities("loadEnabledTokenByLoginName", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadEnabledTokenByLoginName(%s,%s) return %s", loginName, channel,
                    jwTokens.orElse(null)));
        return jwTokens;
    }

    public void batchLogout(Collection<JWTokenEntity> tokens) {
        if (CollectionUtils.isEmpty(tokens)) return;
        List<String> cacheKey = Lists.newArrayList();
        tokens.forEach(x -> {
            x.logout();
            cacheKey.add(String.format("%s_token_%s", getModelName(), x.getLoginToken()));
        });
        super.batchUpdate("batch_logout", (ps, token) -> {
            ps.setObject(1, LocalDateTime.now().toDate());
            ps.setObject(2, token.getRemark());
            ps.setObject(3, token.getId());
        }, tokens);
        cacheKey.forEach(tokenCache::invalidate);
    }

    Optional<JWTokenEntity> loadJWToken(String loginToken) {
        if (Strings.isNullOrEmpty(loginToken)) return Optional.empty();
        Preconditions.checkState(Base64.isBase64(loginToken), "非法的Token 格式，请检查数据...");
        final String cache_key = String.format("%s_token_%s", getModelName(), loginToken);
        JWTokenEntity jwTokenEntity = tokenCache.getIfPresent(cache_key);
        if (jwTokenEntity == null) {
            Map<String, Object> params = Maps.newHashMap();
            params.put("loginToken", loginToken);
            Optional<JWTokenEntity> jwToken = super.queryForEntity("loadJWToken", params, getRowMapper());
            if (logger.isDebugEnabled())
                logger.debug(String.format("loadJWToken(%s) return %s", loginToken, jwToken.orElse(null)));
            jwToken.ifPresent(t -> tokenCache.put(cache_key, t));
            return jwToken;
        }
        return Optional.of(jwTokenEntity);
    }

    @Override
    protected RowMapper<JWTokenEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<JWTokenEntity> {
        @Override
        public JWTokenEntity mapRow(ResultSet res, int i) throws SQLException {
            return new JWTokenEntity(res.getString("id"), res);
        }
    }

    class RemovalListenerImpl implements RemovalListener<String, JWTokenEntity> {
        @Override
        public void onRemoval(String key, JWTokenEntity token, RemovalCause removalCause) {
            if (token != null && !token.isLogout()) {
                if (token.isExpired()) token.expired();
                updateToken(token);
            }
        }
    }
}
