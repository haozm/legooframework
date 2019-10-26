package com.csosm.module.webchat.entity;

import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.EmployeeEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class WechatSignAction extends BaseEntityAction<WechatSignEntity> {

    protected WechatSignAction() {
        super("wechatSign", null);
    }

    @Deprecated
    @Override
    public Optional<WechatSignEntity> findById(Object id) {
        return super.findById(id);
    }

    @Deprecated
    @Override
    public WechatSignEntity loadById(Object id) {
        return super.loadById(id);
    }

    public void sign(EmployeeEntity employee, WebChatUserEntity weixin) {
        Objects.requireNonNull(employee);
        Objects.requireNonNull(weixin);
        Optional<WechatSignEntity> signOpt = findByWeixin(weixin);
        if (signOpt.isPresent()) {
            if (signOpt.get().hasBeSigned(employee)) return;
            updateWechatSign(signOpt.get().changeSigner(employee));
            return;
        }
        addWechatSign(employee, weixin);
    }

    public void sign(EmployeeEntity employee, Collection<WebChatUserEntity> weixins) {
        Objects.requireNonNull(employee);
        if (CollectionUtils.isEmpty(weixins)) return;
        List<WebChatUserEntity> unsignWeixins = loadUnSignedWeixins(employee, weixins);
        if (CollectionUtils.isEmpty(unsignWeixins)) return;
        clearWechatSigns(employee, unsignWeixins);
        addWechatSigns(employee, unsignWeixins);
    }

    /**
     * 添加认领记录
     *
     * @param sign
     */
    private void addWechatSign(EmployeeEntity employee, WebChatUserEntity weixin) {
        WechatSignEntity sign = new WechatSignEntity(employee, weixin);
        String execSql = getExecSql("insert", sign.toMap());
        getNamedParameterJdbcTemplate().update(execSql, sign.toMap());
    }

    private void clearWechatSigns(EmployeeEntity employee, List<WebChatUserEntity> weixins) {
        Map<String, Object> params = employeeCheckAndToMap(employee);
        Set<String> weixinIds = Sets.newHashSet();
        for (WebChatUserEntity weixin : weixins) weixinIds.add(weixin.getUserName());
        params.put("weixinIds", weixinIds);
        String sql = getExecSql("delete_signs", params);
        getNamedParameterJdbcTemplate().update(sql, params);
    }

    private void addWechatSigns(EmployeeEntity employee, List<WebChatUserEntity> weixins) {
        List<WechatSignEntity> signs = Lists.newArrayListWithCapacity(weixins.size());
        for (WebChatUserEntity weixin : weixins) signs.add(new WechatSignEntity(employee, weixin));
        batchInsert(signs);
    }

    private void batchInsert(List<WechatSignEntity> mapping) {
        getJdbcTemplate().batchUpdate(getExecSql("batch_insert", null), mapping, 100,
                new ParameterizedPreparedStatementSetter<WechatSignEntity>() {
                    @Override
                    public void setValues(PreparedStatement ps, WechatSignEntity entity) throws SQLException {
                        //id,batch_id,serial_num,life_status,createUserId
                        ps.setInt(1, entity.getEmployeeId());
                        ps.setString(2, entity.getWeixinId());
                        ps.setInt(3, entity.getStoreId());
                        ps.setInt(4, entity.getCompanyId());
                        ps.setObject(5, entity.getCreateUserId());
                    }
                });
    }

    /**
     * 更新认领记录
     *
     * @param sign
     */
    private void updateWechatSign(WechatSignEntity sign) {
        String execSql = getExecSql("update", sign.toMap());
        getNamedParameterJdbcTemplate().update(execSql, sign.toMap());
    }

    private Map<String, Object> employeeCheckAndToMap(EmployeeEntity employee) {
        Objects.requireNonNull(employee);
        Preconditions.checkArgument(employee.getStoreId().isPresent(), String.format("职员[%s] 无门店信息", employee.getId()));
        Preconditions.checkArgument(employee.getCompanyId().isPresent(), String.format("职员[%s] 无门店信息", employee.getId()));
        Map<String, Object> params = Maps.newHashMap();
        params.put("employeeId", employee.getId());
        params.put("storeId", employee.getStoreId().get());
        params.put("companyId", employee.getCompanyId().get());
        return params;
    }

    private Map<String, Object> weixinCheckAndToMap(WebChatUserEntity weixin) {
        Objects.requireNonNull(weixin);
        Map<String, Object> params = Maps.newHashMap();
        params.put("weixinId", weixin.getUserName());
        params.put("storeId", weixin.getStoreId());
        params.put("companyId", weixin.getCompanyId());
        return params;
    }

    /**
     * 获取导购微信认领信息
     *
     * @param employee 导购
     * @return
     */
    public List<WechatSignEntity> findByEmployee(EmployeeEntity employee) {
        return null;
    }

    /**
     * 通过微信查找认领信息
     *
     * @param weixin 微信
     * @return
     */
    public Optional<WechatSignEntity> findByWeixin(WebChatUserEntity weixin) {
        Map<String, Object> params = weixinCheckAndToMap(weixin);
        String sql = getExecSql("find_by_weixin", params);
        WechatSignEntity entity = getNamedParameterJdbcTemplate().query(sql, params, getResultSetExtractor());
        if (null == entity) return Optional.absent();
        return Optional.of(entity);
    }

    public Optional<WechatSignEntity> findByEmployeeAndWeixin(EmployeeEntity employee, WebChatUserEntity weixin) {
        return null;
    }

    /**
     * 判断导购是否认领某个微信好友
     *
     * @param employee
     * @param weixin
     * @return
     */
    public boolean hasSigned(EmployeeEntity employee, WebChatUserEntity weixin) {
        return false;
    }

    /**
     * 区分多个微信好友，那些是导购未认领的，哪些是导购已认领的
     *
     * @param employee
     * @param weixins
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<WebChatUserEntity>> partSignWeixins(EmployeeEntity employee, Collection<WebChatUserEntity> weixins) {
        if (CollectionUtils.isEmpty(weixins)) return Collections.EMPTY_MAP;
        Map<String, Object> params = employeeCheckAndToMap(employee);
        String sql = getExecSql("find_weixinIds_by_employeeId", params);
        String weixinIdsStr = getNamedParameterJdbcTemplate().queryForObject(sql, params, String.class);
        Set<String> weixinIds = Sets.newHashSet();
        if (!Strings.isNullOrEmpty(weixinIdsStr)) weixinIds = Sets.newHashSet(weixinIdsStr.split(","));
        Map<String, List<WebChatUserEntity>> partMap = Maps.newHashMap();
        List<WebChatUserEntity> unsignedWeixins = Lists.newArrayList();
        List<WebChatUserEntity> signedWeixins = Lists.newArrayList();
        for (WebChatUserEntity weixin : weixins) {
            if (weixinIds.contains(weixin.getId())) {
                signedWeixins.add(weixin);
                continue;
            }
            unsignedWeixins.add(weixin);
        }
        partMap.put(WechatSignEntity.UNSIGNED, unsignedWeixins);
        partMap.put(WechatSignEntity.SIGNED, signedWeixins);
        return partMap;
    }

    /**
     * 获取多个微信好友，已经被导购认领的微信好友
     *
     * @param employee
     * @param weixins
     * @return
     */
    public List<WebChatUserEntity> loadSignedWeixins(EmployeeEntity employee, Collection<WebChatUserEntity> weixins) {
        Map<String, List<WebChatUserEntity>> partMap = partSignWeixins(employee, weixins);
        return partMap.get(WechatSignEntity.SIGNED);
    }

    /**
     * 获取多个微信好友，未被导购认领的微信好友
     *
     * @param employee
     * @param weixins
     * @return
     */
    public List<WebChatUserEntity> loadUnSignedWeixins(EmployeeEntity employee, Collection<WebChatUserEntity> weixins) {
        Map<String, List<WebChatUserEntity>> partMap = partSignWeixins(employee, weixins);
        return partMap.get(WechatSignEntity.UNSIGNED);
    }

    @Override
    protected ResultSetExtractor<WechatSignEntity> getResultSetExtractor() {
        return new ResultSetExtractor<WechatSignEntity>() {

            @Override
            public WechatSignEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next()) return WechatSignEntity.valueOf(rs);
                return null;
            }
        };
    }

}
