package com.csosm.module.webchat;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.event.EventBusSubscribe;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.EmployeeServer;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.webchat.entity.WebChatUserAction;
import com.csosm.module.webchat.entity.WebChatUserEntity;
import com.csosm.module.webchat.group.*;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.*;
import java.util.concurrent.*;

import static java.util.Collections.EMPTY_LIST;

public class WeixinGroupService extends AbstractBaseServer implements EventBusSubscribe {

    private static final Logger logger = LoggerFactory.getLogger(WeixinGroupService.class);

    private ThreadPoolTaskExecutor executor;

    public void setExecutor(ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    /**
     * 修改分组名称
     *
     * @param groupId 分组ID
     * @param name    分组名称
     * @param store   门店
     */
    public void modifyGroupName(String groupId, String name, StoreEntity store) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupId), "入参分组ID不能为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参分组名称不能为空");
        Objects.requireNonNull(store);
        getBean(WeixinGroupAction.class).modifyGroupName(getExistCommonGroup(store, groupId), name);
    }

    /**
     * 移除好友分组
     *
     * @param groupId 分组ID
     * @param store   门店
     */
    public void removeGroup(String groupId, StoreEntity store) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupId), "入参分组ID不能为空");
        Objects.requireNonNull(store);
        getBean(WeixinGroupAction.class).removeGroup(store, getExistCommonGroup(store, groupId));
    }

    /**
     * 添加组好友
     *
     * @param groupId   分组ID
     * @param weixinIds 微信好友ID
     * @param store     门店
     */
    public void addFriends(String groupId, List<String> weixinIds, StoreEntity store) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupId), "入参分组ID不能为空");
        Objects.requireNonNull(store);
        if (CollectionUtils.isEmpty(weixinIds))
            return;
        WeixinGroupEntity group = getExistCommonGroup(store, groupId);
        Preconditions.checkState(group.isEditable(), String.format("该分组[%s]不允许添加好友", groupId));
        Optional<List<WebChatUserEntity>> weixinsOpt = getBean(WebChatUserAction.class).loadAllByIds(store, weixinIds);
        if (!weixinsOpt.isPresent())
            return;
        if (weixinsOpt.get().isEmpty())
            return;
        List<WebChatUserEntity> weixins = weixinsOpt.get();
        getBean(WeixinGroupAction.class).addFriends(group, weixins);
    }

    /**
     * 移除组微信好友
     *
     * @param groupId   分组ID
     * @param weixinIds 待移除微信好友ID
     * @param store     门店
     */
    public void removeFriends(String groupId, List<String> weixinIds, StoreEntity store) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupId), "入参分组ID不能为空");
        Objects.requireNonNull(store);
        if (CollectionUtils.isEmpty(weixinIds))
            return;
        WeixinGroupEntity group = getExistCommonGroup(store, groupId);
        Preconditions.checkState(group.isEditable(), String.format("该分组[%s]不允许移除好友", groupId));
        Optional<List<WebChatUserEntity>> weixinsOpt = getBean(WebChatUserAction.class).loadAllByIds(store, weixinIds);
        if (!weixinsOpt.isPresent())
            return;
        if (weixinsOpt.get().isEmpty())
            return;
        List<WebChatUserEntity> weixins = weixinsOpt.get();
        getBean(WeixinGroupAction.class).removeFriends(group, weixins);
    }

    /**
     * 获取可授权的导购
     *
     * @param loginUser
     * @param store
     * @param employeeIds
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<EmployeeEntity> getGrantableEmployees(LoginUserContext loginUser, StoreEntity store,
                                                       List<String> employeeIds) {
        Optional<List<EmployeeEntity>> employeesOpt = getBean(EmployeeServer.class)
                .loadEnabledShoppingGuides(store.getId(), loginUser);
        if (!employeesOpt.isPresent()) {
            return EMPTY_LIST;
        }
        List<EmployeeEntity> grantEmployees = Lists.newArrayList();
        for (EmployeeEntity emp : employeesOpt.get()) {
            if (employeeIds.contains(emp.getId().toString()))
                grantEmployees.add(emp);
        }
        return grantEmployees;
    }

    /**
     * 将分组分配给多个导购
     *
     * @param loginUser   当前登录用户
     * @param store       门店
     * @param groupId     分组Id
     * @param employeeIds 导购ID
     */
    public void grantGroupToEmployees(LoginUserContext loginUser, StoreEntity store, String groupId,
                                      List<String> employeeIds) {
        Objects.requireNonNull(store);
        Optional<WeixinGroupEntity> groupOpt = getBean(WeixinGroupAction.class).findGroup(store, groupId);
        Preconditions.checkState(groupOpt.isPresent(), String.format("待授权分组[%s]不存在", groupId));
        Preconditions.checkState(groupOpt.get().isGrantable(), String.format("该分组[%s]不允许授权", groupId));
        getBean(WeixinGroupAction.class).grantToEmployees(store, groupOpt.get(),
                getGrantableEmployees(loginUser, store, employeeIds));
    }

    private WeixinGroupEntity getExistCommonGroup(StoreEntity store, String groupId) {
        Optional<WeixinGroupEntity> groupOpt = getBean(WeixinGroupAction.class).findGroup(store, groupId);
        Preconditions.checkState(groupOpt.isPresent(), String.format("门店[%s]无分组[%s]存在", store.getId(), groupId));
        return groupOpt.get();
    }

