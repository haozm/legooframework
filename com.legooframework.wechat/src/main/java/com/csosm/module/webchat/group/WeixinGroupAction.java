package com.csosm.module.webchat.group;

import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.webchat.WeixinGroupService;
import com.csosm.module.webchat.entity.WebChatUserEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

/**
 * 新版本的好友分组
 *
 * @author Administrator
 */
public class WeixinGroupAction extends BaseEntityAction<WeixinGroupEntity> {

    private static final Logger logger = LoggerFactory.getLogger(WeixinGroupService.class);

    private ThreadPoolTaskExecutor executor;

    public void setExecutor(ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    protected WeixinGroupAction() {
        super("group", null);
    }

    @Deprecated
    @Override
    public Optional<WeixinGroupEntity> findById(Object id) {
        throw new RuntimeException("该方法已废弃请勿使用");
    }

    @Deprecated
    @Override
    public WeixinGroupEntity loadById(Object id) {
        throw new RuntimeException("该方法已废弃请勿使用");
    }

    public boolean existInGroup(EmployeeEntity employee, WebChatUserEntity weixin) {
        Objects.requireNonNull(employee);
        Objects.requireNonNull(weixin);
        if (!employee.getStoreId().isPresent())
            return false;
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", employee.getStoreId().get());
        params.put("guideId", employee.getId());
        params.put("weixinId", weixin.getUserName());
        return existInGroup(params);
    }

    private boolean existInGroup(Map<String, Object> params) {
        String sql = getExecSql("count_weixin_group", params);
        Map<String, Object> resultMap = getNamedParameterJdbcTemplate().queryForMap(sql, params);
        Integer count = MapUtils.getInteger(resultMap, "count");
        if (count >= 1)
            return true;
        return false;
    }

    /**
     * 判断微信好友是否在门店下的分组好友列表内
     *
     * @param store
     * @param group
     * @param weixin
     * @return
     */
    public boolean existInGrantableGroup(StoreEntity store, WeixinGroupEntity group, WebChatUserEntity weixin) {
        Objects.requireNonNull(group);
        Objects.requireNonNull(weixin);
        if (group.isAllFriendGroup())
            return true;
        if (group.isNewFriendGroup())
            return existInNewFriendGroup(store, weixin);
        if (group.isCommonGroup())
            return existInCommonGroup(store, group, weixin);
        return false;
    }

    public boolean existUserDefinedGroupName(StoreEntity store, String name) {
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("name", name);
        String sql = getExecSql("count_userdefined_group_name", params);
        Integer count = getNamedParameterJdbcTemplate().queryForObject(sql, params, Integer.class);
        return count > 0;
    }

    /**
     * 为门店添加自定义分组
     *
     * @param store 门店
     * @param name  分组名称
     * @return
     */
    public WeixinGroupEntity addUserDefinedGroup(StoreEntity store, String name) {
        Objects.requireNonNull(store);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "分组名称不能为空");
        Preconditions.checkArgument(!existUserDefinedGroupName(store, name), String.format("组名[%s]已存在", name));
        WeixinGroupEntity group = WeixinGroupEntity.createUserDefinedGroup(store, name);
        String sql = getExecSql("insert_common_group", group.toMap());
        int res = getNamedParameterJdbcTemplate().update(sql, group.toMap());
        Preconditions.checkState(res == 1, "新增分组失败");
        return group;
    }

