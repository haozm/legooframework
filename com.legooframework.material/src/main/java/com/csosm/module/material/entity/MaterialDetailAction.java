package com.csosm.module.material.entity;

import com.csosm.commons.adapter.LoginUserContext;

import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MaterialDetailAction extends BaseEntityAction<MaterialDetailEntity> {

    public MaterialDetailAction() {
        super("MaterialDetailEntity", "defCache");
    }

    private static Gson gson = new Gson();
    private static Type MATERIAL_TYPE = new TypeToken<List<MaterialDetailEntity.Material>>() {
    }.getType();

    private void addBlacklistByUser(LoginUserContext user, MaterialDetailEntity material) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(material);
        Optional<MaterialBlacklistEntity> blacklist = findByUser(user);
        if (blacklist.isPresent()) {
            Optional<MaterialBlacklistEntity> clone = blacklist.get().addBlackMaterial(material);
            if (clone.isPresent()) {
                getJdbc().update(getExecSql("updateBlackById", null), clone.get().toMap());
            }
        } else {
            MaterialBlacklistEntity enity = MaterialBlacklistEntity.createAnyListByUser(user, material, true);
            getJdbc().update(getExecSql("insertBlack", null), enity.toMap());
        }
    }

    private void addFansListByUser(LoginUserContext user, MaterialDetailEntity material) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(material);
        Optional<MaterialBlacklistEntity> blacklist = findByUser(user);
        if (blacklist.isPresent()) {
            Optional<MaterialBlacklistEntity> clone = blacklist.get().addFansMaterial(material);
            if (clone.isPresent()) {
                getJdbc().update(getExecSql("updateWhiteById", null), clone.get().toMap());
            }
        } else {
            MaterialBlacklistEntity enity = MaterialBlacklistEntity.createAnyListByUser(user, material, false);
            getJdbc().update(getExecSql("insertWhite", null), enity.toMap());
        }
    }

    private void rmeFanslistByUser(LoginUserContext user, MaterialDetailEntity material) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(material);
        Optional<MaterialBlacklistEntity> blacklist = findByUser(user);
        if (!blacklist.isPresent()) return;
        Optional<MaterialBlacklistEntity> clone = blacklist.get().removeFansMaterial(material);
        if (!clone.isPresent()) return;
        if (clone.get().canBeDel()) {
            getJdbc().update(getExecSql("deleteBlackOrWhiteById", null), clone.get().toMap());
        } else {
            getJdbc().update(getExecSql("updateWhiteById", null), clone.get().toMap());
        }
    }

    private void rmeBlacklistByUser(LoginUserContext user, MaterialDetailEntity material) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(material);
        Optional<MaterialBlacklistEntity> blacklist = findByUser(user);
        if (!blacklist.isPresent()) return;
        Optional<MaterialBlacklistEntity> clone = blacklist.get().removeBlackMaterial(material);
        if (!clone.isPresent()) return;
        if (clone.get().canBeDel()) {
            getJdbc().update(getExecSql("deleteBlackOrWhiteById", null), clone.get().toMap());
        } else {
            getJdbc().update(getExecSql("updateBlackById", null), clone.get().toMap());
        }
    }

    public Optional<MaterialBlacklistEntity> findByStore(StoreEntity store) {
        Preconditions.checkNotNull(store);
        Map<String, Integer> params = Maps.newHashMap();
        params.put("id", store.getId());
        params.put("range", 4);
        params.put("companyId", store.getCompanyId().or(-1));
        try {
            MaterialBlacklistEntity enitty = getJdbc().queryForObject(getExecSql("findBlackOrWhiteByStore", null), params,
                    new BlackRowMapperImpl());
            return Optional.fromNullable(enitty);
        } catch (EmptyResultDataAccessException e) {
            return Optional.absent();
        }
    }

    public Optional<MaterialBlacklistEntity> findByUser(LoginUserContext user) {
        Preconditions.checkNotNull(user);
        try {
            if (user.getStore().isPresent()) {
                Map<String, Integer> params = Maps.newHashMap();
                params.put("id", user.getStore().get().getId());
                params.put("range", 4);
                params.put("companyId", user.getStore().get().getCompanyId().or(-1));
                MaterialBlacklistEntity enitty = getJdbc()
                        .queryForObject(getExecSql("findBlackOrWhiteByStore", null), params, new BlackRowMapperImpl());
                return Optional.fromNullable(enitty);
            } else if (user.getOrganization().isPresent()) {
                Map<String, Integer> params = Maps.newHashMap();
                params.put("id", user.getOrganization().get().getId());
                params.put("range", user.getOrganization().get().isCompany() ? 2 : 3);
                params.put("companyId", user.getOrganization().get().getMyCompanyId());
                MaterialBlacklistEntity enitty = getJdbc()
                        .queryForObject(getExecSql("findBlackOrWhiteByStore", null), params, new BlackRowMapperImpl());
                return Optional.fromNullable(enitty);
            } else {
                throw new IllegalArgumentException("账户同时拥有门店与组织...，数据异常...");
            }
        } catch (EmptyResultDataAccessException e) {
            return Optional.absent();
        }
    }

    public Optional<List<MaterialBlacklistEntity>> findByOrgs(Collection<OrganizationEntity> orgs) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(orgs));
        List<Map<String, Object>> datas = Lists.newArrayList();
        for (OrganizationEntity $it : orgs) {
            Map<String, Object> data = Maps.newHashMap();
            data.put("id", $it.getId());
            data.put("range", $it.isCompany() ? 2 : 3);
            data.put("companyId", $it.getMyCompanyId());
            datas.add(data);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("datas", datas);
        List<MaterialBlacklistEntity> enitty = getJdbc().query(getExecSql("findBlackByOrgs", params), params, new BlackRowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(enitty) ? null : enitty);
    }

    @Override
    public Optional<MaterialDetailEntity> findById(Object id) {
        MaterialDetailEntity res = selectById(id);
        return Optional.fromNullable(res);
    }

    public Long createByCsosm(int group, String json, Date deadline) {
        Optional<MaterialGroupEntity> group_ent = findGroupById(group);
        Preconditions.checkState(group_ent.isPresent(), "id=%s对应的分类不存在...");
        long uuid = UUID.randomUUID().toString().hashCode();
        List<MaterialDetailEntity.Material> metals = gson.fromJson(json, MATERIAL_TYPE);
        MaterialDetailEntity entity = MaterialDetailEntity.createByCsosm(uuid, group_ent.get(), metals, deadline);
        getNamedParameterJdbcTemplate().update(getExecSql("insert", null), entity.toMap());
        return uuid;
    }