//	public List<MemberDTO> searchFriend(String groupId, Map<String, Object> searchs, StoreEntity store) {
//		Preconditions.checkArgument(!Strings.isNullOrEmpty(groupId), "入参groupId不能为空");
//		Objects.requireNonNull(store);
//		Optional<WeixinGroupEntity> groupOpt = getBean(WeixinGroupAction.class).findGroup(store, groupId);
//		if (!groupOpt.isPresent())
//			return Lists.newArrayList();
//		return getBean(WeixinGroupAction.class).searchFriend(store,groupOpt.get(), searchs);
//	}

    public List<MemberDTO> searchFriend(String groupId, Map<String, Object> searchs, StoreEntity store) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupId), "入参groupId不能为空");
        Objects.requireNonNull(store);
        return getBean(WeixinGroupAction.class).searchFriend(store, groupId, searchs);
    }

    @SuppressWarnings("unchecked")
    public Set<MemberDTO> listFriends(List<String> groupIds, StoreEntity store) {
        Objects.requireNonNull(store);
        if (CollectionUtils.isEmpty(groupIds)) return Collections.EMPTY_SET;
        Set<MemberDTO> memberDtos = Sets.newHashSet();
        if (WeixinGroupFactory.hasAllFriendGroup(groupIds)) {
            memberDtos.addAll(getBean(WeixinGroupAction.class).listAllFriendGroupFriends(store));
            return memberDtos;
        }
        if (WeixinGroupFactory.hasNewFriendGroup(groupIds))
            memberDtos.addAll(getBean(WeixinGroupAction.class).listNewFriendGroupFriends(store));
        if (WeixinGroupFactory.hasLabelGroup(groupIds)) {
            List<String> labelGroupIds = WeixinGroupFactory.getLabelGroupIds(groupIds);
            memberDtos.addAll(getBean(WeixinGroupAction.class).listLabelGroupFriends(store, labelGroupIds));
        }
        if (WeixinGroupFactory.hasCommonGroup(groupIds)) {
            List<String> commonGroupIds = WeixinGroupFactory.getCommonGroupIds(groupIds);
            memberDtos.addAll(getBean(WeixinGroupAction.class).listCommonGroupFriends(store, commonGroupIds));
        }
        return memberDtos;
    }

    public void resetGroupsAndFriends(LoginUserContext userContext, List<String> groupIds, String friendId) {
        Optional<WebChatUserEntity> weixinOpt = getBean(WebChatUserAction.class).findById(userContext.getExitsStore(), friendId);
        Preconditions.checkArgument(weixinOpt.isPresent(), String.format("微信好友[%s]不存在", friendId));
        List<WeixinGroupEntity> resetableGroups = getResetableGroups(userContext);
        List<WeixinGroupEntity> resetGroups = getResetGroups(resetableGroups, groupIds);
        getBean(WeixinGroupAction.class).removeFriendFromGroups(weixinOpt.get(), resetableGroups);
        getBean(WeixinGroupAction.class).addFriendToGroups(weixinOpt.get(), resetGroups);
    }

    /**
     * 获取可重置的分组
     *
     * @param userContext
     * @return
     */
    private List<WeixinGroupEntity> getResetableGroups(LoginUserContext userContext) {
        List<WeixinGroupEntity> resetableGroups = Lists.newArrayList();
        for (WeixinGroupEntity group : loadNoMemberGroups(userContext)) {
            if (group.isEditable()) resetableGroups.add(group);
        }
        return resetableGroups;
    }

    @SuppressWarnings("unchecked")
    private List<WeixinGroupEntity> getResetGroups(List<WeixinGroupEntity> resetableGroups, List<String> groupIds) {
        if (CollectionUtils.isEmpty(resetableGroups)) return EMPTY_LIST;
        List<WeixinGroupEntity> resetGroups = Lists.newArrayList();
        for (WeixinGroupEntity group : resetableGroups)
            if (groupIds.contains(group.getId())) resetGroups.add(group);
        return resetGroups;
    }

    public void toGroup(StoreEntity store, String orgin, String dest) {
        Objects.requireNonNull(store);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(orgin), "原分组ID不能为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dest), "目标分组ID不能为空");
        Optional<WeixinGroupEntity> lableGroupOpt = getBean(WeixinGroupAction.class).loadLableGroup(store, orgin);
        Preconditions.checkState(lableGroupOpt.isPresent(), String.format("门店[%s]标签分组[%s]", store.getId(), orgin));
        List<MemberDTO> labelMembers = getBean(WeixinGroupAction.class).listLabelGroupFriends(store, lableGroupOpt.get());
        List<String> weixinIds = Lists.newArrayList();
        for (MemberDTO member : labelMembers) weixinIds.add(member.getUserName());
        addFriends(dest, weixinIds, store);
    }

    public List<WeixinGroupEntity> loadGroupsForWeixin(StoreEntity store) {
        Objects.requireNonNull(store);
        return getBean(WeixinGroupAction.class).loadLabelGroups(store);
    }

    /**
     * 生成导购组
     *
     * @param store
     * @param userContext
     * @return
     */
    public void addGuideGroups(StoreEntity store, LoginUserContext userContext) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(userContext);
        Preconditions.checkState(userContext.getCompany().isPresent(), "当前用户不存在公司ID");
        Optional<List<EmployeeEntity>> empsOpt = getBean(EmployeeServer.class).loadEnabledShoppingGuides(store.getId(),
                userContext);
        Preconditions.checkState(empsOpt.isPresent() && (!empsOpt.get().isEmpty()), "门店[%s]没有导购", store.getId());
        List<EmployeeEntity> employees = empsOpt.get();
        getBean(WeixinGroupAction.class).addAndGrantGuideGroups(store, employees);
    }

    /**
     * 创建导购组
     *
     * @param store
     * @param employees
     * @return
     */
    private GroupMemberDTO createGuideGroups(StoreEntity store, List<EmployeeEntity> employees) {
        WeixinGroupEntity guideGroup = WeixinGroupEntity.createGuideGroup(store);
        List<MemberDTO> members = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(employees)) {
            for (EmployeeEntity emp : employees) {
                members.add(new MemberDTO(String.valueOf(emp.getId()), "", String.valueOf(emp.getId()),
                        emp.getUserName(), "", -1, 0, "", ""));
            }
        }
        return new GroupMemberDTO(guideGroup.getId(), guideGroup.getName(),
                members.size(), members);
    }

    /**
     * 给当前登录用户获取已授权的分组列表
     *
     * @param userContext
     * @return
     */
    public List<GroupMemberDTO> loadGrantedGroupsWithMembersForGuide(LoginUserContext userContext, final Map<String, Object> searchs) {
        Objects.requireNonNull(userContext);
        final StoreEntity store = userContext.getExitsStore();
        final EmployeeEntity employee = userContext.getEmployee();
        List<GroupMemberDTO> groups = Lists.newArrayList();
        final CountDownLatch latch = new CountDownLatch(3);
        Future<Optional<GroupMemberDTO>> allFriendGroupFuture = executor.submit(new Callable<Optional<GroupMemberDTO>>() {

            @Override
            public Optional<GroupMemberDTO> call() throws Exception {
                try {
                    return getBean(WeixinGroupAction.class).searchGrantedAllFriendGroupWithFriends(store, employee, searchs);
                } finally {
                    latch.countDown();
                }
            }
        });
        Future<Optional<GroupMemberDTO>> newFriendGroupFuture = executor.submit(new Callable<Optional<GroupMemberDTO>>() {

            @Override
            public Optional<GroupMemberDTO> call() throws Exception {
                try {
                    return getBean(WeixinGroupAction.class).searchNewFriendGroupWithFriends(store, searchs);
                } finally {
                    latch.countDown();
                }
            }
        });
        Future<List<GroupMemberDTO>> commonGroupsFuture = executor.submit(new Callable<List<GroupMemberDTO>>() {

            @Override
            public List<GroupMemberDTO> call() throws Exception {
                try {
                    return getBean(WeixinGroupAction.class).searchGrantedCommonGroupsWithFriends(store, employee, searchs);
                } finally {
                    latch.countDown();
                }
            }
        });

        try {
            latch.await(60, TimeUnit.SECONDS);
            Optional<GroupMemberDTO> allFriendGroupOpt = allFriendGroupFuture.get();
            if (allFriendGroupOpt.isPresent()) groups.add(allFriendGroupOpt.get());
            Optional<GroupMemberDTO> newFriendGroupOpt = newFriendGroupFuture.get();
            if (newFriendGroupOpt.isPresent()) groups.add(newFriendGroupOpt.get());
            List<GroupMemberDTO> commonGroups = commonGroupsFuture.get();
            groups.addAll(commonGroups);
            GroupMemberDTO guideGroup = getGuideGroup(store, userContext);
            groups.add(guideGroup);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public List<GroupMemberDTO> loadGroupsWithMembersForStore(StoreEntity store) {
        List<GroupMemberDTO> groups = Lists.newArrayList();
        GroupMemberDTO allFriendGroup = getBean(WeixinGroupAction.class).loadAllFriendGroupWithFriends(store);
        groups.add(allFriendGroup);
        GroupMemberDTO newFriendGroup = getBean(WeixinGroupAction.class).loadNewFriendGroupWithFriends(store);
        groups.add(newFriendGroup);
        List<GroupMemberDTO> commonGroups = getBean(WeixinGroupAction.class).loadCommonGroupsWithFriends(store);
        groups.addAll(commonGroups);
        return groups;
    }

    public List<GroupMemberDTO> loadGroupsWithMembersForWeixin(StoreEntity store) {
        return getBean(WeixinGroupAction.class).loadLabelGroupsWithFriends(store);
    }

    private GroupMemberDTO getGuideGroup(StoreEntity store, LoginUserContext userContext) {
        Optional<List<EmployeeEntity>> empsOpt = getBean(EmployeeServer.class).loadEnabledShoppingGuides(store.getId(),
                userContext);
        if (empsOpt.isPresent()) {
            return createGuideGroups(store, empsOpt.get());
        } else {
            return createGuideGroups(store, null);
        }
    }

    public List<WeixinGroupEntity> loadNoMemberGroups(LoginUserContext userContext) {
        Objects.requireNonNull(userContext);
        StoreEntity store = userContext.getExitsStore();
        EmployeeEntity employee = userContext.getEmployee();
        return getBean(WeixinGroupAction.class).loadAllGrantedGroups(store, employee);
    }

    private void grantAllFriendGroupToEmployees(LoginUserContext userContext, StoreEntity store) {
        WeixinGroupEntity allFriendGroup = getBean(WeixinGroupAction.class).loadAllFriendGroup(store);
        Optional<List<EmployeeEntity>> employeesOpt = getBean(EmployeeServer.class)
                .loadEnabledShoppingGuides(store.getId(), userContext);
        if (!employeesOpt.isPresent())
            return;
        getBean(WeixinGroupAction.class).grantToEmployees(store, allFriendGroup, employeesOpt.get());
    }

    public void addAndGrantedGuideGroupIfAbsent(LoginUserContext userContext, StoreEntity store) {
        Objects.requireNonNull(userContext);
        Objects.requireNonNull(store);
        EmployeeEntity employee = userContext.getEmployee();
        getBean(WeixinGroupAction.class).addAndGrantGuideIfAbsent(store, employee);
    }

    public void grantAllFriendGroupIfAbsent(LoginUserContext userContext, StoreEntity store) {
        Objects.requireNonNull(store);
        if (getBean(WeixinGroupAction.class).hasLogAllFriendGroup(store)) return;
        getBean(WeixinGroupAction.class).saveGrantedLogForAllFriendGroup(store);
        getBean(WeixinGroupAction.class).clearGrantedAllFriendGroup(store);
        grantAllFriendGroupToEmployees(userContext, store);
    }


}
