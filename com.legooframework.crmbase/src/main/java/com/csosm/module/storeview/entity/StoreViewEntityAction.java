package com.csosm.module.storeview.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class StoreViewEntityAction extends BaseEntityAction<StoreViewEntity> {

    private static final Logger logger = LoggerFactory.getLogger(StoreViewEntityAction.class);

    public StoreViewEntityAction() {
        super("StoreViewEnity", "defCache");
    }

    public boolean hasStoreView(EmployeeEntity employee) {
        Objects.requireNonNull(employee);
        Map<String, Object> params = Maps.newHashMap();
        params.put("empId", employee.getId());
        int count = getJdbc().queryForObject(getExecSql("count_emp_view", params), params, Integer.class);
        return count >= 1;
    }

    /**
     * 获取指定 分组实体的全部分组实体列表
     *
     * @param nodeId 分组实体
     * @return 分组实体列表
     */
    public Optional<List<StoreViewEntity>> loadSubNodes(String nodeId) {
        Preconditions.checkNotNull(nodeId, "入参 nodeId 不可以为空值 .");
        Optional<StoreViewEntity> entityOpt = findById(nodeId);
        if (!entityOpt.isPresent()) return Optional.absent();
        List<StoreViewEntity> treeNodes = loadAllTreeNodes(entityOpt.get().getTreeType(), entityOpt.get().getOwnerId(),
                entityOpt.get().getCompanyId());
        if (CollectionUtils.isEmpty(treeNodes)) return Optional.absent();
        List<StoreViewEntity> subNodes = Lists.newArrayList();
        java.util.Optional<StoreViewEntity> root = treeNodes.stream().filter(x -> x.getId().equals(nodeId)).findFirst();
        Preconditions.checkState(root.isPresent());
        if (root.get().isRoot()) {
            return Optional.fromNullable(subNodes);
        } else {
            LinkedList<StoreViewEntity> linkedList = Lists.newLinkedList();
            linkedList.addLast(root.get());
            StoreViewEntity cursor;
            while (!linkedList.isEmpty()) {
                cursor = linkedList.pop();
                // 尚未含自身
                for (StoreViewEntity it : treeNodes) {
                    if (cursor.isParentNode(it)) {
                        linkedList.addLast(it);
                        subNodes.add(it);
                    }
                }
            }
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadSubNodes(%s) res %s", nodeId, subNodes));
        return Optional.fromNullable(CollectionUtils.isEmpty(subNodes) ? null : subNodes);
    }

    /**
     * 加载该用户定义的树  如果没有根节点则自动生成 根节点
     *
     * @param employee 用户信息
     * @return 实体列表
     */
    public List<StoreViewEntity> loadDataPermissionTree(EmployeeEntity employee, LoginUserContext user) {
        Preconditions.checkNotNull(employee, "参数 EmployeeEntity employee 不可为空值...");
        StoreViewEntity root = addDataPermissionTreeRootNode(employee, user);
        return loadAllTreeNodes(StoreViewEntity.TYPE_DATA_PERMISSION, root.getOwnerId(), employee.getCompanyId().or(-1));
    }

    /**
     * 加载该用户定义的树  如果没有根节点则自动生成 根节点
     *
     * @param company 公司信息
     * @return 实体列表
     */
    public List<StoreViewEntity> loadSmsRechargeTree(OrganizationEntity company, LoginUserContext user) {
        Preconditions.checkNotNull(company, "参数 company 不可为空值...");
        StoreViewEntity root = addSmsRechargeTreeRoot(company, user);
        return loadAllTreeNodes(StoreViewEntity.TYPE_SMS_RECHARGE, null, root.getCompanyId());
    }

    /**
     * 新增根目录节点
     *
     * @param employee
     * @param loginUser
     * @return
     */
    StoreViewEntity addDataPermissionTreeRootNode(EmployeeEntity employee, LoginUserContext loginUser) {
        Preconditions.checkNotNull(employee, "新增透视图拥有者不可以为空.");
        StoreViewEntity instance = StoreViewEntity.createDataPermissionRoot(employee, loginUser);
        Optional<StoreViewEntity> storeViewOpt = findTreeRoot(employee.getId(), employee.getCompanyId().or(-1),
                StoreViewEntity.TYPE_DATA_PERMISSION);
        if (storeViewOpt.isPresent()) return storeViewOpt.get();
        int res = getJdbc().update(getExecSql("insert", null), instance.toMap());
        Preconditions.checkState(1 == res, "新增职员%s数据透视图失败...", employee.getUserName());
        if (getCache().isPresent())
            getCache().get().invalidate(String.format("%s_employee_%s", getModel(), employee.getId()));
        storeViewOpt = findTreeRoot(employee.getId(), employee.getCompanyId().or(-1),
                StoreViewEntity.TYPE_DATA_PERMISSION);
        Preconditions.checkState(storeViewOpt.isPresent());
        addRootEvent(storeViewOpt.get());
        return storeViewOpt.get();
    }

    /**
     * 短信充值树新增，如果没有则创建一颗
     *
     * @param company
     * @param loginUser
     * @return
     */
    StoreViewEntity addSmsRechargeTreeRoot(OrganizationEntity company, LoginUserContext loginUser) {
        Preconditions.checkNotNull(company, "公司充值透视图拥有者不可以为空.");
        StoreViewEntity instance = StoreViewEntity.createSmsRechargeRoot(company, loginUser);
        Optional<StoreViewEntity> storeViewOpt = findTreeRoot(null, company.getId(), StoreViewEntity.TYPE_SMS_RECHARGE);
        if (storeViewOpt.isPresent()) return storeViewOpt.get();
        int res = getJdbc().update(getExecSql("insert", null), instance.toMap());
        Preconditions.checkState(1 == res, "新增公司%s充值透视图失败...", company.getName());
        if (getCache().isPresent())
            getCache().get().invalidate(String.format("%s_company_%s", getModel(), company.getId()));
        storeViewOpt = findTreeRoot(null, company.getId(), StoreViewEntity.TYPE_SMS_RECHARGE);
        Preconditions.checkState(storeViewOpt.isPresent(), "新增公司%s充值透视图失败...", company.getName());
        return storeViewOpt.get();
    }

    @SuppressWarnings("unchecked")
    private List<StoreViewEntity> loadAllTreeNodes(int treeType, Integer ownerId, Integer companyId) {
        final String cache_key = treeType == StoreViewEntity.TYPE_SMS_RECHARGE ?
                String.format("%s_company_%s", getModel(), companyId) :
                String.format("%s_employee_%s", getModel(), ownerId);
        if (getCache().isPresent()) {
            Object value = getCache().get().getIfPresent(cache_key);
            if (null != value) return (List<StoreViewEntity>) value;
        }

        Map<String, Object> params = Maps.newHashMap();
        params.put("treeType", treeType);
        params.put("companyId", companyId);
        if (StoreViewEntity.TYPE_DATA_PERMISSION == treeType) {
            params.put("ownerId", ownerId);
        }

        List<StoreViewEntity> allTreeNodes = getJdbc().query(getExecSql("loadAllTreeNodes", params), params,
                new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllTreeNodes(%s,%s,%s) allTreeNodes size is %s", treeType, ownerId, companyId,
                    CollectionUtils.isEmpty(allTreeNodes) ? 0 : allTreeNodes.size()));
        if (getCache().isPresent() && !CollectionUtils.isEmpty(allTreeNodes)) {
            getCache().get().put(cache_key, allTreeNodes);
        }
        return allTreeNodes;
    }

    /**
     * 移除 指定 分组节点 下的 门店信息
     *
     * @param nodeId 分组节点ID
     * @param stores 待移除的门店实体列表,当门店列表为空时 忽略本次操作
     */
    public void removeStoreFromGroupNode(Object nodeId, List<StoreEntity> stores) {
        if (CollectionUtils.isEmpty(stores)) return;
        Optional<StoreViewEntity> optional = findById(nodeId);
        Preconditions.checkState(optional.isPresent(), "ID=%s对应的节点不存在...", nodeId);
        Set<Integer> exitIds = optional.get().exitsStores(stores);
        if (CollectionUtils.isEmpty(exitIds)) return;
        java.util.Optional<StoreViewEntity> instance = optional.get().removeStores(stores);
        if (!instance.isPresent()) return;
        int res = getJdbc().update(getExecSql("updateStores", null), instance.get().toMap());
        Preconditions.checkState(1 == res, "删除门店失败...");
        if (instance.get().isSmsRechargeTree()) {
            if (getCache().isPresent())
                getCache().get().invalidate(String.format("%s_company_%s", getModel(), optional.get().getCompanyId()));
            return;
        }
        // 判断当前整棵树是否存在存在删除的节点 如果存在则需要过滤
        if (getCache().isPresent())
            getCache().get().invalidate(String.format("%s_employee_%s", getModel(), optional.get().getOwnerId()));
        List<StoreViewEntity> tree = loadAllTreeNodes(StoreViewEntity.TYPE_DATA_PERMISSION, optional.get().getOwnerId(),
                instance.get().getCompanyId());
        Set<Integer> exitsStoreIds = Sets.newHashSet();
        tree.stream().filter(StoreViewEntity::hasStores).forEach(c -> exitsStoreIds.addAll(c.getStoreIds()));
        Sets.SetView<Integer> exitView = Sets.intersection(exitIds, exitsStoreIds);
        Set<Integer> removedIds = Sets.newHashSet();
        if (!exitView.isEmpty()) {
            removedIds.addAll(exitView);
            if (!CollectionUtils.isEmpty(removedIds)) {
                exitIds.removeAll(removedIds);
                removedIds.clear();
            }
        }
        removedIds.addAll(exitIds);
        List<StoreEntity> removeStos = stores.stream().filter(x -> removedIds.contains(x.getId()))
                .collect(Collectors.toList());
        // 级联操作 删除
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", optional.get().getCompanyId());
        params.put("ownerId", optional.get().getCreateUserId());
        List<StoreViewEntity> storeViews = getJdbc().query(getExecSql("findStoreViewsByOwners", null), params,
                new RowMapperImpl());

        if (CollectionUtils.isEmpty(storeViews) || CollectionUtils.isEmpty(removeStos)) {
            if (getCache().isPresent())
                getCache().get().invalidate(String.format("%s_employee_%s", getModel(), optional.get().getOwnerId()));
        } else {
            final List<StoreViewEntity> removed = Lists.newArrayList();
            for (StoreViewEntity $it : storeViews) $it.removeStores(removeStos).ifPresent(removed::add);
            if (CollectionUtils.isEmpty(removed)) return;
            Objects.requireNonNull(getJdbcTemplate()).batchUpdate(getExecSql("batchUpdateStores", null), removed, 128,
                    (ps, stv) -> {
                        ps.setObject(1, stv.hasStores() ? Joiner.on(',').join(stv.getStoreIds()) : null);
                        ps.setObject(2, stv.getId());
                    });
            if (getCache().isPresent()) {
                getCache().get().invalidateAll();
            }
        }
    }

    /**
     * 同步 树结构 信息 用于级联删除操作
     *
     * @param root
     */
    private void addRootEvent(StoreViewEntity root) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", root.getCompanyId());
        params.put("ownerId", root.getCreateUserId());
        String treePath = null;
        try {
            treePath = getJdbc().queryForObject(getExecSql("findTreePathByOwner", null), params, String.class);
        } catch (EmptyResultDataAccessException e) {
            treePath = null;
        }
        StoreViewStructureEntity instance = new StoreViewStructureEntity(root, treePath);
        int res = getJdbc().update(getExecSql("insertTreeStructure", null), instance.toMap());
        Preconditions.checkState(res == 1, "保存结构失败");
    }

    /**
     * 新增门店到 组织下
     *
     * @param nodeId    分组节点ID
     * @param stores    待新增的门店实体列表,当门店列表为空时 忽略本次操作
     * @param loginUser 操作用户信息
     */
    public void addStoresToNode(Object nodeId, List<StoreEntity> stores, LoginUserContext loginUser) {
        Preconditions.checkNotNull(nodeId, "新增的组织节点ID不可以为空.");
        if (CollectionUtils.isEmpty(stores)) return;
        Optional<StoreViewEntity> optional = findById(nodeId);
        Preconditions.checkState(optional.isPresent(), "ID=%s对应的节点不存在...", nodeId);

        if (optional.get().isDataPermissionTree()) {
            StoreViewEntity instance = optional.get().addStores(stores, loginUser.getUserId());
            if (optional.get().equals(instance)) return;
            int res = getJdbc().update(getExecSql("updateStores", null), instance.toMap());
            Preconditions.checkState(1 == res);
            if (getCache().isPresent()) {
                getCache().get().invalidate(String.format("%s_employee_%s", getModel(), optional.get().getOwnerId()));
            }
        } else if (optional.get().isSmsRechargeTree()) {
            StoreViewEntity instance = optional.get().addStores(stores, loginUser.getUserId());
            if (optional.get().equals(instance)) return;
            List<Integer> storeIds = stores.stream().mapToInt(BaseEntity::getId).boxed().collect(Collectors.toList());
            List<StoreViewEntity> list = loadAllTreeNodes(StoreViewEntity.TYPE_SMS_RECHARGE, null, optional.get().getCompanyId());
            java.util.Optional<StoreViewEntity> opt = list.stream().filter(x -> x.containsAny(storeIds)).findFirst();
            Preconditions.checkState(!opt.isPresent(), "存在门店在其他组织下，不可以重复添加...");
            int res = getJdbc().update(getExecSql("updateStores", null), instance.toMap());
            Preconditions.checkState(1 == res);
            if (getCache().isPresent()) {
                getCache().get().invalidate(String.format("%s_company_%s", getModel(), instance.getCompanyId()));
            }
        } else {
            throw new RuntimeException("非法的树节点格式.....");
        }
    }

    /**
     * 修改指定节点的分组名称
     *
     * @param nodeId    分组ID
     * @param nodeName  新的Name
     * @param loginUser 修改人
     */
    public void editGroupNodeName(String nodeId, String nodeName, String nodeDesc, LoginUserContext loginUser) {
        Preconditions.checkNotNull(loginUser);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(nodeName), "待修改的节点名称不可以为空.");
        Optional<StoreViewEntity> optional = findById(nodeId);
        Preconditions.checkState(optional.isPresent(), "ID=%s对应的节点不存在...", nodeId);
        if (optional.get().getNodeName().equals(nodeName) && optional.get().getNodeDesc().equals(nodeDesc)) return;
        StoreViewEntity instance = optional.get().changeInfo(nodeName, nodeDesc, loginUser);
        if (optional.get().equals(instance)) return;
        int res = getJdbc().update(getExecSql("editGroupNodeName", null), instance.toMap());
        Preconditions.checkState(1 == res);
        if (getCache().isPresent()) {
            if (optional.get().isSmsRechargeTree()) {
                getCache().get().invalidate(String.format("%s_company_%s", getModel(), optional.get().getCompanyId()));
            } else {
                getCache().get().invalidate(String.format("%s_employee_%s", getModel(), optional.get().getOwnerId()));
            }
        }
    }

    @Override
    public Optional<StoreViewEntity> findById(Object id) {
        return Optional.fromNullable(super.selectById(id));
    }

    /**
     * 新增下级节点
     *
     * @param parentId  上级节点ID
     * @param nodeName  分组名称
     * @param loginUser 操作用户
     * @return 新增节点ID
     */
    public String addSubGroupNode(String parentId, String nodeName, String nodeDesc, LoginUserContext loginUser) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(parentId), "上级节点不可以为空值.");
        Optional<StoreViewEntity> parentNode = findById(parentId);
        Preconditions.checkState(parentNode.isPresent(), "Id = %d 对应的节点不存在...");
        StoreViewEntity entity = new StoreViewEntity(nodeName, nodeDesc, parentNode.get(), loginUser);
        int res = getJdbc().update(getExecSql("insert", null), entity.toMap());
        Preconditions.checkState(1 == res);
        if (getCache().isPresent()) {
            if (parentNode.get().isSmsRechargeTree()) {
                getCache().get().invalidate(String.format("%s_company_%s", getModel(), parentNode.get().getCompanyId()));
            } else {
                getCache().get().invalidate(String.format("%s_employee_%s", getModel(), entity.getOwnerId()));
            }
        }
        return entity.getId();
    }

    /**
     * 删除指定 ID的 分组信息
     *
     * @param nodeId    待删除的 分组信息ID
     * @param loginUser 操作用户
     */
    public void removeSubNodeById(Object nodeId, LoginUserContext loginUser) {
        Preconditions.checkNotNull(loginUser, "操作用户为空，不可以执行删除操作");
        Preconditions.checkNotNull(nodeId, "当前节点ID不可以为空值.");
        Optional<StoreViewEntity> instance = findById(nodeId);
        Preconditions.checkState(instance.isPresent(), "Id=%d 对应的节点不存在...", nodeId);
        Preconditions.checkState(!instance.get().isRoot(), "根节点不允许删除...");

        Preconditions.checkState(instance.get().isDataPermissionTree(), "充值透视图不允许删除...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", instance.get().getId());
        Long count = getJdbc().queryForObject(getExecSql("exitSubNodes", null), params, Long.class);
        Preconditions.checkState(count == null || count == 0L, "当前节点 %s 包含下级节点，不允许删除.",
                instance.get().getNodeName());
        Preconditions.checkState(!instance.get().hasStores(), "当前节点 %s 包含下级节点，不允许删除.",
                instance.get().getNodeName());

        int res = getJdbc().update(sqlMetaEntityFactory.getExecSql(getModel(), "removeSubGroupNodeById", null),
                params);
        Preconditions.checkState(1 == res);
        if (logger.isDebugEnabled())
            logger.debug(String.format("User %s 删除 TreeView 节点 %s", loginUser.getEmployee().getUserName(), instance.get()));
        if (getCache().isPresent()) {
            if (instance.get().isSmsRechargeTree()) {
                getCache().get().invalidate(String.format("%s_company_%s", getModel(), instance.get().getCompanyId()));
            } else {
                getCache().get().invalidate(String.format("%s_employee_%s", getModel(), instance.get().getOwnerId()));
            }
        }
    }

    /**
     * 查询不同类型树的根节点
     *
     * @return
     */
    private Optional<StoreViewEntity> findTreeRoot(Integer employeeId, Integer companyId, int treeType) {
        Preconditions.checkNotNull(companyId);
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        if (StoreViewEntity.TYPE_DATA_PERMISSION == treeType) {
            Preconditions.checkNotNull(employeeId, "透视图拥有者不可以为空.");
            params.put("ownerId", employeeId);
        }
        params.put("treeType", treeType);
        try {
            StoreViewEntity entity = getJdbc().queryForObject(getExecSql("findTreeRoot", params), params, new RowMapperImpl());
            if (logger.isDebugEnabled())
                logger.debug(String.format("findTreeRoot(%s,%s,%s) res %s", employeeId, companyId, treeType, entity));
            return Optional.fromNullable(entity);
        } catch (EmptyResultDataAccessException e) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("findTreeRoot(%s,%s,%s) res null", employeeId, companyId, treeType));
            return Optional.absent();
        }
    }

    class RowMapperImpl implements RowMapper<StoreViewEntity> {
        @Override
        public StoreViewEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    private StoreViewEntity buildByResultSet(ResultSet resultSet) throws SQLException {
//        String id, Integer ownerId, String pid, String nodeName,
//                String storeIds, String nodeDesc, Integer companyId, Object createUserId
        return new StoreViewEntity(resultSet.getString("id"), resultSet.getInt("ownerId"), resultSet.getInt("treeType"),
                resultSet.getString("pId"),
                resultSet.getString("nodeName"), resultSet.getString("storeIds"), resultSet.getString("storeInfo"),
                resultSet.getString("nodeDesc"),
                resultSet.getInt("companyId"), resultSet.getObject("createUserId"));
    }

    @Override
    protected ResultSetExtractor<StoreViewEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<StoreViewEntity> {

        @Override
        public StoreViewEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return buildByResultSet(resultSet);
            }
            return null;
        }
    }
}