//    public Long createByAdmin(int group, String json, Date deadline, LoginUserContext user) {
//        Preconditions.checkNotNull(user);
//        Preconditions.checkArgument(!Strings.isNullOrEmpty(json), "内容不可以未空...");
//        Optional<MaterialGroupEntity> group_ent = findGroupById(group);
//        Preconditions.checkState(group_ent.isPresent(), "id=%s对应的分类不存在...");
//        long uuid = UUID.randomUUID().toString().hashCode();
//        List<MaterialDetailEntity.Material> metals = gson.fromJson(json, MATERIAL_TYPE);
//        MaterialDetailEntity entity = MaterialDetailEntity.createByAdmin(uuid, group_ent.get(), metals, deadline, user);
//        getNamedParameterJdbcTemplate().update(getExecSql("insert", null), entity.toMap());
//        return uuid;
//    }

    public Long createByStore(int group, StoreEntity store, String json, Date deadline, LoginUserContext user) {
        Preconditions.checkNotNull(store);
        Preconditions.checkNotNull(user);
        Preconditions.checkState(user.getCompany().isPresent());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(json), "内容不可以未空...");
        Preconditions.checkState(store.getCompanyId().isPresent());
        Optional<MaterialGroupEntity> group_ent = findGroupById(group);
        Preconditions.checkState(group_ent.isPresent(), "id=%s对应的分类不存在...");
        long uuid = UUID.randomUUID().toString().hashCode();
        List<MaterialDetailEntity.Material> metals = gson.fromJson(json, MATERIAL_TYPE);
        MaterialDetailEntity entity = MaterialDetailEntity.createByStore(uuid, group_ent.get(), store, metals, deadline, user);
        getJdbc().update(getExecSql("insert", null), entity.toMap());
        return uuid;
    }

    public Long createByOrg(int group, OrganizationEntity organization, String json, Date deadline, LoginUserContext user) {
        Preconditions.checkNotNull(organization);
        Preconditions.checkNotNull(user);
        Preconditions.checkState(user.getCompany().isPresent());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(json), "内容不可以未空...");
        long uuid = UUID.randomUUID().toString().hashCode();
        Optional<MaterialGroupEntity> group_ent = findGroupById(group);
        Preconditions.checkState(group_ent.isPresent(), "id=%s对应的分类不存在...");
        List<MaterialDetailEntity.Material> metals = gson.fromJson(json, MATERIAL_TYPE);
        MaterialDetailEntity entity = MaterialDetailEntity.createByOrg(uuid, group_ent.get(), organization, metals, deadline, user);
        getJdbc().update(getExecSql("insert", null), entity.toMap());
        return uuid;
    }

    public void switchStatus(Long materialId, LoginUserContext userContext, boolean status) {
        Preconditions.checkNotNull(materialId);
        Optional<MaterialDetailEntity> materialDetail = findById(materialId);
        Preconditions.checkState(materialDetail.isPresent(), "不存在 %s 对应的素材...");
        if (materialDetail.get().isSameOrg(userContext)) {
            checkUser(userContext, materialDetail.get());
            Optional<MaterialDetailEntity> clone = status ? materialDetail.get().enabled() : materialDetail.get().disabled();
            if (clone.isPresent())
                getJdbc().update(getExecSql("switchStatus", null), clone.get().toMap());
        } else {
            Preconditions.checkState(!userContext.getEmployee().isAdmin(), "超管不支持操作非通用素材...");
            if (status) {
                rmeBlacklistByUser(userContext, materialDetail.get());
            } else {
                addBlacklistByUser(userContext, materialDetail.get());
            }
        }
    }

    public void switchFansStatus(Long materialId, LoginUserContext userContext, boolean status) {
        Preconditions.checkNotNull(materialId);
        Optional<MaterialDetailEntity> materialDetail = findById(materialId);
        Preconditions.checkState(materialDetail.isPresent(), "不存在 %s 对应的素材...");
        if (status) {
            addFansListByUser(userContext, materialDetail.get());
        } else {
            rmeFanslistByUser(userContext, materialDetail.get());
        }
    }

    public void editInfo(Long materialId, LoginUserContext userContext, String json, Date deadline) {
        Preconditions.checkNotNull(materialId);
        Optional<MaterialDetailEntity> materialDetail = findById(materialId);
        Preconditions.checkState(materialDetail.isPresent(), "不存在 %s 对应的素材...");
        checkUser(userContext, materialDetail.get());
        List<MaterialDetailEntity.Material> metals = gson.fromJson(json, MATERIAL_TYPE);
        Optional<MaterialDetailEntity> clone = materialDetail.get().editInfo(metals, deadline);
        if (clone.isPresent())
            getJdbc().update(getExecSql("editInfo", null), clone.get().toMap());
    }

    private void checkUser(LoginUserContext user, MaterialDetailEntity materialDetail) {
        if (materialDetail.isCsosm()) {
            Preconditions.checkState(user.getEmployee().isAdmin(), "原始素材只能系统管理员才可以编辑....");
        } else {
            Preconditions.checkState(!user.getEmployee().isAdmin(), "超管只能编辑通用素材...");
            Optional<StoreEntity> store = user.getStore();
            Optional<OrganizationEntity> org = user.getOrganization();
            Preconditions.checkState(!(store.isPresent() && org.isPresent()), "当前登陆用户同时属于门店与组织，数据异常...");
            if (materialDetail.isStore()) {
                Preconditions.checkState(store.isPresent(), "当前用户无门店信息，不允许操作...");
                Preconditions.checkState(store.get().getId().equals(materialDetail.getOrgId()), "当前登陆用户与素材所属门店不一致... 非法操作");
            } else if (materialDetail.isOrg() || materialDetail.isCompany()) {
                Preconditions.checkState(org.isPresent(), "当前登陆用户尚未分配组织架构，无法执行该操作...");
                Preconditions.checkState(org.get().getId().equals(materialDetail.getOrgId()), "当前登陆用户与素材所属组织不一致... 非法操作");
            }
        }
    }

    public Optional<MaterialGroupEntity> findGroupById(Integer id) {
        Optional<List<MaterialGroupEntity>> list = loadAllGroups();
        if (!list.isPresent()) return Optional.absent();
        MaterialGroupEntity exits = null;
        for (MaterialGroupEntity $it : list.get()) {
            if ($it.getId().equals(id)) {
                exits = $it;
                break;
            }
        }
        return Optional.fromNullable(exits);
    }

    @SuppressWarnings("unchecked")
    public Optional<List<MaterialGroupEntity>> loadAllGroups() {
        final String cache_key = String.format("%s_group_all", getModel());
        if (getCache().isPresent()) {
            List<MaterialGroupEntity> list = (List<MaterialGroupEntity>) getCache().get().getIfPresent(cache_key);
            if (CollectionUtils.isNotEmpty(list)) return Optional.of(list);
        }
        List<MaterialGroupEntity> list = getJdbc().query(getExecSql("loadAllGroups", null), new GroupRowMapperImpl());
        if (CollectionUtils.isEmpty(list)) return Optional.absent();
        if (getCache().isPresent()) getCache().get().put(cache_key, list);
        return Optional.of(list);
    }

    @Override
    protected ResultSetExtractor<MaterialDetailEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class GroupRowMapperImpl implements RowMapper<MaterialGroupEntity> {
        @Override
        public MaterialGroupEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildGroup(resultSet);
        }
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<MaterialDetailEntity> {

        @Override
        public MaterialDetailEntity extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return buildDetail(resultSet);
            }
            return null;
        }
    }

    class RowMapperImpl implements RowMapper<MaterialDetailEntity> {
        @Override
        public MaterialDetailEntity mapRow(ResultSet res, int i) throws SQLException {
            return buildDetail(res);
        }
    }

    class BlackRowMapperImpl implements RowMapper<MaterialBlacklistEntity> {
        @Override
        public MaterialBlacklistEntity mapRow(ResultSet res, int i) throws SQLException {
            return buildBlack(res);
        }
    }

    private MaterialBlacklistEntity buildBlack(ResultSet resultSet) throws SQLException {
        String blacklist = resultSet.getString("blacklist");

        Set<Long> blacklist_set = Sets.newHashSet();
        if (!Strings.isNullOrEmpty(blacklist)) {
            for (String $it : StringUtils.split(blacklist, ',')) blacklist_set.add(Long.valueOf($it));
        }
        String whitelist = resultSet.getString("whitelist");
        Set<Long> whitelist_set = Sets.newHashSet();
        if (!Strings.isNullOrEmpty(whitelist)) {
            for (String $it : StringUtils.split(whitelist, ',')) whitelist_set.add(Long.valueOf($it));
        }
        return new MaterialBlacklistEntity(resultSet.getInt("id"), resultSet.getInt("range"), resultSet.getInt("companyId"),
                blacklist_set, whitelist_set);
    }

    private MaterialDetailEntity buildDetail(ResultSet resultSet) throws SQLException {
        String json = resultSet.getString("context");
        List<MaterialDetailEntity.Material> metals = gson.fromJson(json, MATERIAL_TYPE);

        return new MaterialDetailEntity(resultSet.getLong("id"), resultSet.getInt("groupVal"),
                resultSet.getInt("range"), resultSet.getInt("orgId"), resultSet.getInt("companyId"),
                resultSet.getInt("type"), resultSet.getInt("size"), resultSet.getInt("enabled") == 1,
                resultSet.getDate("deadline"), metals, resultSet.getInt("useTimes"));
    }

    private MaterialGroupEntity buildGroup(ResultSet resultSet) throws SQLException {
        return new MaterialGroupEntity(resultSet.getInt("id"), resultSet.getString("groupName"),
                resultSet.getInt("groupType"));
    }

    public void incrementUseTimes(Long id) {
        MaterialDetailEntity material = loadById(id);
        material.incrementUseTimes();
        getJdbc().update(getExecSql("incrementUseTimes", null), material.toMap());
    }
}