    /**
     * 修改分组名称
     *
     * @param group 待修改分组
     * @param name  名称
     * @return
     */
    public WeixinGroupEntity modifyGroupName(WeixinGroupEntity group, String name) {
        Objects.requireNonNull(group);
        Preconditions.checkArgument(group.isEditable(), "该分组不允许修改分组名称");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "分组名称不能为空");
        if (group.getName().equals(name))
            return group;
        WeixinGroupEntity clone = group.modifyName(name);
        int res = getNamedParameterJdbcTemplate().update(getExecSql("modify_group_name", clone.toMap()), clone.toMap());
        Preconditions.checkState(res == 1, "修改分组名称失败");
        return clone;
    }

    /**
     * 移除分组包括组好友，组授权信息
     *
     * @param user
     * @param group
     * @return
     */
    public String removeGroup(StoreEntity store, WeixinGroupEntity group) {
        Objects.requireNonNull(group);
        Preconditions.checkState(group.isEditable(), String.format("该分组[%s]不允许移除", group.getId()));
        storeCheck(store);
        Preconditions.checkArgument(group.isEditable(), "该分组不允许移除");
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("storeId", store.getId());
        paramMap.put("groupId", group.getId());
        paramMap.put("companyId", store.getCompanyId().get());
        getNamedParameterJdbcTemplate().update(getExecSql("delete_group", null), paramMap);
        getNamedParameterJdbcTemplate().update(getExecSql("delete_group_friend", null), paramMap);
        getNamedParameterJdbcTemplate().update(getExecSql("delete_groud_guides", null), paramMap);
        return group.getId();
    }

    /**
     * 查找分组的好友ID
     *
     * @param group
     * @return
     */
    @SuppressWarnings("unchecked")
    private Set<String> findFriendIds(WeixinGroupEntity group) {
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("groupId", group.getId());
        String execSql = getExecSql("find_friendIds_by_groupId", paramMap);
        List<Map<String, Object>> friendIdMaps = getNamedParameterJdbcTemplate().queryForList(execSql, paramMap);
        if (CollectionUtils.isEmpty(friendIdMaps))
            return Collections.EMPTY_SET;
        Set<String> friendIds = Sets.newHashSet();
        for (Map<String, Object> map : friendIdMaps)
            friendIds.add(MapUtils.getString(map, "friendId"));
        return friendIds;
    }

    /**
     * 移除好友
     *
     * @param group   分组
     * @param weixins 微信好友列表
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<String> addFriends(WeixinGroupEntity group, List<WebChatUserEntity> weixins) {
        Objects.requireNonNull(group);
        Preconditions.checkState(group.isEditable(), "该分组不允许添加微信好友");
        Set<String> friendIds = Sets.newHashSet();
        if (CollectionUtils.isEmpty(weixins))
            return friendIds;
        for (WebChatUserEntity weixin : weixins)
            friendIds.add(weixin.getUserName());
        friendIds.removeAll(findFriendIds(group));
        if (friendIds.isEmpty())
            return Collections.EMPTY_SET;
        Map[] friendMaps = new Map[friendIds.size()];
        List<String> weixinIds = Lists.newArrayList(friendIds);
        for (int i = 0; i < weixinIds.size(); i++) {
            String friendId = weixinIds.get(i);
            Map<String, Object> friendMap = Maps.newHashMap();
            friendMap.put("id", UUID.randomUUID().toString().replaceAll("-", ""));
            friendMap.put("groupId", group.getId());
            friendMap.put("friendId", friendId);
            friendMap.put("createUserId", -1);
            friendMaps[i] = friendMap;
        }
        String sql = getExecSql("insert_group_friend", null);
        getNamedParameterJdbcTemplate().batchUpdate(sql, friendMaps);
        return friendIds;
    }

    /**
     * 移除分组微信好友
     *
     * @param group   分组
     * @param weixins 微信好友列表
     * @return
     */
    @SuppressWarnings("unchecked")
    public void removeFriends(WeixinGroupEntity group, List<WebChatUserEntity> weixins) {
        Objects.requireNonNull(group);
        Preconditions.checkState(group.isEditable(), "该分组不需要移除好友");
        if (CollectionUtils.isEmpty(weixins))
            return;
        Map[] paramMap = new Map[weixins.size()];
        for (int i = 0; i < weixins.size(); i++) {
            String friendId = weixins.get(i).getId();
            Map<String, Object> map = Maps.newHashMap();
            map.put("friendId", friendId);
            map.put("groupId", group.getId());
            paramMap[i] = map;
        }
        getNamedParameterJdbcTemplate().batchUpdate(getExecSql("delete_friends", null), paramMap);
    }

    /**
     * 从多个组里移除好友
     *
     * @param weixin
     * @param groups
     */
    public void removeFriendFromGroups(WebChatUserEntity weixin, List<WeixinGroupEntity> groups) {
        Objects.requireNonNull(weixin);
        if (CollectionUtils.isEmpty(groups))
            return;
        Map<String, Object> paramMap = Maps.newHashMap();
        List<String> groupIds = Lists.newArrayList();
        for (WeixinGroupEntity group : groups)
            groupIds.add(group.getId());
        paramMap.put("groupIds", groupIds);
        paramMap.put("friendId", weixin.getUserName());
        String execSql = getExecSql("remove_groups_friend", paramMap);
        getNamedParameterJdbcTemplate().update(execSql, paramMap);
    }

    /**
     * 往多个组里添加单个好友
     *
     * @param weixin
     * @param groups
     */
    @SuppressWarnings("unchecked")
    public void addFriendToGroups(WebChatUserEntity weixin, List<WeixinGroupEntity> groups) {
        Objects.requireNonNull(weixin);
        Preconditions.checkArgument(!CollectionUtils.isEmpty(groups), "待添加的组为空");
        Map[] friendMaps = new Map[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            Map<String, Object> friendMap = Maps.newHashMap();
            friendMap.put("id", UUID.randomUUID().toString().replaceAll("-", ""));
            friendMap.put("groupId", groups.get(i).getId());
            friendMap.put("friendId", weixin.getUserName());
            friendMap.put("createUserId", -1);
            friendMaps[i] = friendMap;
        }
        String sql = getExecSql("insert_group_friend", null);
        getNamedParameterJdbcTemplate().batchUpdate(sql, friendMaps);
    }

    /**
     * 移除导购分组授权
     */
    void clearGrantedGroup(StoreEntity store, WeixinGroupEntity group) {
        storeCheck(store);
        Preconditions.checkState(store.getCompanyId().isPresent());
        Objects.requireNonNull(group);
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("storeId", store.getId());
        paramMap.put("groupId", group.getId());
        paramMap.put("companyId", store.getCompanyId().get());
        getNamedParameterJdbcTemplate().update(getExecSql("delete_groud_guides", null), paramMap);
    }

    /**
     * 给导购分配分组权限
     *
     * @param groups    待分配的组
     * @param employees 需要分配的导购
     */
    public void grantToEmployees(final StoreEntity store, final WeixinGroupEntity group,
                                 final List<EmployeeEntity> employees) {
        storeCheck(store);
        Objects.requireNonNull(group);
//        clearGrantedGroup(store, group);
        String sql = getExecSql("insert_group_guides", null);
        getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                EmployeeEntity entity = employees.get(i);
                ps.setString(1, UUID.randomUUID().toString().replaceAll("-", ""));
                ps.setString(2, group.getId());
                ps.setInt(3, entity.getId());
                ps.setInt(4, store.getId());
                ps.setInt(5, store.getCompanyId().get());
                ps.setObject(6, -1);
            }

            @Override
            public int getBatchSize() {
                return employees.size();
            }
        });
    }

    /**
     * 检验门店
     *
     * @param store 门店
     */
    private void storeCheck(StoreEntity store) {
        Objects.requireNonNull(store);
        Preconditions.checkArgument(store.getCompanyId().isPresent(), String.format("门店[%s] 无公司信息", store.getId()));
        Preconditions.checkArgument(store.hasDevice(), String.format("门店[%s] 无设备信息", store.getId()));
    }

    /**
     * 检验门店且将门店信息转换为map
     *
     * @param store
     * @return
     */
    private Map<String, Object> storeCheckAndToMap(StoreEntity store) {
        storeCheck(store);
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("tableName", store.getContactTableName());
        paramMap.put("storeId", store.getId());
        paramMap.put("companyId", store.getCompanyId().get());
        paramMap.put("deviceIds", store.getDeviceIds().get());
        paramMap.put("beforeDays", store.getBeforeDays());
        return paramMap;
    }

    /**
     * 职员校验
     *
     * @param employee
     */
    private void employeeCheck(EmployeeEntity employee) {
        Objects.requireNonNull(employee);
        Preconditions.checkArgument(employee.getStoreId().isPresent(), String.format("职员[%s]无门店信息", employee.getId()));
        Preconditions.checkArgument(employee.getCompanyId().isPresent(),
                String.format("职员[%s]无公司信息", employee.getId()));
    }

    /**
     * 职员检验切将职员信息转换为MAP
     *
     * @param employee
     * @return
     */
    private Map<String, Object> employeeCheckAndToMap(EmployeeEntity employee) {
        employeeCheck(employee);
        Map<String, Object> params = Maps.newHashMap();
        params.put("guideId", employee.getId());
        params.put("storeId", employee.getStoreId().get());
        params.put("companyId", employee.getCompanyId().get());
        return params;
    }

    /**
     * 根据分组ID获取门店的分组
     *
     * @param store
     * @param groupId
     * @return
     */
    public Optional<WeixinGroupEntity> findGroup(StoreEntity store, String groupId) {
        Objects.requireNonNull(store);
        Preconditions.checkArgument(!Strings.isNullOrEmpty("分组ID不能为空"));
        if (WeixinGroupFactory.isAllFriendGroup(groupId))
            return Optional.of(loadAllFriendGroup(store));
        if (WeixinGroupFactory.isNewFriendGroup(groupId))
            return Optional.of(loadNewFriendGroup(store));
        if (WeixinGroupFactory.isLabelGroup(groupId))
            return loadLableGroup(store, groupId);
        Optional<WeixinGroupEntity> userDefinedOpt = loadUserDefinedGroup(store, groupId);
        if (userDefinedOpt.isPresent())
            return userDefinedOpt;
        return loadGuideGroup(store, groupId);
    }

    public Optional<WeixinGroupEntity> findGrantedSignedGroup(StoreEntity store, WebChatUserEntity weixin) {
        Objects.requireNonNull(weixin);
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("weixinId", weixin.getUserName());
        String sql = getExecSql("find_granted_signed_group", params);
        WeixinGroupEntity group = getNamedParameterJdbcTemplate().query(sql, params, getResultSetExtractor());
        if (group == null) return Optional.absent();
        return Optional.of(group);
    }

    /**
     * 加载门店的所有组 包括 1、门店组【普通组、所有好友组、新增好友组】 2、标签组 3、导购组
     *
     * @param store 门店
     * @return 分组列表
     */
    public List<WeixinGroupEntity> loadAllGroups(StoreEntity store) {
        storeCheck(store);
        List<WeixinGroupEntity> groups = Lists.newArrayList();
        groups.addAll(loadStoreGroups(store));
        groups.addAll(loadLabelGroups(store));
        groups.addAll(loadGuideGroups(store));
        return groups;
    }

    /**
     * 加载门店中可授权的分组
     *
     * @param store
     * @return
     */
    public List<WeixinGroupEntity> loadAllGrantableGroups(StoreEntity store) {
        storeCheck(store);
        List<WeixinGroupEntity> grantableGroups = Lists.newArrayList();
        List<WeixinGroupEntity> allGroups = loadAllGroups(store);
        if (allGroups.isEmpty())
            return grantableGroups;
        for (WeixinGroupEntity group : allGroups)
            if (group.isGrantable())
                grantableGroups.add(group);
        return grantableGroups;
    }

    /**
     * 加载门店授权给职员的组,可以进行授权的组有： 所有好友组、新增好友组、普通组【自定义组】、及导购组
     *
     * @param store    门店
     * @param employee 职员
     * @return
     */
    public List<WeixinGroupEntity> loadAllGrantedGroups(final StoreEntity store, final EmployeeEntity employee) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(employee);
        List<WeixinGroupEntity> groups = Lists.newArrayList();
        final CountDownLatch latch = new CountDownLatch(3);
        Future<Optional<WeixinGroupEntity>> allFriendGroupFuture = executor.submit(new Callable<Optional<WeixinGroupEntity>>() {
            @Override
            public Optional<WeixinGroupEntity> call() throws Exception {
                try {
                    return loadGrantedAllFriendGroup(store, employee);
                } finally {
                    latch.countDown();
                }
            }
        });
        Future<WeixinGroupEntity> newFriendGroupFuture = executor.submit(new Callable<WeixinGroupEntity>() {

            @Override
            public WeixinGroupEntity call() throws Exception {
                try {
                    return loadNewFriendGroup(store);
                } finally {
                    latch.countDown();
                }
            }
        });
        Future<List<WeixinGroupEntity>> commonGroupsFuture = executor.submit(new Callable<List<WeixinGroupEntity>>() {

            @Override
            public List<WeixinGroupEntity> call() throws Exception {
                try {
                    return loadGrantedCommonGroups(store, employee);
                } finally {
                    latch.countDown();
                }
            }
        });
        try {
            latch.await(30, TimeUnit.SECONDS);
            Optional<WeixinGroupEntity> allFriendGroupOpt = allFriendGroupFuture.get();
            if (allFriendGroupOpt.isPresent())
                groups.add(allFriendGroupOpt.get());
            WeixinGroupEntity newFriendGroup = newFriendGroupFuture.get();
            groups.add(newFriendGroup);
            groups.addAll(commonGroupsFuture.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return groups;
    }

    /**
     * 加载门店分组,目前门店分组包含有 1、所有好友组 2、新增好友组 3、普通组
     *
     * @param store 门店
     * @return 分组列表
     */
    public List<WeixinGroupEntity> loadStoreGroups(StoreEntity store) {
        storeCheck(store);
        List<WeixinGroupEntity> groups = Lists.newArrayList();
        groups.add(loadAllFriendGroup(store));
        groups.add(loadNewFriendGroup(store));
        groups.addAll(loadCommonGroups(store));
        return groups;
    }

    /**
     * 判断门店是否存在所有好友组的授权
     *
     * @param store
     * @return
     */
    public boolean hasGrantedAllFriendGroup(StoreEntity store) {
        return countAllFriendGroup(storeCheckAndToMap(store)) > 0;
    }

    /**
     * 判断导购是否存在所有好友组的授权
     *
     * @param employee
     * @return
     */
    public boolean hasGrantedAllFriendGroup(StoreEntity store, EmployeeEntity employee) {
        Map<String, Object> paramMap = employeeCheckAndToMap(employee);
        paramMap.putAll(storeCheckAndToMap(store));
        return countAllFriendGroup(paramMap) == 1;
    }

    private Integer countAllFriendGroup(Map<String, Object> paramMap) {
        String execSql = getExecSql("count_granted_all_friend_group", paramMap);
        return getNamedParameterJdbcTemplate().queryForObject(execSql, paramMap, Integer.class);
    }

    /**
     * 加载门店所有好友组
     *
     * @param store 门店
     * @return
     */
    public WeixinGroupEntity loadAllFriendGroup(StoreEntity store) {
        Map<String, Object> params = storeCheckAndToMap(store);
        String sql = getExecSql("load_all_friend_group", params);
        WeixinGroupEntity group = getNamedParameterJdbcTemplate().query(sql, params, getResultSetExtractor());
        return group;
    }

    /**
     * 加载门店 分配给职员的所有好友组，该组不一定存在
     *
     * @param store    门店
     * @param employee 职员
     * @return
     */
    public Optional<WeixinGroupEntity> loadGrantedAllFriendGroup(StoreEntity store, EmployeeEntity employee) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(employee);
        if (hasGrantedAllFriendGroup(store, employee))
            return Optional.of(loadAllFriendGroup(store));
        return Optional.absent();
    }

    /**
     * 将某个门店的所有好友组分配给职员
     *
     * @param store    门店
     * @param employee 职员
     */
    public void grantAllFriendGroup(StoreEntity store, EmployeeEntity employee) {
        // TODO
    }

    /**
     * 加载当前门店的新增好友组
     *
     * @param store 门店
     * @return
     */
    public WeixinGroupEntity loadNewFriendGroup(StoreEntity store) {
        Map<String, Object> params = storeCheckAndToMap(store);
        String sql = getExecSql("load_new_friend_group", params);
        WeixinGroupEntity group = getNamedParameterJdbcTemplate().query(sql, params, getResultSetExtractor());
        return group;
    }

    /**
     * 根据分组ID查找普通组【自定义】分组
     *
     * @param id 分组ID
     * @return 分组
     */
    public Optional<WeixinGroupEntity> loadUserDefinedGroup(StoreEntity store, String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty("分组ID不能为空"));
        Map<String, Object> paramMap = storeCheckAndToMap(store);
        paramMap.put("id", id);
        paramMap.put("type", WeixinGroupEntity.Type.USER_DEFINED_GROUP.getType());
        String sql = getExecSql("load_common_group", paramMap);
        WeixinGroupEntity group = getNamedParameterJdbcTemplate().query(sql, paramMap, getResultSetExtractor());
        if (group == null)
            return Optional.absent();
        return Optional.of(group);
    }

    public boolean existInCommonGroup(StoreEntity store, WeixinGroupEntity group, WebChatUserEntity weixin) {
        Objects.requireNonNull(weixin);
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("friendId", weixin.getUserName());
        params.put("groupId", group.getId());
        String sql = getExecSql("count_common_group_friend", params);
        Map<String, Object> resultMap = getNamedParameterJdbcTemplate().queryForMap(sql, params);
        Integer count = MapUtils.getInteger(resultMap, "count");
        if (count >= 1)
            return true;
        return false;
    }

    /**
     * 加载门店所有自定义组
     *
     * @param store 门店
     * @return
     */
    public List<WeixinGroupEntity> loadCommonGroups(StoreEntity store) {
        Map<String, Object> params = storeCheckAndToMap(store);
//		params.put("type", WeixinGroupEntity.Type.USER_DEFINED_GROUP.getType());
        String sql = getExecSql("load_common_groups", params);
        return getNamedParameterJdbcTemplate().query(sql, params, getGroupsExtractor());
    }

    /**
     * 获取 门店下分配给职员的自定义分组
     *
     * @param store    门店
     * @param employee 职员
     * @return
     */
    public List<WeixinGroupEntity> loadGrantedCommonGroups(StoreEntity store, EmployeeEntity employee) {
        Map<String, Object> paramMap = employeeCheckAndToMap(employee);
        paramMap.putAll(storeCheckAndToMap(store));
        String sql = getExecSql("load_granted_common_groups_4guide", paramMap);
        return getNamedParameterJdbcTemplate().query(sql, paramMap, getGroupsExtractor());
    }

    /**
     * 根据分组ID查找门店的标签分组
     *
     * @param store   门店
     * @param groupId 分组ID
     * @return
     */
    public Optional<WeixinGroupEntity> loadLableGroup(StoreEntity store, String groupId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty("分组ID不能为空"));
        Map<String, Object> paramMap = storeCheckAndToMap(store);
        paramMap.put("deviceId", WeixinGroupFactory.getDeviceId(groupId));
        paramMap.put("labelId", WeixinGroupFactory.getLabelId(groupId));
        String sql = getExecSql("load_label_group", paramMap);
        WeixinGroupEntity group = getNamedParameterJdbcTemplate().query(sql, paramMap, getResultSetExtractor());
        if (group == null)
            return Optional.absent();
        return Optional.of(group);
    }

    /**
     * 加载门店所有的标签组
     *
     * @param store 门店
     * @return 分组列表
     */
    public List<WeixinGroupEntity> loadLabelGroups(StoreEntity store) {
        Map<String, Object> paramMap = storeCheckAndToMap(store);
        String sql = getExecSql("load_label_groups", paramMap);
        return getNamedParameterJdbcTemplate().query(sql, paramMap, getGroupsExtractor());
    }

    /**
     * 根据分组ID查找导购分组
     *
     * @param id 分组ID
     * @return 分组
     */
    public Optional<WeixinGroupEntity> loadGuideGroup(StoreEntity store, String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty("分组ID不能为空"));
        Map<String, Object> paramMap = storeCheckAndToMap(store);
        paramMap.put("id", id);
        paramMap.put("type", WeixinGroupEntity.Type.EMPLOYEE_GROUP.getType());
        String sql = getExecSql("load_common_group", paramMap);
        WeixinGroupEntity group = getNamedParameterJdbcTemplate().query(sql, paramMap, getResultSetExtractor());
        if (group == null)
            return Optional.absent();
        return Optional.of(group);
    }

    /**
     * 加载门店所有的导购组
     *
     * @param store
     * @return
     */
    public List<WeixinGroupEntity> loadGuideGroups(StoreEntity store) {
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("type", WeixinGroupEntity.Type.EMPLOYEE_GROUP.getType());
        String sql = getExecSql("load_common_groups", params);
        return getNamedParameterJdbcTemplate().query(sql, params, getGroupsExtractor());
    }

    /**
     * 获取授权给导购的导购组
     *
     * @param store
     * @param employee
     * @return
     */
    public Optional<WeixinGroupEntity> loadGrantedGuideGroup(StoreEntity store, EmployeeEntity employee) {
        Map<String, Object> params = storeCheckAndToMap(store);
        params.putAll(employeeCheckAndToMap(employee));
        String execSql = getExecSql("load_granted_guide_groups", params);
        WeixinGroupEntity group = getNamedParameterJdbcTemplate().query(execSql, params, getResultSetExtractor());
        if (group == null)
            return Optional.absent();
        return Optional.of(group);
    }

    /**
     * 获取 门店下分配给职员的自定义分组
     *
     * @param store    门店
     * @param employee 职员
     * @return
     */
    public List<WeixinGroupEntity> loadGrantedGuideGroups(StoreEntity store, EmployeeEntity employee) {
        Map<String, Object> paramMap = employeeCheckAndToMap(employee);
        paramMap.putAll(storeCheckAndToMap(store));
        paramMap.put("type", WeixinGroupEntity.Type.EMPLOYEE_GROUP.getType());
        String sql = getExecSql("load_granted_common_groups_4guide", paramMap);
        return getNamedParameterJdbcTemplate().query(sql, paramMap, getGroupsExtractor());
    }

    /**
     * 如果导购的分组不存在则创建并授权导购组
     *
     * @param store
     * @param employee
     */
    public WeixinGroupEntity addAndGrantGuideIfAbsent(StoreEntity store, EmployeeEntity employee) {
        storeCheck(store);
        employeeCheck(employee);
        Optional<WeixinGroupEntity> guideGroupOpt = loadGrantedGuideGroup(store, employee);
        if (guideGroupOpt.isPresent())
            return guideGroupOpt.get();
        Map<Integer, WeixinGroupEntity> guideMap = Maps.newHashMap();
        WeixinGroupEntity guideGroup = WeixinGroupEntity.createEmployeeGroup(store, employee.getUserName() + "组");
        guideMap.put(employee.getId(), guideGroup);
        addGuideGroup(guideGroup);
        grantGuideGroups(store, guideMap);
        return guideGroup;
    }

    /**
     * 为某个门店批量新增导购组
     *
     * @param store     门店
     * @param employees 导购列表
     */
    public void addAndGrantGuideGroups(StoreEntity store, List<EmployeeEntity> employees) {
        Objects.requireNonNull(store);
        if (CollectionUtils.isEmpty(employees))
            return;
        Map<Integer, WeixinGroupEntity> grantedGuideMap = findGrantedGuideGroupsMap(store);
        Map<Integer, WeixinGroupEntity> unGrantedGuideMap = Maps.newHashMap();
        for (EmployeeEntity employee : employees) {
            if (!grantedGuideMap.containsKey(employee.getId()) && employee.isEnabled())
                unGrantedGuideMap.put(employee.getId(), WeixinGroupEntity.createEmployeeGroup(store, (employee.getUserName() + "组")));
        }
        addGuideGroups(unGrantedGuideMap.values());
        grantGuideGroups(store, unGrantedGuideMap);
    }

    private void grantGuideGroups(final StoreEntity store, final Map<Integer, WeixinGroupEntity> guideMap) {
        String sql = getExecSql("insert_group_guides", null);
        getJdbcTemplate().batchUpdate(sql, guideMap.entrySet(), 1000,
                new ParameterizedPreparedStatementSetter<Entry<Integer, WeixinGroupEntity>>() {
                    @Override
                    public void setValues(PreparedStatement ps, Entry<Integer, WeixinGroupEntity> entry)
                            throws SQLException {
                        // id,batch_id,serial_num,life_status,createUserId
                        ps.setString(1, UUID.randomUUID().toString().replaceAll("-", ""));
                        ps.setString(2, entry.getValue().getId());
                        ps.setInt(3, entry.getKey());
                        ps.setInt(4, store.getId());
                        ps.setInt(5, store.getCompanyId().get());
                        ps.setObject(6, -1);
                    }
                });
    }

    /**
     * 批量新增导购组
     *
     * @param store
     * @param employees
     */
    @SuppressWarnings("unchecked")
    public void addGuideGroups(Collection<WeixinGroupEntity> groups) {
        List<Map<String, Object>> groupMaps = Lists.newArrayList();
        for (WeixinGroupEntity group : groups)
            groupMaps.add(group.toMap());
        Map<String, Objects>[] maps = new Map[groupMaps.size()];
        groupMaps.toArray(maps);
        getNamedParameterJdbcTemplate().batchUpdate(getExecSql("insert_common_group", null), maps);
    }

    public void addGuideGroup(WeixinGroupEntity group) {
        getNamedParameterJdbcTemplate().update(getExecSql("insert_common_group", null), group.toMap());
    }
    /**
     * 获取门店已经授权的导购组，并放入map
     *
     * @param store 门店
     * @return
     */
    private Map<Integer, WeixinGroupEntity> findGrantedGuideGroupsMap(StoreEntity store) {
        Map<String, Object> params = storeCheckAndToMap(store);
        String execSql = getExecSql("load_granted_guide_groups", params);
        return getNamedParameterJdbcTemplate().query(execSql, params,
                new ResultSetExtractor<Map<Integer, WeixinGroupEntity>>() {
                    @Override
                    public Map<Integer, WeixinGroupEntity> extractData(ResultSet rs)
                            throws SQLException, DataAccessException {
                        Map<Integer, WeixinGroupEntity> guideMap = Maps.newHashMap();
                        while (rs.next())
                            guideMap.put(rs.getInt("guideId"), WeixinGroupEntity.valueOf(rs));
                        return guideMap;
                    }
                });
    }

    /**
     * 保存或更新所有好友授权日志
     *
     * @param store 门店
     */
    public void saveOrUpdateGrantedLogForAllFriendGroup(StoreEntity store) {
        Objects.requireNonNull(store);
        Optional<AllFriendGroupLogEntity> groupLogOpt = findGrantedLogForAllFriendGroup();
        if (!groupLogOpt.isPresent()) {
            saveGrantedLogForAllFriendGroup(store);
            return;
        }
        updateGrantedLogForAllFriendGroup(groupLogOpt.get(), store);
    }

    /**
     * 保存门店所有好友组授权日志
     *
     * @param store
     * @return
     */
    public AllFriendGroupLogEntity saveGrantedLogForAllFriendGroup(StoreEntity store) {
        Objects.requireNonNull(store);
        WeixinGroupEntity allFriendGroup = loadAllFriendGroup(store);
        AllFriendGroupLogEntity groupLog = new AllFriendGroupLogEntity(allFriendGroup);
        groupLog.addGrantStore(store);
        String sql = getExecSql("save_allfriend_group_log", null);
        getNamedParameterJdbcTemplate().update(sql, groupLog.toMap());
        return groupLog;
    }

    /**
     * 更新门店所有好友组授权日志
     *
     * @param group
     * @param store
     */
    public void updateGrantedLogForAllFriendGroup(AllFriendGroupLogEntity group, StoreEntity store) {
        Objects.requireNonNull(store);
        group.addGrantStore(store);
        String sql = getExecSql("update_allfriend_group_log", null);
        getNamedParameterJdbcTemplate().update(sql, group.toMap());
    }

    /**
     * 查找所有好友组授权日志
     *
     * @return
     */
    public Optional<AllFriendGroupLogEntity> findGrantedLogForAllFriendGroup() {
        String sql = getExecSql("find_allfriend_group_log", null);
        AllFriendGroupLogEntity group = getNamedParameterJdbcTemplate().query(sql, getAllFriendGroupLogExtractor());
        if (group == null)
            return Optional.absent();
        return Optional.of(group);
    }

    /**
     * 判断时候有所有好友分组的记录
     *
     * @param store
     * @return
     */
    public boolean hasLogAllFriendGroup(StoreEntity store) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        String sql = getExecSql("count_allfriend_group_log", params);
        Integer count = getNamedParameterJdbcTemplate().queryForObject(sql, params, Integer.class);
        return count > 0;
    }

    public void clearGrantedAllFriendGroup(StoreEntity store) {
        Map<String, Object> params = storeCheckAndToMap(store);
        String sql = getExecSql("delete_all_friend_guide_groups", params);
        getNamedParameterJdbcTemplate().update(sql, params);
    }

    /**
     * --------------------------------------查询相关功能begin---------------------------------
     */
    private void fillSearchCondition(Map<String, Object> params, Map<String, Object> searchs) {
        if (searchs != null && searchs.containsKey("search")) {
            String search = MapUtils.getString(searchs, "search");
            if (!Strings.isNullOrEmpty(search))
                params.put("search", String.format("%%%s%%", search));
        }
        if (searchs != null && searchs.containsKey("labelIds")) {
            String labelIdsStr = MapUtils.getString(searchs, "labelIds");
            if (Strings.isNullOrEmpty(labelIdsStr.trim()))
                return;
            String[] labelIds = labelIdsStr.split(",");

            if (labelIds.length != 0)
                params.put("userLabels", Lists.newArrayList(labelIds));
        }
    }

    /**
     * 搜索某个门店下的某个分组的好友列表
     *
     * @param store   门店
     * @param group   分组
     * @param searchs 关键字
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<MemberDTO> searchFriend(StoreEntity store, String groupId, Map<String, Object> searchs) {
        Objects.requireNonNull(store);
        Preconditions.checkArgument(store.hasDevice(), String.format("门店[%s]无设备信息", store.getId()));
        if (WeixinGroupFactory.isAllFriendGroup(groupId))
            return searchAllFriendGroupFriends(store, searchs);
        if (WeixinGroupFactory.isCommonGroup(groupId))
            return searchCommonGroupFriends(store, Lists.newArrayList(groupId), searchs);
        if (WeixinGroupFactory.isNewFriendGroup(groupId))
            return searchNewFriendGroupFriends(store, searchs);
        if (WeixinGroupFactory.isLabelGroup(groupId))
            return searchLabelGroupFriends(store, groupId, searchs);
        return Collections.EMPTY_LIST;
    }

    /**
     * 搜索某个门店下的某个分组的好友列表
     *
     * @param store   门店
     * @param group   分组
     * @param searchs 关键字
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<MemberDTO> searchFriend(StoreEntity store, WeixinGroupEntity group, Map<String, Object> searchs) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(group);
        Preconditions.checkArgument(store.hasDevice(), String.format("门店[%s]无设备信息", store.getId()));
        if (group.isAllFriendGroup())
            return searchAllFriendGroupFriends(store, searchs);
        if (group.isCommonGroup())
            return searchCommonGroupFriends(store, group, searchs);
        if (group.isNewFriendGroup())
            return searchNewFriendGroupFriends(store, searchs);
        if (group.isLabelGroup())
            return searchLabelGroupFriends(store, group, searchs);
        return Collections.EMPTY_LIST;
    }

    /**
     * 查询当前门店所有授权组及授权对象
     *
     * @param store
     * @return
     */
    public List<GroupEmployeesDTO> loadGrantedGroupWithEmployees(StoreEntity store) {
        Map<String, Object> paramMap = storeCheckAndToMap(store);
        List<GroupEmployeesDTO> groupEmployeesDtos = Lists.newArrayList();
        String sql = getExecSql("query_group_guides", paramMap);
        List<Map<String, Object>> resMaps = getNamedParameterJdbcTemplate().queryForList(sql, paramMap);
        if (CollectionUtils.isEmpty(resMaps))
            return groupEmployeesDtos;
        TreeSet<String> groupIds = Sets.newTreeSet();
        for (Map<String, Object> resMap : resMaps)
            groupIds.add(MapUtils.getString(resMap, "groupId"));
        for (String groupId : groupIds) {
            List<String> guideIds = Lists.newArrayList();
            String groupName = null;
            for (Map<String, Object> resMap : resMaps) {
                String gId = MapUtils.getString(resMap, "groupId");
                if (gId.equals(groupId)) {
                    if (groupName == null)
                        groupName = MapUtils.getString(resMap, "groupName");
                    guideIds.add(MapUtils.getString(resMap, "guideId"));
                }
            }
            GroupEmployeesDTO dto = new GroupEmployeesDTO(groupId, groupName, guideIds);
            groupEmployeesDtos.add(dto);
        }
        return groupEmployeesDtos;
    }

    /**
     * 获取某个门店带有好友信息的所有好友组
     *
     * @param store 门店
     * @return
     */
    public GroupMemberDTO loadAllFriendGroupWithFriends(StoreEntity store) {
        storeCheck(store);
        WeixinGroupEntity allFriendGroup = loadAllFriendGroup(store);
        GroupMemberDTO dto = new GroupMemberDTO(allFriendGroup.getId(), allFriendGroup.getName());
        List<MemberDTO> friendsDto = listAllFriendGroupFriends(store);
        dto.setMembers(friendsDto);
        return dto;
    }

    /**
     * 获取某个门店某个导购下的带有好友信息的所有好友组
     *
     * @param store
     * @param employee
     * @return
     */
    public Optional<GroupMemberDTO> loadGrantedAllFriendGroupWithFriends(StoreEntity store, EmployeeEntity employee) {
        storeCheck(store);
        Objects.requireNonNull(employee);
        if (!hasGrantedAllFriendGroup(store, employee))
            return Optional.absent();
        return Optional.of(loadAllFriendGroupWithFriends(store));
    }

    /**
     * 获取某门店下所有好友分组好友列表
     *
     * @param store
     * @return
     */
    public List<MemberDTO> listAllFriendGroupFriends(StoreEntity store) {
        Objects.requireNonNull(store);
        return searchAllFriendGroupFriends(store, null);
    }

    /**
     * 按条件查询授权给导购的所有好友分组及组员
     *
     * @param store    门店
     * @param employee 导购
     * @param searchs  查询条件
     * @return
     */
    public Optional<GroupMemberDTO> searchGrantedAllFriendGroupWithFriends(StoreEntity store, EmployeeEntity employee,
                                                                           Map<String, Object> searchs) {
        storeCheck(store);
        Objects.requireNonNull(employee);
        if (!hasGrantedAllFriendGroup(store, employee))
            return Optional.absent();
        List<MemberDTO> members = searchAllFriendGroupFriends(store, searchs);
//        if (members.isEmpty())
//            return Optional.absent();
        WeixinGroupEntity allFriendGroup = WeixinGroupFactory.getAllFriendGroup(store);
        GroupMemberDTO dto = new GroupMemberDTO(allFriendGroup.getId(), allFriendGroup.getName());
        dto.setMembers(members);
        return Optional.of(dto);
    }

    /**
     * 根据搜索条件搜索某门店下的所有好友组相关好友
     *
     * @param store   门店
     * @param searchs 搜索条件
     * @return
     */
    private List<MemberDTO> searchAllFriendGroupFriends(StoreEntity store, Map<String, Object> searchs) {
        Map<String, Object> params = storeCheckAndToMap(store);
        fillSearchCondition(params, searchs);
        return getNamedParameterJdbcTemplate().query(getExecSql("search_all_friend_group", params), params,
                getMemberDTOListExtractor());
    }

    public boolean existInNewFriendGroup(StoreEntity store, WebChatUserEntity weixin) {
        Objects.requireNonNull(weixin);
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("friendId", weixin.getUserName());
        String sql = getExecSql("count_new_friend_group_friend", params);
        Map<String, Object> resultMap = getNamedParameterJdbcTemplate().queryForMap(sql, params);
        Integer count = MapUtils.getInteger(resultMap, "count");
        if (count >= 1)
            return true;
        return false;
    }


    /**
     * 查询门店的带好友的新增好友组
     *
     * @param store 门店
     * @return
     */
    public GroupMemberDTO loadNewFriendGroupWithFriends(StoreEntity store) {
        storeCheck(store);
        WeixinGroupEntity addNewFriendGroup = loadNewFriendGroup(store);
        GroupMemberDTO dto = new GroupMemberDTO(addNewFriendGroup.getId(), addNewFriendGroup.getName());
        List<MemberDTO> friendsDto = listNewFriendGroupFriends(store);
        dto.setMembers(friendsDto);
        return dto;
    }

    /**
     * 查询新增好友组好友列表
     *
     * @param store 门店
     * @return
     */
    public List<MemberDTO> listNewFriendGroupFriends(StoreEntity store) {
        return searchNewFriendGroupFriends(store, null);
    }

    /**
     * 按条件查询授权给导购的带有好友列表的新增好友组
     *
     * @param employee 导购
     * @param store    门店
     * @param searchs  查询条件
     * @return
     */
    public Optional<GroupMemberDTO> searchNewFriendGroupWithFriends(StoreEntity store,
                                                                    Map<String, Object> searchs) {
        storeCheck(store);
        WeixinGroupEntity addNewFriendGroup = WeixinGroupFactory.getAddNewFriendGroup(store);
        GroupMemberDTO dto = new GroupMemberDTO(addNewFriendGroup.getId(), addNewFriendGroup.getName());
        List<MemberDTO> friendsDto = searchNewFriendGroupFriends(store, searchs);
//        if (friendsDto.isEmpty())
//            return Optional.absent();
        dto.setMembers(friendsDto);
        return Optional.of(dto);
    }

    /**
     * 根据搜索条件搜索门店下新增好友组相关好友
     *
     * @param store   门店
     * @param searchs 搜索条件
     * @return
     */
    private List<MemberDTO> searchNewFriendGroupFriends(StoreEntity store, Map<String, Object> searchs) {
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("beforeDays", store.getBeforeDays());
        fillSearchCondition(params, searchs);
        return getNamedParameterJdbcTemplate().query(getExecSql("search_new_friend_group", params), params,
                getMemberDTOListExtractor());
    }

    /**
     * 获取某门店下的带有微信好友信息的普通组【自定义分组】列表
     *
     * @param store
     * @return
     */
    public List<GroupMemberDTO> loadCommonGroupsWithFriends(StoreEntity store) {
        storeCheck(store);
        return loadCommonGroupsWithFriends(store, null);
    }

    /**
     * 获取某门店某导购下的带有微信好友信息的普通组【自定义分组】列表
     *
     * @param store
     * @param employee
     * @return
     */
    public List<GroupMemberDTO> loadCommonGroupsWithFriends(StoreEntity store, EmployeeEntity employee) {
        return searchCommonGroupsWithFriends(store, employee, null);
    }

    public List<GroupMemberDTO> searchGrantedCommonGroupsWithFriends(StoreEntity store, EmployeeEntity employee,
                                                                     Map<String, Object> searchs) {
        return searchCommonGroupsWithFriends(store, employee, searchs);
    }

    public List<GroupMemberDTO> searchCommonGroupsWithFriends(StoreEntity store, EmployeeEntity employee,
                                                              Map<String, Object> searchs) {
        Map<String, Object> params = storeCheckAndToMap(store);
        if (employee != null)
            params.put("guideId", employee.getId());
        List<WeixinGroupEntity> commonGroups = loadGrantedCommonGroups(store, employee);
        fillSearchCondition(params, searchs);
        List<Map<String, Object>> resMaps = getNamedParameterJdbcTemplate()
                .queryForList(getExecSql("search_common_groups_with_members", params), params);
        return parseToGroupMemberDTO(commonGroups, resMaps);
    }

    private List<GroupMemberDTO> parseToGroupMemberDTO(List<WeixinGroupEntity> commonGroups, List<Map<String, Object>> resMaps) {
        List<GroupMemberDTO> groupMembers = Lists.newArrayList();
        if (resMaps.isEmpty())
            return groupMembers;
        List<String> groupIds = Lists.newArrayList();
        for (Map<String, Object> resMap : resMaps) {
            String groupId = MapUtils.getString(resMap, "groupId");
            if (!groupIds.contains(groupId))
                groupIds.add(groupId);
        }
        for (WeixinGroupEntity group : commonGroups) {
            List<MemberDTO> members = Lists.newArrayList();
            for (Map<String, Object> resMap : resMaps) {
                String gId = MapUtils.getString(resMap, "groupId");
                if (gId.equals(group.getId())) {
                    String userId = MapUtils.getString(resMap, "userName");
                    if (Strings.isNullOrEmpty(userId))
                        continue;
                    String userName = MapUtils.getString(resMap, "userName");
                    String iconUrl = MapUtils.getString(resMap, "iconUrl");
                    String nickName = MapUtils.getString(resMap, "nickName");
                    String conRemark = MapUtils.getString(resMap, "conRemark");
                    String memberId = MapUtils.getString(resMap, "memberId");
                    String memberName = MapUtils.getString(resMap, "memberName");
                    Integer type = MapUtils.getInteger(resMap, "wxType");
                    Integer signed = MapUtils.getInteger(resMap, "signed");
                    MemberDTO member = new MemberDTO(userId, iconUrl, userName, nickName, conRemark, type, signed, memberId,
                            memberName);
                    members.add(member);
                }
            }
            GroupMemberDTO dto = new GroupMemberDTO(group.getId(), group.getName(), members.size(), members);
            groupMembers.add(dto);
        }
        return groupMembers;
    }

    /**
     * 获取某门店下的某个自定义分组的好友列表
     *
     * @param store
     * @param group
     * @return
     */
    public List<MemberDTO> listCommonGroupFriends(StoreEntity store, WeixinGroupEntity group) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(group);
        return searchCommonGroupFriends(store, group, null);
    }

    public List<MemberDTO> listCommonGroupFriends(StoreEntity store, List<String> groupIds) {
        return searchCommonGroupFriends(store, groupIds, null);
    }

    public List<MemberDTO> searchCommonGroupFriends(StoreEntity store, List<String> groupIds,
                                                    Map<String, Object> searchs) {
        Map<String, Object> params = storeCheckAndToMap(store);
        if (groupIds.size() == 1) {
            params.put("groupId", groupIds.get(0));
        } else {
            params.put("groupIds", groupIds);
        }
        fillSearchCondition(params, searchs);
        return getNamedParameterJdbcTemplate().query(getExecSql("search_common_group", params), params,
                getMemberDTOListExtractor());
    }

    /**
     * 根据搜索条件搜索某门店下某自定义分组相关的好友
     *
     * @param store   门店
     * @param group   自定义分组
     * @param searchs 搜索条件
     * @return
     */
    private List<MemberDTO> searchCommonGroupFriends(StoreEntity store, WeixinGroupEntity group,
                                                     Map<String, Object> searchs) {
        return searchCommonGroupFriends(store, Lists.newArrayList(group.getId()), searchs);
    }

    public List<GroupMemberDTO> loadLabelGroupsWithFriends(StoreEntity store) {
        storeCheck(store);
        return loadLabelGroupsWithFriends(store, null);
    }

    public List<GroupMemberDTO> loadLabelGroupsWithFriends(StoreEntity store, EmployeeEntity employee) {
        Map<String, Object> params = storeCheckAndToMap(store);
        if (employee != null)
            params.put("guideId", employee.getId());
        List<Map<String, Object>> resMaps = getNamedParameterJdbcTemplate()
                .queryForList(getExecSql("list_label_groups", params), params);
        List<WeixinGroupEntity> labelGroups = loadLabelGroups(store);
        return parseToGroupMemberDTO(labelGroups, resMaps);
    }

    public Set<MemberDTO> listLabelGroupFriends(StoreEntity store, List<String> groupIds) {
        Set<MemberDTO> memberDtos = Sets.newHashSet();
        if (CollectionUtils.isEmpty(groupIds))
            return memberDtos;
        Multimap<String, String> multiMap = ArrayListMultimap.create();
        for (String groupId : groupIds) {
            String deviceId = WeixinGroupFactory.getDeviceId(groupId);
            String labelId = WeixinGroupFactory.getLabelId(groupId);
            multiMap.put(deviceId, labelId);
        }
        Map<String, Collection<String>> deviceMap = multiMap.asMap();
        for (String key : deviceMap.keySet()) {
            memberDtos.addAll(listLabelGroupFriends(store, key, deviceMap.get(key)));
        }
        return memberDtos;
    }

    private List<MemberDTO> listLabelGroupFriends(StoreEntity store, String deviceId, Collection<String> labelIds) {
        Map<String, Object> paramMap = storeCheckAndToMap(store);
        paramMap.put("deviceId", deviceId);
        paramMap.put("labelIds", labelIds);
        String execSql = getExecSql("search_label_groups", paramMap);
        List<MemberDTO> members = getNamedParameterJdbcTemplate().query(execSql, paramMap, getMemberDTOListExtractor());
        return members;
    }

    public List<MemberDTO> listLabelGroupFriends(StoreEntity store, WeixinGroupEntity group) {
        Objects.requireNonNull(store);
        return searchLabelGroupFriends(store, group, null);
    }

    public List<MemberDTO> searchLabelGroupFriends(StoreEntity store, WeixinGroupEntity group,
                                                   Map<String, Object> searchs) {
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("deviceId", WeixinGroupFactory.getDeviceId(group.getId()));
        params.put("labelId", WeixinGroupFactory.getLabelId(group.getId()));
        fillSearchCondition(params, searchs);
        return getNamedParameterJdbcTemplate().query(getExecSql("search_label_group", params), params,
                getMemberDTOListExtractor());
    }

    public List<MemberDTO> searchLabelGroupFriends(StoreEntity store, String groupId,
                                                   Map<String, Object> searchs) {
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("deviceId", WeixinGroupFactory.getDeviceId(groupId));
        params.put("labelId", WeixinGroupFactory.getLabelId(groupId));
        fillSearchCondition(params, searchs);
        return getNamedParameterJdbcTemplate().query(getExecSql("search_label_group", params), params,
                getMemberDTOListExtractor());
    }


    public List<WeixinGroupEntity> loadGroupsForStore(StoreEntity store) {
        Map<String, Object> params = storeCheckAndToMap(store);
        String execSql = getExecSql("load_store_groups", params);
        return getNamedParameterJdbcTemplate().query(execSql, params, getGroupsExtractor());
    }

    /**
     * --------------------------------------查询相关功能end---------------------------------
     */
    private ResultSetExtractor<List<WeixinGroupEntity>> getGroupsExtractor() {
        return new ResultSetExtractor<List<WeixinGroupEntity>>() {

            @Override
            public List<WeixinGroupEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<WeixinGroupEntity> groupList = Lists.newArrayList();
                while (rs.next())
                    groupList.add(WeixinGroupEntity.valueOf(rs));
                return groupList;
            }
        };
    }

    @Override
    protected ResultSetExtractor<WeixinGroupEntity> getResultSetExtractor() {
        return new ResultSetExtractor<WeixinGroupEntity>() {
            @Override
            public WeixinGroupEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next())
                    return WeixinGroupEntity.valueOf(rs);
                return null;
            }
        };
    }

    private ResultSetExtractor<AllFriendGroupLogEntity> getAllFriendGroupLogExtractor() {
        return new ResultSetExtractor<AllFriendGroupLogEntity>() {
            @Override
            public AllFriendGroupLogEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next())
                    return AllFriendGroupLogEntity.valueOf(rs);
                return null;
            }

        };
    }

    private ResultSetExtractor<List<MemberDTO>> getMemberDTOListExtractor() {
        return new ResultSetExtractor<List<MemberDTO>>() {

            @Override
            public List<MemberDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<MemberDTO> members = Lists.newArrayList();
                while (rs.next()) {
                    String userId = rs.getString("userName");
                    if (Strings.isNullOrEmpty(userId))
                        continue;
                    members.add(MemberDTO.valueOf(rs));
                }
                return members;
            }
        };
    }
}
