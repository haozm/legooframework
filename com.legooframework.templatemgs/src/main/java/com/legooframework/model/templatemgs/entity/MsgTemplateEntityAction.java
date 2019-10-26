package com.legooframework.model.templatemgs.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.membercare.entity.BusinessType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.object.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MsgTemplateEntityAction extends BaseEntityAction<MsgTemplateEntity> {

    private static final Logger logger = LoggerFactory.getLogger(MsgTemplateEntityAction.class);

    private SimpleJdbcInsert simpleJdbcInsert;
    private SqlUpdate update4Black;
    private SqlUpdate update4Change;
    private SqlUpdate update4Default;
    private SqlUpdate update4BlackList;

    public MsgTemplateEntityAction() {
        super("templatemgsCache");
    }

    public Optional<MsgTemplateEntity> findById(Object id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        List<Map<String, Object>> list = getNamedParameterJdbcTemplate()
                .queryForList("SELECT company_id AS 'cId', store_id AS 'sId' FROM MSG_TEMPLATE_CONTEXT WHERE id = :id", params);
        Preconditions.checkState(list.size() <= 1, "数据异常，存在多个主键ID=%s", id);
        if (CollectionUtils.isEmpty(list)) return Optional.empty();
        Integer companyId = MapUtils.getInteger(list.get(0), "cId");
        Integer storeId = MapUtils.getInteger(list.get(0), "sId");
        Optional<List<MsgTemplateEntity>> opts = null;
        if (companyId == -1) {
            opts = loadByGeneral();
        } else if (storeId == -1) {
            opts = loadByCompany(companyId);
        } else {
            opts = loadByStore(companyId, storeId);
        }
        Optional<MsgTemplateEntity> res = opts.flatMap(msgTemplateEntities -> msgTemplateEntities.stream()
                .filter(x -> x.getId().equals(id)).findFirst());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findById(%s) res %s", id, res.orElse(null)));
        return res;
    }

    /**
     * 加载当前激活可用的 模板列表
     *
     * @param user       霹雳
     * @param classifyId 豹子头
     * @return AA
     */
    public Optional<List<MsgTemplateEntity>> loadEnabledListByUser(LoginContext user, CrmStoreEntity store, String classifyId) {
        Optional<List<MsgTemplateEntity>> opts = loadByStore(user.getTenantId().intValue(),
                store == null ? user.getStoreId() : store.getId());
        if (!opts.isPresent()) return Optional.empty();
        List<MsgTemplateEntity> eblallist = opts.get().stream()
                .filter(MsgTemplateEntity::isUnBlacked)
                .filter(x -> x.isClassify(classifyId)).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(eblallist) ? null : eblallist);
    }

    /**
     * 加载当前激活可用的 90 模板列表
     *
     * @param store 门店
     * @return 无邪
     */
    public Optional<List<MsgTemplateEntity>> loadEnabledTouch90ByStore(CrmStoreEntity store) {
        Optional<List<MsgTemplateEntity>> opts = loadByStore(store.getCompanyId(), store.getId());
        if (!opts.isPresent()) return Optional.empty();
        String prefix = String.format("%s_%s_", BusinessType.TOUCHED90.toString(), store.getCompanyId());
        List<MsgTemplateEntity> eblallist = opts.get().stream()
                .filter(MsgTemplateEntity::isUnBlacked)
                .filter(x -> StringUtils.startsWith(x.getSingleClassifies(), prefix))
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(eblallist) ? null : eblallist);
    }

    /**
     * 加载当前激活可用的 模板列表
     *
     * @param company 公司
     * @return 无邪
     */
    public Optional<List<MsgTemplateEntity>> loadEnabledListByCom(CrmOrganizationEntity company) {
        Optional<List<MsgTemplateEntity>> opts = loadByCompany(company.getId());
        if (!opts.isPresent()) return Optional.empty();
        List<MsgTemplateEntity> sub_list = opts.get().stream()
                .filter(MsgTemplateEntity::isUnBlacked).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    /**
     * 加载当前激活可用的 模板列表
     *
     * @param store      门店
     * @param classifyId AI u
     * @return 无邪
     */
    public Optional<List<MsgTemplateEntity>> loadEnabledListByStore(CrmStoreEntity store, String classifyId) {
        Optional<List<MsgTemplateEntity>> opts = loadByStore(store.getCompanyId(), store.getId());
        if (!opts.isPresent()) return Optional.empty();
        List<MsgTemplateEntity> eblallist = opts.get().stream()
                .filter(MsgTemplateEntity::isUnBlacked)
                .filter(x -> x.isClassify(classifyId)).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(eblallist) ? null : eblallist);
    }

    public void changeTemplate(String tempId, String title, String context, String classifyId,
                               Collection<UseScope> useScopes) {
        if (null == context && null == classifyId && CollectionUtils.isEmpty(useScopes)) return;
        LoginContext user = LoginContextHolder.get();
        Optional<MsgTemplateEntity> ins = this.findById(tempId);
        Preconditions.checkState(ins.isPresent(), "不存在ID=%s 对应的模板实例...", tempId);
        Optional<MsgTemplateEntity> update = ins.get().changeTemplate(title, context, classifyId, useScopes, user);
        update.ifPresent(x -> {
            this.update4Change.update(x.getTemplate(), x.getSingleClassifies(), x.getUseScopes4Save(),
                    x.getTitle(), x.getId());
            String cache_key = cacheKey(x.getCompanyId(), x.getStoreId());
            getCache().ifPresent(c -> c.evict(cache_key));
        });
    }

    public Optional<List<MsgTemplateEntity>> findUnBlackByStore(CrmStoreEntity store) {
        Optional<List<MsgTemplateEntity>> list = loadByStore(store.getCompanyId(), store.getId());
        if (!list.isPresent()) return Optional.empty();
        List<MsgTemplateEntity> sub_list = list.get().stream().filter(MsgTemplateEntity::isUnBlacked).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    /**
     * 获取指定节点默认的模板
     *
     * @param classifies 分类标识
     * @return 我的大地
     */
    public Optional<List<MsgTemplateEntity>> loadDefaultByClassifies(List<String> classifies) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("classifies", classifies.stream().map(x -> String.format("'%s'", x)).collect(Collectors.toList()));
        params.put("sql", "loadDefaultByClassifies");
        Optional<List<MsgTemplateEntity>> templates = super.queryForEntities("loadDefaultByClassifies", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadDefaultByClassifies(%s) return size is %s", classifies,
                    templates.map(List::size).orElse(0)));
        return templates;
    }

    private Optional<MsgTemplateEntity> findById(Optional<List<MsgTemplateEntity>> list, String id) {
        return list.flatMap(msgTemplateEntities -> msgTemplateEntities.stream().filter(x -> x.getId().equals(id)).findFirst());
    }

    public void blackTemplate(String tempId) {
        LoginContext user = LoginContextHolder.get();
        Optional<MsgTemplateEntity> template = this.findById(tempId);
        if (!template.isPresent()) return;
        if (template.get().isOWner(user)) {
            Optional<MsgTemplateEntity> _black = template.get().blacked(user);
            _black.ifPresent(x -> {
                this.update4Black.update(x.isBlacked() ? 1 : 0, x.isDefaulted() ? 1 : 0, x.getId());
                getCache().ifPresent(Cache::clear);
            });
        } else {
            Optional<MsgTemplateBlack> blackList = loadBlackListByUser(user);
            if (blackList.isPresent()) {
                blackList = blackList.get().blacked(template.get());
                blackList.ifPresent(x -> {
                    update4BlackList(x);
                    getCache().ifPresent(Cache::clear);
                });
            } else {
                MsgTemplateBlack ins = new MsgTemplateBlack(user, template.get());
                update4BlackList(ins);
                getCache().ifPresent(Cache::clear);
            }
        }
    }

    public void unBlackTemplate(String tempId) {
        LoginContext user = LoginContextHolder.get();
        Optional<MsgTemplateEntity> template = this.findById(tempId);
        if (!template.isPresent()) return;
        if (template.get().isOWner(user)) {
            Optional<MsgTemplateEntity> clone = template.get().unBlacked(user);
            clone.ifPresent(x -> {
                this.update4Black.update(x.isBlacked() ? 1 : 0, x.isDefaulted() ? 1 : 0, x.getId());
                getCache().ifPresent(Cache::clear);
            });
        } else {
            Optional<MsgTemplateBlack> blackList = loadBlackListByUser(user);
            blackList.ifPresent(c -> {
                Optional<MsgTemplateBlack> clome = c.unblacked(template.get());
                clome.ifPresent(x -> {
                    update4BlackList(x);
                    getCache().ifPresent(Cache::clear);
                });
            });
        }
    }

    private String cacheKey(Integer companyId, Integer storeId) {
        return String.format("%s_%s_%s", getModelName(), companyId, storeId);
    }

    private Optional<List<MsgTemplateEntity>> loadByGeneral() {
        final String cacheKey = cacheKey(-1, -1);
        if (getCache().isPresent()) {
            @SuppressWarnings("unchecked")
            List<MsgTemplateEntity> values = getCache().get().get(cacheKey, List.class);
            if (CollectionUtils.isNotEmpty(values)) return Optional.of(values);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", -1);
        Optional<List<MsgTemplateEntity>> list = super.queryForEntities("loadByGeneral", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByGeneral() res is %s", list.map(List::size).orElse(0)));
        list.ifPresent(x -> getCache().ifPresent(c -> c.put(cacheKey, x)));
        return list;
    }

    private Optional<List<MsgTemplateEntity>> loadByCompany(Integer companyId) {
        final String cacheKey = cacheKey(companyId, -1);
        if (getCache().isPresent()) {
            @SuppressWarnings("unchecked")
            List<MsgTemplateEntity> values = getCache().get().get(cacheKey, List.class);
            if (CollectionUtils.isNotEmpty(values)) return Optional.of(values);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        Optional<List<MsgTemplateEntity>> list = super.queryForEntities("loadByCompany", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByCompany(%s) res is %s", companyId, list.map(List::size).orElse(0)));
        list.ifPresent(x -> getCache().ifPresent(c -> c.put(cacheKey, x)));
        return list;
    }

    private Optional<List<MsgTemplateEntity>> loadByStore(Integer companyId, Integer storeId) {
        final String cacheKey = cacheKey(companyId, storeId);
        if (getCache().isPresent()) {
            @SuppressWarnings("unchecked")
            List<MsgTemplateEntity> values = getCache().get().get(cacheKey, List.class);
            if (CollectionUtils.isNotEmpty(values)) return Optional.of(values);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        Optional<List<MsgTemplateEntity>> list = super.queryForEntities("loadByStore", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByStore(%s,%s) res is %s", companyId, storeId, list.map(List::size).orElse(0)));
        list.ifPresent(x -> getCache().ifPresent(c -> c.put(cacheKey, x)));
        return list;
    }

    @Override
    protected void initTemplateConfig() {
        super.initTemplateConfig();
        Preconditions.checkNotNull(getJdbcTemplate());
        Preconditions.checkNotNull(getJdbcTemplate().getDataSource());
        this.simpleJdbcInsert = new SimpleJdbcInsert(getJdbcTemplate()).withTableName("MSG_TEMPLATE_CONTEXT");
        this.simpleJdbcInsert.usingColumns("id", "company_id", "org_id", "store_id", "blacked", "classifies",
                "use_scopes", "expire_date", "temp_context", "temp_title", "tenant_id", "creator", "is_default");
        this.simpleJdbcInsert.compile();

        this.update4Black = new SqlUpdate(getJdbcTemplate().getDataSource(), "UPDATE MSG_TEMPLATE_CONTEXT SET blacked= ?,is_default = ? WHERE id= ?");
        this.update4Black.declareParameter(new SqlParameter("blacked", Types.NUMERIC));
        this.update4Black.declareParameter(new SqlParameter("isDefault", Types.NUMERIC));
        this.update4Black.declareParameter(new SqlParameter("id", Types.VARCHAR));
        this.update4Black.compile();

        this.update4Change = new SqlUpdate(getJdbcTemplate().getDataSource(),
                "UPDATE MSG_TEMPLATE_CONTEXT SET temp_context= ?, classifies = ?, use_scopes =?, temp_title= ? WHERE id= ?");
        this.update4Change.declareParameter(new SqlParameter("tempContext", Types.VARCHAR));
        this.update4Change.declareParameter(new SqlParameter("classifies", Types.VARCHAR));
        this.update4Change.declareParameter(new SqlParameter("useScopes", Types.VARCHAR));
        this.update4Change.declareParameter(new SqlParameter("tempTitle", Types.VARCHAR));
        this.update4Change.declareParameter(new SqlParameter("id", Types.VARCHAR));
        this.update4Change.compile();

        this.update4BlackList = new SqlUpdate(getJdbcTemplate().getDataSource(),
                "REPLACE INTO MSG_TEMPLATE_BLACKLIST (company_id, org_id, store_id, black_list) VALUES (? , -1 , ? , ?)");
        this.update4BlackList.declareParameter(new SqlParameter("companyId", Types.NUMERIC));
        this.update4BlackList.declareParameter(new SqlParameter("storeId", Types.NUMERIC));
        this.update4BlackList.declareParameter(new SqlParameter("blackList", Types.VARCHAR));
        this.update4BlackList.compile();

        this.update4Default = new SqlUpdate(getJdbcTemplate().getDataSource(),
                "UPDATE MSG_TEMPLATE_CONTEXT SET is_default= ? WHERE id= ?");
        this.update4Default.declareParameter(new SqlParameter("isDefault", Types.NUMERIC));
        this.update4Default.declareParameter(new SqlParameter("id", Types.VARCHAR));
        this.update4Default.compile();

    }

    private void update4BlackList(MsgTemplateBlack templateBlack) {
        this.update4BlackList.update(templateBlack.getCompanyId(), templateBlack.getStoreId(), templateBlack.getBlackList4Save());
    }

    public void setDefaulte(String templateId, LoginContext user) {
        Optional<MsgTemplateEntity> entity = this.findById(templateId);
        Preconditions.checkState(entity.isPresent(), "不存在ID=%s对应的模板...");
        Preconditions.checkState(entity.get().isOWner(user), "当前模板的拥有者不属于当前用户...");
        if (entity.get().isDefaulted()) return;
        Optional<MsgTemplateEntity> exits = loadDefTemplate(user, entity.get().getSingleClassifies());
        if (user.isShoppingGuide() || user.isStoreManager()) {

        } else {

        }
        Preconditions.checkState(!exits.isPresent(), "当前模板所在范围已经有有默认的模板存在...");
        Optional<MsgTemplateEntity> clone = entity.get().setDefaulte();
        clone.ifPresent(x -> {
            this.update4Default.update(x.isDefaulted() ? 1 : 0, x.getId());
            String cacheKey = cacheKey(user.getTenantId().intValue(), user.getStoreId());
            getCache().ifPresent(c -> c.evict(cacheKey));
        });
    }

    public void setUnDefaulte(String templateId, LoginContext user) {
        Optional<MsgTemplateEntity> entity = this.findById(templateId);
        Preconditions.checkState(entity.isPresent(), "不存在ID=%s对应的模板...");
        Preconditions.checkState(entity.get().isOWner(user), "当前模板的拥有者不属于当前用户...");
        Optional<MsgTemplateEntity> clone = entity.get().setUnDefaulte();
        clone.ifPresent(x -> {
            this.update4Default.update(x.isDefaulted() ? 1 : 0, x.getId());
            String cacheKey = cacheKey(user.getTenantId().intValue(), user.getStoreId());
            getCache().ifPresent(c -> c.evict(cacheKey));
        });
    }

    private Optional<MsgTemplateEntity> loadDefTemplate(LoginContext user, String classfy) {
        Optional<List<MsgTemplateEntity>> list = Optional.empty();
        if (user.isShoppingGuide() || user.isStoreManager()) {
            list = loadByStore(user.getTenantId().intValue(), user.getStoreId());
            if (list.isPresent()) {
                List<MsgTemplateEntity> sub_list = list.get().stream()
                        .filter(MsgTemplateEntity::isStore).filter(MsgTemplateEntity::isDefaulted)
                        .filter(x -> StringUtils.equals(x.getSingleClassifies(), classfy))
                        .collect(Collectors.toList());
                return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list.get(0));
            }
        } else if (user.isManager()) {
            list = loadByGeneral();
        } else {
            list = loadByCompany(user.getTenantId().intValue());
            if (list.isPresent()) {
                List<MsgTemplateEntity> sub_list = list.get().stream()
                        .filter(MsgTemplateEntity::isCompany).filter(MsgTemplateEntity::isDefaulted)
                        .filter(x -> StringUtils.equals(x.getSingleClassifies(), classfy))
                        .collect(Collectors.toList());
                return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list.get(0));
            }
        }
        // NEXT
        if (!list.isPresent()) return Optional.empty();
        List<MsgTemplateEntity> sub_list = list.get().stream().filter(MsgTemplateEntity::isUnBlacked)
                .filter(x -> StringUtils.equals(x.getSingleClassifies(), classfy))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sub_list)) return Optional.empty();
        return sub_list.stream().filter(MsgTemplateEntity::isDefaulted).findFirst();
    }

    public void insert(MsgTemplateEntity templateCtx, LoginContext user) {
        if (templateCtx.isDefaulted()) {
            Optional<MsgTemplateEntity> exits = loadDefTemplate(user, templateCtx.getSingleClassifies());
            Preconditions.checkState(!exits.isPresent(), "当前模板所在范围已经有有默认的模板存在...");
        }
        Map<String, Object> parameters = Maps.newHashMapWithExpectedSize(16);
        parameters.put("id", templateCtx.getId());
        parameters.put("company_id", templateCtx.getCompanyId());
        parameters.put("org_id", templateCtx.getOrgId());
        parameters.put("store_id", templateCtx.getStoreId());
        parameters.put("blacked", templateCtx.isBlacked() ? 1 : 0);
        parameters.put("classifies", templateCtx.getSingleClassifies());
        parameters.put("use_scopes", templateCtx.getUseScopes4Save());
        parameters.put("expire_date", null);
        parameters.put("temp_context", templateCtx.getTemplate());
        parameters.put("temp_title", templateCtx.getTitle());
        parameters.put("tenant_id", templateCtx.getTenantId());
        parameters.put("is_default", templateCtx.isDefaulted() ? 1 : 0);
        parameters.put("creator", templateCtx.getCreator() == null ? -1L : templateCtx.getCreator());
        this.simpleJdbcInsert.execute(parameters);
        getCache().ifPresent(Cache::clear);
    }


    @Override
    protected RowMapper<MsgTemplateEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<MsgTemplateEntity> {
        @Override
        public MsgTemplateEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new MsgTemplateEntity(res.getString("id"), res);
        }
    }

    class BlackListRowMapper implements RowMapper<MsgTemplateBlack> {
        @Override
        public MsgTemplateBlack mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new MsgTemplateBlack(rs);
        }
    }

    private Optional<MsgTemplateBlack> loadBlackListByUser(LoginContext user) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", user.getTenantId().intValue());
        params.put("storeId", user.getStoreId());
        String sql = getStatementFactory().getExecSql(getModelName(), "loadBlackList", params);
        try {
            MsgTemplateBlack ins = getNamedParameterJdbcTemplate().queryForObject(sql, params, new BlackListRowMapper());
            if (logger.isDebugEnabled())
                logger.debug(String.format("loadBlackListByUser(%s,%s) res is %s", user.getTenantId(), user.getStoreId(),
                        ins));
            return Optional.of(ins);
        } catch (EmptyResultDataAccessException e) {
            logger.debug(String.format("loadBlackListByUser(%s,%s) res is null", user.getTenantId(), user.getStoreId()));
            return Optional.empty();
        }
    }

}
