package com.csosm.module.base.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.commons.mvc.BetweenDayDto;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class OrganizationEntityAction extends BaseEntityAction<OrganizationEntity> {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationEntityAction.class);

    public OrganizationEntityAction() {
        super("OrganizationEntity", "adapterCache");
    }

    /**
     * 判断所有的职员都在组织内
     *
     * @param empIds
     * @param orgId
     * @return
     */
    public boolean allInOrg(Collection<Integer> empIds, Integer orgId) {
        OrganizationEntity org = loadById(orgId);
        Map<String, Object> params = Maps.newHashMap();
        params.put("empIds", empIds);
        params.put("orgId", org.getId());
        String sql = getExecSql("count_emps", params);
        Integer result = getNamedParameterJdbcTemplate().queryForObject(sql, params, Integer.class);
        return result.intValue() == empIds.size();
    }

    /**
     * 添加新组织
     *
     * @param parent    父组织
     * @param name      组织名称
     * @param shortName 组织短称
     * @return
     */
    public String saveOrgination(LoginUserContext loginUser, OrganizationEntity parent, String name, String shortName) {
        java.util.Objects.requireNonNull(parent, "入参parent不能为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参name不能为空");
        OrganizationEntity entity = new OrganizationEntity(parent, selectMaxId() + 1, name, shortName);
        entity.setCreateUser(loginUser);
        String sql = getExecSql("add_organization", entity.toMap());
        int result = getNamedParameterJdbcTemplate().update(sql, entity.toMap());
        Preconditions.checkState(result == 1, "新增组织发生异常");
        if (getCache().isPresent()) getCache().get().invalidateAll();
        logProxy(SystemlogEntity.create(this.getClass(), "saveOrgination", String.format("新增组织[%s]", entity),
                "组织管理"));
        return entity.getCode();
    }

    /**
     * 新增公司
     *
     * @param loginUser 当前登录用户
     * @param name      公司名称
     * @param shortName 公司短称
     * @return
     */
    public OrganizationEntity saveCompany(LoginUserContext loginUser, Integer companyId, String name,
                                          Integer industryType, String shortName, String linkMan, String linkPhone) {
        Preconditions.checkNotNull(loginUser);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "公司[name]不能为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(shortName), "公司简称[shortName]不能为空");
        OrganizationEntity entity = new OrganizationEntity(companyId, name, shortName, industryType, linkMan, linkPhone);
        entity.setCreateUser(loginUser);
        List<OrganizationEntity> comexits = getJdbc().query(getExecSql("exits_company", null), entity.toMap(),
                new RowMapperImpl());
        if (CollectionUtils.isNotEmpty(comexits))
            throw new IllegalArgumentException(String.format("存在重复的ID:%s 或者 shortName:%s", companyId, shortName));

        int result = getJdbc().update(getExecSql("add_company", null), entity.toMap());
        Preconditions.checkState(result == 1, "新增公司发生异常");
        if (getCache().isPresent()) getCache().get().invalidateAll();
        logProxy(SystemlogEntity.create(this.getClass(), "saveCompany", String.format("新增公司[%s]", entity),
                "公司注册"));
        return loadById(companyId);
    }

    /**
     * 移除组织
     *
     * @param orgId 组织ID
     * @return
     */
    public String removeOrgination(Integer orgId) {
        Preconditions.checkNotNull(orgId, "入参orgId不能为空");
        OrganizationEntity org = loadById(orgId);
        if (getCache().isPresent()) getCache().get().invalidateAll();
        return removeOrgination(org);
    }

    /**
     * 移除组织
     *
     * @param org
     * @return
     */
    public String removeOrgination(OrganizationEntity org) {
        Preconditions.checkNotNull(org, "入参org不能为空");
        String sql = getExecSql("remove_organization", org.toMap());
        int result = getNamedParameterJdbcTemplate().update(sql, org.toMap());
        Preconditions.checkState(result == 1, "移除组织发生异常");
        if (getCache().isPresent()) getCache().get().invalidateAll();
        logProxy(SystemlogEntity.delete(this.getClass(), "removeOrgination", String.format("移除组织[%s]", org.getId()),
                "组织管理"));
        return org.getCode();
    }

    /**
     * 修改组织信息
     *
     * @param org         组织
     * @param name        组织名称
     * @param shortName   组织短称
     * @param hiddenPhone 是否隐藏会员电话号码
     */
    public void editOrgination(LoginUserContext loginUser, OrganizationEntity org, String name, String shortName, Integer hiddenPhone) {
        Preconditions.checkNotNull(org, "入参org不能为空");
        OrganizationEntity clone = org.modifyOrganization(name, shortName, hiddenPhone);
        if (clone.equals(org)) return;
        clone.setModifyUser(loginUser);
        String sql = getExecSql("update_organization", clone.toMap());
        int result = getNamedParameterJdbcTemplate().update(sql, clone.toMap());
        Preconditions.checkState(result == 1, "更新组织信息发生异常");
        if (getCache().isPresent()) getCache().get().invalidateAll();
        logProxy(SystemlogEntity.update(this.getClass(), "editOrgination", String.format("编辑组织[%s]", clone),
                "组织管理"));
    }

    /**
     * 修改公司信息
     *
     * @param company            公司
     * @param name               名称
     * @param shortName          公司短称
     * @param industryType       行业类型
     * @param showAchievementOrg 是否显示业绩单位
     * @param hiddenPhone        是否隐藏会员电话号码
     */
    public void editCompany(LoginUserContext loginUser, OrganizationEntity company, String name, String shortName, Integer industryType,
                            Integer showAchievementOrg, Integer hiddenPhone) {
        Preconditions.checkNotNull(company, "入参company不能为空");
        OrganizationEntity clone = company.modifyCompany(name, shortName, industryType, showAchievementOrg, hiddenPhone);
        if (clone.equals(company)) return;
        clone.setModifyUser(loginUser);
        String sql = getExecSql("update_company", clone.toMap());
        int result = getNamedParameterJdbcTemplate().update(sql, clone.toMap());
        Preconditions.checkState(result == 1, "更新公司信息发生异常");
        if (getCache().isPresent()) getCache().get().invalidateAll();
        logProxy(SystemlogEntity.update(this.getClass(), "editOrgination", String.format("编辑组织[%s]", clone),
                "组织管理"));
    }

    /**
     * 迁移组织
     *
     * @param parent 父组织
     * @param org    待迁移组织
     */
    public void switchOrgization(OrganizationEntity parent, OrganizationEntity org) {
        Preconditions.checkNotNull(parent, "入参parent不能为空");
        Preconditions.checkNotNull(org, "入参org不能为空");
        Preconditions.checkState(!org.isMyself(parent), "不允许进行自身对自身的迁移");
        if (org.isMyParent(parent)) return;
        String code = String.format("%s_%s", parent.getCode(), org.getId());
        Map<String, Object> params = Maps.newHashMap();
        params.put("newCode", code);
        params.put("oldCode", org.getCode());
        params.put("parentId", parent.getId());
        String parentSql = getExecSql("update_parent", params);
        int result = getNamedParameterJdbcTemplate().update(parentSql, params);
        Preconditions.checkState(result == 1, "更改父组织发生异常");
        String switchSql = getExecSql("switch_orgization", params);
        getNamedParameterJdbcTemplate().update(switchSql, params);
        if (getCache().isPresent()) getCache().get().invalidateAll();
        logProxy(SystemlogEntity.update(this.getClass(), "switchOrgization",
                String.format("迁移组织from[%s] to [%s]", org.getId(), parent.getId()), "组织管理"));
    }

    private Integer selectMaxId() {
        String sql = getExecSql("select_maxId", null);
        return getJdbcTemplate().queryForObject(sql, Integer.class);
    }

    public Optional<OrganizationEntity> findOrgById(Integer companyId, Integer id) {
        Optional<List<OrganizationEntity>> orgsOpt = findOrgByIds(companyId, Lists.newArrayList(id));
        if (!orgsOpt.isPresent()) return Optional.absent();
        List<OrganizationEntity> orgs = orgsOpt.get();
        if (orgs.isEmpty()) return Optional.absent();
        Preconditions.checkState(orgs.size() == 1, String.format("id[%s]对应组织超过一个", id));
        return Optional.of(orgs.get(0));
    }

    public Optional<List<OrganizationEntity>> findOrgByIds(Integer companyId, Collection<Integer> ids) {
        Preconditions.checkNotNull(companyId);
        Optional<OrganizationEntity> company = findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "不存在Id=%s对应的公司.", companyId);
        if (CollectionUtils.isEmpty(ids)) return Optional.absent();
        Optional<List<OrganizationEntity>> all_orgs_opt = loadAllByCompany(company.get());
        if (!all_orgs_opt.isPresent()) return Optional.absent();
        List<OrganizationEntity> sub_list = Lists.newArrayList();
        for (OrganizationEntity $it : all_orgs_opt.get()) {
            if (ids.contains($it.getId())) sub_list.add($it);
        }
        return Optional.fromNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    public Optional<List<OrganizationEntity>> loadAllSuperOrgs(Integer companyId, Integer orgId, boolean incloudSlef) {
        Preconditions.checkNotNull(orgId);
        Preconditions.checkNotNull(companyId);
        // 公司名下 直接  挂在门店的情况
        if (Ints.compare(orgId, companyId) == 0) return Optional.absent();

        Optional<OrganizationEntity> company = findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "不存在Id=%s对应的公司.", companyId);
        Optional<List<OrganizationEntity>> all_orgs_opt = loadAllByCompany(company.get());
        if (!all_orgs_opt.isPresent()) return Optional.absent();
        OrganizationEntity self = null;
        for (OrganizationEntity $it : all_orgs_opt.get()) {
            if ($it.getId().equals(orgId)) {
                self = $it;
                break;
            }
        } // end_for
        Preconditions.checkNotNull(self, "orgId=%s 对应的组织不存在....", orgId);
        List<OrganizationEntity> super_list = Lists.newArrayList();
        for (OrganizationEntity $it : all_orgs_opt.get()) {
            if (self.isMySuperOrg($it)) super_list.add($it);
        }
        if (incloudSlef) super_list.add(self);
        return Optional.fromNullable(CollectionUtils.isEmpty(super_list) ? null : super_list);
    }

    public Optional<List<OrganizationEntity>> loadAllSubOrgs(Integer companyId, Integer orgId, boolean incloudSlef) {
        Preconditions.checkNotNull(orgId);
        Preconditions.checkNotNull(companyId);
        Optional<OrganizationEntity> company = findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "不存在Id=%s对应的公司.", companyId);
        Optional<List<OrganizationEntity>> all_orgs_opt = loadAllByCompany(company.get());
        if (!all_orgs_opt.isPresent()) return Optional.absent();
        if (companyId.equals(orgId)) {
            return all_orgs_opt;
        }
        OrganizationEntity self = null;
        for (OrganizationEntity $it : all_orgs_opt.get()) {
            if ($it.getId().equals(orgId)) {
                self = $it;
                break;
            }
        } // end_for
        Preconditions.checkNotNull(self, "orgId=%s 对应的组织不存在....", orgId);
        List<OrganizationEntity> sub_list = Lists.newArrayList();
        if (incloudSlef) sub_list.add(self);
        for (OrganizationEntity $it : all_orgs_opt.get()) {
            if (self.isMySubOrg($it)) sub_list.add($it);
        }
        return Optional.fromNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    /**
     * 获取指定ID 的所有下级组织部门列表信息 不包含含自身
     */
    public Optional<List<OrganizationEntity>> loadAllSubOrgs(OrganizationEntity parent, Integer companyId) {
        Preconditions.checkNotNull(parent);
        Preconditions.checkNotNull(companyId);
        Optional<OrganizationEntity> company = findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "不存在Id=%s对应的公司.", companyId);
        Optional<List<OrganizationEntity>> all_orgs_opt = loadAllByCompany(company.get());
        if (!all_orgs_opt.isPresent()) return Optional.absent();
        List<OrganizationEntity> sub_list = Lists.newArrayList();
        String macth_code = String.format("%s_", parent.getCode());
        for (OrganizationEntity $it : all_orgs_opt.get()) {
            if (StringUtils.startsWith($it.getCode(), macth_code)) sub_list.add($it);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("组织 %s 含有直接下级组织 %s 个。",
                    parent.getName(), CollectionUtils.isEmpty(sub_list) ? 0 : sub_list.size()));
        }
        return Optional.fromNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    /**
     * 获取指定ID 的所有下级组织部门列表信息 不包含含自身
     */
    public Optional<List<OrganizationEntity>> loadDirectSubOrgs(OrganizationEntity parent, Integer companyId) {
        Preconditions.checkNotNull(parent);
        Preconditions.checkNotNull(companyId);
        Optional<OrganizationEntity> company = findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "不存在Id=%s对应的公司.", companyId);
        Optional<List<OrganizationEntity>> all_orgs_opt = loadAllByCompany(company.get());
        if (!all_orgs_opt.isPresent()) return Optional.absent();
        List<OrganizationEntity> sub_list = Lists.newArrayList();
        for (OrganizationEntity $it : all_orgs_opt.get()) {
            if ($it.isMyParent(parent)) sub_list.add($it);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("组织 %s 含有直接下级组织 %s 个。",
                    parent.getName(), CollectionUtils.isEmpty(sub_list) ? 0 : sub_list.size()));
        }
        return Optional.fromNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    /**
     * 通过ID获取公司信息，注意 该ID可能是一个组织部门ID 并非公司ID type=1 才是公司
     */
    public Optional<OrganizationEntity> findCompanyById(Integer id) {
        Optional<OrganizationEntity> comOpt = findById(id);
        if (!comOpt.isPresent()) return comOpt;
        if (!comOpt.get().isCompany()) return Optional.absent();
        if (logger.isDebugEnabled())
            logger.debug(String.format("<%s> findCompanyById(%s) return %s", getModel(), id, comOpt.get()));
        return comOpt;
    }

    @SuppressWarnings("unchecked")
    public Optional<List<OrganizationEntity>> loadAllCompanies() {
        String cache_key = String.format("%s_all_companies", getModel());
        if (getCache().isPresent()) {
            List<OrganizationEntity> orgs = (List<OrganizationEntity>) getCache().get().getIfPresent(cache_key);
            if (CollectionUtils.isNotEmpty(orgs)) return Optional.of(orgs);
        }
        List<OrganizationEntity> orgs = getNamedParameterJdbcTemplate()
                .query(getExecSql("loadAllCompanyies", null), new RowMapperImpl());
        if (CollectionUtils.isNotEmpty(orgs) && getCache().isPresent())
            getCache().get().put(cache_key, orgs);
        return Optional.fromNullable(CollectionUtils.isEmpty(orgs) ? null : orgs);
    }

    /**
     * 获取报表组织数最大深度
     */
    public int loadReportOrgDeep(Integer companyId) {
//        Preconditions.checkNotNull(companyId, "须指定公司ID");
//        BetweenDayDto betweenDate = BetweenDayDto.withStartAndEnd(DateTime.now().plusDays(-10), DateTime.now());
//        Map<String, Object> param = betweenDate.toSqlDateStr(null);
//        param.put("companyId", companyId);
//        List<String> codes_opt = getNamedParameterJdbcTemplate()
//                .queryForList(getExecSql("loadReportAllOrgs", null), param, String.class);
//        if (CollectionUtils.isEmpty(codes_opt)) return 0;
//        int deep = 0;
//        for (String $it : codes_opt) {
//            deep = Ints.max(deep, StringUtils.split($it, '_').length);
//        }
//        if (logger.isDebugEnabled())
//            logger.debug(String.format("compayId= %s,组织深度 %s 。", companyId, deep));
        return 0;
    }

    /**
     * 加载报表专用组织树
     */
    @SuppressWarnings("unchecked")
    public Optional<List<OrganizationEntity>> loadAllOrgs4Report(BetweenDayDto betweenDate, LoginUserContext userContext) {
        Preconditions.checkNotNull(userContext, "无法获取登陆用户上下文");
        Preconditions.checkArgument(userContext.getCompany().isPresent(), "该用户尚为加入任何公司，无法调用该功能.");
        Optional<OrganizationEntity> company = findCompanyById(userContext.getCompany().get().getId());
        Preconditions.checkState(company.isPresent(), "不存在Id=%s对应的公司.",
                userContext.getCompany().get().getId());

        Optional<List<OrganizationEntity>> all_orgs_opt = loadAllByCompany(company.get());
        if (!all_orgs_opt.isPresent()) return Optional.absent();
        Map<String, Object> param = betweenDate.toSqlDateStr(null);
        param.put("companyId", userContext.getCompany().get().getId());
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();
        List<String> org_code_list = getNamedParameterJdbcTemplate()
                .queryForList(getExecSql("loadReportAllOrgs", null), param, String.class);
        if (CollectionUtils.isEmpty(org_code_list)) return Optional.absent();
        stopwatch.stop();
        // 增加时间间隔监控输出日志
        if (logger.isTraceEnabled())
            logger.trace(String.format("queryForList(loadReportAllOrgs)  elapsed %s ms",
                    stopwatch.elapsed(TimeUnit.MILLISECONDS)));

        Set<Integer> org_ids = Sets.newHashSet();
        String[] _args;
        for (String code : org_code_list) {
            _args = StringUtils.split(code, '_');
            for (String $it : _args) org_ids.add(Integer.valueOf($it));
        }
        List<OrganizationEntity> sub_list = Lists.newArrayList();
        for (OrganizationEntity $it : all_orgs_opt.get()) {
            if (org_ids.contains($it.getId())) sub_list.add($it);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("compayId= %s,date=%s 有效组织 %s 个。", userContext.getCompany().get().getId(),
                    betweenDate, CollectionUtils.isEmpty(sub_list) ? 0 : sub_list.size()));
        }
        return Optional.fromNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }


    @SuppressWarnings("unchecked")
    public Optional<List<OrganizationEntity>> loadAllByCompany(OrganizationEntity company) {
        Preconditions.checkNotNull(company);
        final String cache_key = String.format("%s_all_org_%s", getModel(), company.getId());
        if (getCache().isPresent()) {
            Object cache_val = getCache().get().getIfPresent(cache_key);
            if (cache_val != null) {
                List<OrganizationEntity> orgs = (List<OrganizationEntity>) cache_val;
                if (logger.isDebugEnabled())
                    logger.debug(String.format("loadAllByCompany (%s) from cache.", company.getId()));
                return Optional.of(orgs);
            }
        }
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("code", String.format("%s_%%", company.getId()));
        List<OrganizationEntity> list = getNamedParameterJdbcTemplate()
                .query(getExecSql("findByCode", null), paramMap, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByCode(%s) return entity's size is %s",
                    company.getId(), CollectionUtils.isEmpty(list) ? 0 : list.size()));
        if (getCache().isPresent() && !CollectionUtils.isEmpty(list)) {
            getCache().get().put(cache_key, list);
        }
        return Optional.fromNullable(CollectionUtils.isEmpty(list) ? null : list);
    }


    @Override
    protected ResultSetExtractor<OrganizationEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<OrganizationEntity> {

        @Override
        public OrganizationEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return new OrganizationEntity(
                        resultSet.getInt("id"),
                        resultSet.getString("code"),
                        resultSet.getInt("parentId"),
                        resultSet.getInt("type"),
                        resultSet.getString("name"),
                        resultSet.getString("shortName"),
                        resultSet.getInt("status"),
                        resultSet.getInt("depth"),
                        0 == resultSet.getInt("rootNode"),
                        (Integer) resultSet.getObject("industryType"),
                        (Integer) resultSet.getObject("orgShowFlag"),
                        (Integer) resultSet.getObject("hiddenMemberPhoneFlag"),
                        resultSet.getString("linkMan"),
                        resultSet.getString("linkPhone"));
            }
            return null;
        }
    }

    @Override
    protected OrganizationEntity selectById(Object id) {
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("id", id);

        List<OrganizationEntity> list = getNamedParameterJdbcTemplate()
                .query(getExecSql("findById", paramMap), paramMap, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("<%s> selectById(%s) return entity. is %s",
                    getModel(), id, CollectionUtils.isEmpty(list) ? 0 : list.size()));
        OrganizationEntity root = null;
        if (!CollectionUtils.isEmpty(list)) {
            for (OrganizationEntity cursor : list) {
                if (Objects.equal(id, cursor.getId())) {
                    root = cursor;
                    break;
                }
            }
            if (root != null) root.setSubOrgs(list);
        }
        if (logger.isDebugEnabled()) logger.debug(String.format("selectById(%s) -> %s", id, root));
        return root;
    }

    class RowMapperImpl implements RowMapper<OrganizationEntity> {
        @Override
        public OrganizationEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new OrganizationEntity(
                    resultSet.getInt("id"),
                    resultSet.getString("code"),
                    resultSet.getInt("parentId"),
                    resultSet.getInt("type"),
                    resultSet.getString("name"),
                    resultSet.getString("shortName"),
                    resultSet.getInt("status"),
                    resultSet.getInt("depth"),
                    0 == resultSet.getInt("rootNode"),
                    (Integer) resultSet.getObject("industryType"),
                    (Integer) resultSet.getObject("orgShowFlag"),
                    (Integer) resultSet.getObject("hiddenMemberPhoneFlag"),
                    resultSet.getString("linkMan"),
                    resultSet.getString("linkPhone"));
        }
    }
}
