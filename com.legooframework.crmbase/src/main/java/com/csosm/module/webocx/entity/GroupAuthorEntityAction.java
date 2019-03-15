package com.csosm.module.webocx.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupAuthorEntityAction extends BaseEntityAction<GroupAuthorEntity> {

    private static final Logger logger = LoggerFactory.getLogger(GroupAuthorEntityAction.class);

    public GroupAuthorEntityAction() {
        super("GroupAuthorEntity", "authorCache");
    }

    public boolean isActived(LoginUserContext loginUser, PageDefinedDto pageDefinedDto) {
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前用户缺乏公司信息...");
        OrganizationEntity company = loginUser.getCompany().get();
        Optional<List<GroupAuthorEntity>> groupAuthors = Optional.absent();
        if (loginUser.getMaxPowerRole().isPresent() && (loginUser.getMaxPowerRole().get().isShoppingGuide() ||
                loginUser.getMaxPowerRole().get().isStoreManager())) {
            Preconditions.checkState(loginUser.getStore().isPresent(), "当前用户门店信息...");
            StoreEntity store = loginUser.getStore().get();
            groupAuthors = findAllByStore(store);
        } else if (loginUser.getMaxPowerRole().isPresent() && loginUser.getMaxPowerRole().get().isLeader()) {
            groupAuthors = findAllByCom(company);
        }

        if (!groupAuthors.isPresent()) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("NOT ANY CONFIG FOR GroupAuthorEntity GOR %s", pageDefinedDto.getFullName()));
            return true;
        }
        boolean res = false;
        for (GroupAuthorEntity $it : groupAuthors.get()) {
            if ($it.equalsPage(pageDefinedDto) && $it.isDisAnabled()) {
                res = true;
                break;
            }
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("CONFIG FOR GroupAuthorEntity GOR %s res is %s", pageDefinedDto.getFullName(), res));
        return res;
    }

    public Optional<List<GroupAuthorEntity>> findAllByCom(OrganizationEntity company) {
        return findAllByComId(company.getId());
    }

    @Override
    public Optional<GroupAuthorEntity> findById(Object id) {
        throw new UnsupportedOperationException("不支持该操作...");
    }

    public void saveOrUpdate(OrganizationEntity company, boolean enabled, PageDefinedDto pageDefinedDto,
                             LoginUserContext userContext) {
        Optional<List<GroupAuthorEntity>> exits = findAllByCom(company);
        GroupAuthorEntity instance = new GroupAuthorEntity(company, pageDefinedDto, enabled, userContext);
        saveOrUpdateEntity(instance, exits, enabled, userContext);
        if (!enabled) getJdbc().update(getExecSql("disabledByCom", null), instance.toMap());
        if (getCache().isPresent() && !enabled) getCache().get().invalidateAll();
    }

    @SuppressWarnings("unchecked")
    private Optional<List<GroupAuthorEntity>> findAllByComId(Integer companyId) {
        final String cache_key = String.format("%s_%s_%s", getModel(), companyId, -1);
        if (getCache().isPresent()) {
            List<GroupAuthorEntity> list = (List<GroupAuthorEntity>) getCache().get().getIfPresent(cache_key);
            if (list != null) {
                if (logger.isDebugEnabled())
                    logger.debug(String.format("findAllByCom(%s) Touch from Cache by key=%s.", companyId, cache_key));
                return Optional.of(list);
            }
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", -1);
        params.put("companyId", companyId);
        List<GroupAuthorEntity> list = getJdbc().query(getExecSql("findAllByStore", null), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findAllByCom(%s) from DB Size is %s.", companyId,
                    CollectionUtils.isEmpty(list) ? 0 : list.size()));
        if (CollectionUtils.isNotEmpty(list) && getCache().isPresent()) {
            getCache().get().put(cache_key, list);
        }
        return Optional.fromNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

    public List<PageDefinedDto> filterByStore(StoreEntity store, List<PageDefinedDto> pageDefineds) {
        Optional<List<GroupAuthorEntity>> group_stores = findAllByStore(store);
        if (!group_stores.isPresent()) return pageDefineds;
        Set<String> diabledIds = Sets.newHashSet();
        for (GroupAuthorEntity $it : group_stores.get()) {
            if (!$it.isEnabled()) diabledIds.add($it.getGroupId());
        }
        if (CollectionUtils.isEmpty(diabledIds)) return pageDefineds;
        List<PageDefinedDto> clone = Lists.newArrayList();
        for (PageDefinedDto p : pageDefineds) {
            if (diabledIds.contains(p.getFullName())) clone.add(p);
        }
        if (CollectionUtils.isNotEmpty(clone)) pageDefineds.removeAll(clone);
        return pageDefineds;
    }

    @SuppressWarnings("unchecked")
    public Optional<List<GroupAuthorEntity>> findAllByStore(StoreEntity store) {
        final String cache_key = String.format("%s_%s_%s", getModel(), store.getCompanyId().or(-1), store.getId());
        if (getCache().isPresent()) {
            List<GroupAuthorEntity> list = (List<GroupAuthorEntity>) getCache().get().getIfPresent(cache_key);
            if (list != null) {
                if (logger.isDebugEnabled())
                    logger.debug(String.format("findAllByStore(%s) Touch from Cache by key=%s.", store.getId(), cache_key));
                return Optional.of(list);
            }
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().or(-1));
        List<GroupAuthorEntity> list = getJdbc().query(getExecSql("findAllByStore", null), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findAllByStore(%s) from DB Size is %s.", store.getId(),
                    CollectionUtils.isEmpty(list) ? 0 : list.size()));
        if (CollectionUtils.isNotEmpty(list) && getCache().isPresent()) {
            getCache().get().put(cache_key, list);
        }
        return Optional.fromNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

    /**
     * 更新权限表
     *
     * @param store
     * @param enabled
     * @param pageDefinedDto
     * @param userContext
     */
    public void saveOrUpdate(StoreEntity store, boolean enabled, PageDefinedDto pageDefinedDto, LoginUserContext userContext) {
        final String cache_key = String.format("%s_%s_%s", getModel(), store.getCompanyId().or(-1), store.getId());
        Optional<List<GroupAuthorEntity>> exits = findAllByStore(store);
        GroupAuthorEntity instance = new GroupAuthorEntity(store, pageDefinedDto, enabled, userContext);
        boolean clear_cache = saveOrUpdateEntity(instance, exits, enabled, userContext);
        if (clear_cache && getCache().isPresent()) {
            getCache().get().invalidate(cache_key);
        }
    }

    private boolean saveOrUpdateEntity(GroupAuthorEntity instance, Optional<List<GroupAuthorEntity>> exits,
                                       boolean enabled, LoginUserContext userContext) {
        boolean clear_cache = false;
        if (exits.isPresent()) {
            GroupAuthorEntity o = null;
            for (GroupAuthorEntity $it : exits.get()) {
                if ($it.equalsId(instance)) {
                    o = $it;
                    break;
                }
            }
            if (o != null) {
                Optional<GroupAuthorEntity> clone = o.change(enabled, userContext);
                if (clone.isPresent()) {
                    update(clone.get());
                    clear_cache = true;
                }
            } else {
                insert(instance);
                clear_cache = true;
            }
        } else {
            insert(instance);
        }
        return clear_cache;
    }

    private void insert(GroupAuthorEntity instance) {
        getJdbc().update(getExecSql("insert", null), instance.toMap());
    }

    private void update(GroupAuthorEntity instance) {
        getJdbc().update(getExecSql("update", null), instance.toMap());
    }

    @Override
    protected ResultSetExtractor<GroupAuthorEntity> getResultSetExtractor() {
        return null;
    }

    class RowMapperImpl implements RowMapper<GroupAuthorEntity> {
        @Override
        public GroupAuthorEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            try {
                return new GroupAuthorEntity(resultSet.getInt("id"), resultSet.getInt("companyId"),
                        resultSet.getString("groupId"),
                        resultSet.getInt("enabled") == 1,
                        resultSet.getInt("createUserId"),
                        resultSet.getDate("createTime"),
                        resultSet.getObject("modifyUserId") == null ? null : resultSet.getInt("modifyUserId"),
                        resultSet.getDate("modifyTime"));
            } catch (Exception e) {
                logger.error("还原 GroupAuthorEntity from db has error...", e);
                throw new RuntimeException(e);
            }
        }
    }
}
