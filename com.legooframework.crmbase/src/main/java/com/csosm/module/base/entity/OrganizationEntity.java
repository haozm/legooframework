package com.csosm.module.base.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.entity.Replaceable;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

public class OrganizationEntity extends BaseEntity<Integer> implements Replaceable {

    private String code;
    private Integer parentId;
    // 1 com  2 org
    private Integer type = 2;
    private String name;
    private String shortName;
    private Integer status = 1;
    private Integer depth;
    private boolean rootNode;
    // 所属行业类别 1、女士内衣 2、女装   //数据字段
    private Integer industryType;

    private List<OrganizationEntity> subOrgs;
    // 组织显示（0：显示所有组织；1：不显示无业绩门店）
    private Integer orgShowFlag = 0;
    // 手机号隐藏（0：隐藏；1：不隐藏）
    private Integer hiddenMemberPhoneFlag = 1;
    private String linkMan, linkPhone;

    OrganizationEntity(OrganizationEntity parent, Integer id, String name, String shortName) {
        super(id);
        this.code = String.format("%s_%s", parent.getCode(), id);
        this.parentId = parent.getId();
        this.name = name;
        this.shortName = shortName;
    }

    OrganizationEntity(Integer id, String name, String shortName, Integer industryType, String linkMan, String linkPhone) {
        super(id);
        this.code = id.toString();
        this.name = name;
        this.shortName = shortName;
        this.linkMan = linkMan;
        this.linkPhone = linkPhone;
        this.industryType = industryType;
    }

    OrganizationEntity(Integer id, String code, Integer parentId, Integer type,
                       String name, String shortName, Integer status, Integer depth,
                       boolean rootNode, Integer industryType, Integer orgShowFlag,
                       Integer hiddenMemberPhoneFlag, String linkMan, String linkPhone) {
        super(id);
        this.code = code;
        this.parentId = parentId;
        this.type = type;
        this.name = name;
        this.shortName = shortName;
        this.status = status;
        this.depth = depth;
        this.rootNode = rootNode;
        this.subOrgs = Lists.newArrayList();
        this.industryType = industryType;
        this.orgShowFlag = orgShowFlag;
        this.hiddenMemberPhoneFlag = hiddenMemberPhoneFlag;
        this.linkMan = linkMan;
        this.linkPhone = linkPhone;
    }

    public OrganizationEntity modifyOrganization(String name, String shortName, Integer hiddenPhone) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "组织名称不能为空");
        Preconditions.checkNotNull(type, "组织类型不能为空");
        Preconditions.checkArgument(type == 1 || type == 2, "组织类型为1 或 2");
        Preconditions.checkArgument(hiddenPhone == 0 || hiddenPhone == 1, "隐藏会员电话为0 或 1");
        OrganizationEntity clone = null;
        try {
            clone = (OrganizationEntity) this.clone();
            clone.name = name;
            clone.shortName = shortName;
            clone.hiddenMemberPhoneFlag = hiddenPhone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("克隆组织异常");
        }
        return clone;
    }

    public OrganizationEntity modifyCompany(String name, String shortName, Integer industryType,
                                            Integer showAchievementOrg, Integer hiddenPhone) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "组织名称不能为空");
        Preconditions.checkNotNull(type, "组织类型不能为空");
        Preconditions.checkArgument(type == 1 || type == 2, "组织类型为1 或 2");
        Preconditions.checkArgument(hiddenPhone == 0 || hiddenPhone == 1, "隐藏会员电话为0 或 1");
        OrganizationEntity clone = null;
        try {
            clone = (OrganizationEntity) this.clone();
            clone.name = name;
            clone.shortName = shortName;
            clone.industryType = industryType;
            clone.orgShowFlag = showAchievementOrg;
            clone.hiddenMemberPhoneFlag = hiddenPhone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("克隆组织异常");
        }
        return clone;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("id", getId());
        param.put("code", code);
        param.put("type", type);
        param.put("name", name);
        param.put("shortName", Strings.isNullOrEmpty(this.shortName) ? "" : this.shortName);
        param.put("parentId", this.parentId);
        param.put("status", this.status == null ? -1 : this.status);
        param.put("industryType", this.industryType == null ? -1 : this.industryType);
        param.put("hiddenMemberPhoneFlag", this.hiddenMemberPhoneFlag == null ? 1 : this.hiddenMemberPhoneFlag);
        param.put("orgShowFlag", this.orgShowFlag == null ? -1 : this.orgShowFlag);
        return param;
    }

    public OrgTreeViewDto buildOrgTreeDto() {
        return new OrgTreeViewDto(this);
    }

    public Integer getMyCompanyId() {
        if (isCompany()) return getId();
        String[] splits = StringUtils.split(code, '_');
        Preconditions.checkPositionIndex(
                0, splits.length, String.format("异常的错误组织代码 code = %s", getCode()));
        return Integer.valueOf(splits[0]);
    }

    public String getCode() {
        return code;
    }

    // 是否是他的上级组织
    public boolean isMySuperOrg(OrganizationEntity org) {
        return org.getCode().length() < this.code.length() && StringUtils.startsWith(this.code, String.format("%s_", org.getCode()));
    }

    // 是否是他的下级组织
    public boolean isMySubOrg(OrganizationEntity org) {
        return org.getCode().length() > this.code.length() && StringUtils.startsWith(org.getCode(), String.format("%s_", this.code));
    }

    void setSubOrgs(List<OrganizationEntity> subOrgs) {
        if (CollectionUtils.isEmpty(subOrgs)) return;
        for (OrganizationEntity cur : subOrgs) {
            if (Objects.equal(getId(), cur.getParentId())) {
                this.subOrgs.add(cur);
            }
        }
    }

    public boolean isMyParent(OrganizationEntity parent) {
        Preconditions.checkNotNull(parent);
        return this.parentId == parent.getId();
    }

    public boolean isMyself(OrganizationEntity parent) {
        Preconditions.checkNotNull(parent);
        return this.getId() == parent.getId();
    }

    public int getLevel() {
        return StringUtils.split(code, '_').length;
    }

    public boolean hasSubOrgs() {
        return !CollectionUtils.isEmpty(subOrgs);
    }

    public List<OrganizationEntity> getSubOrgs() {
        return CollectionUtils.isEmpty(subOrgs) ? null : ImmutableList.copyOf(subOrgs);
    }

    public Object getParentId() {
        return parentId;
    }

    public boolean isCompany() {
        return Objects.equal(1, type);
    }

    public boolean isDept() {
        return Objects.equal(2, type);
    }

    public boolean isStore() {
        return Objects.equal(3, type);
    }

    public String getName() {
        return name;
    }

    public Optional<String> getShortName() {
        return Optional.fromNullable(shortName);
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getIndustryType() {
        return industryType;
    }

    public Integer getDepth() {
        return depth;
    }

    public boolean isRootNode() {
        return rootNode;
    }

    /**
     * 显示所有单位
     */
    public void showAllOrg() {
        this.orgShowFlag = 0;
    }

    /**
     * 显示有2年内业绩的单位
     */
    public void showAchievementOrg() {
        this.orgShowFlag = 1;
    }

    /**
     * 显示会员电话
     */
    public void showMemberPhone() {
        this.hiddenMemberPhoneFlag = 1;
    }

    /**
     * 隐藏会员电话
     */
    public void hiddenMemberPhone() {
        this.hiddenMemberPhoneFlag = 0;
    }

    public Integer getType() {
        return type;
    }

    public Integer getOrgShowFlag() {
        return orgShowFlag;
    }

    public Integer getHiddenMemberPhoneFlag() {
        return hiddenMemberPhoneFlag;
    }

    public void setCreateUser(LoginUserContext loginUser) {
        this.setCreateUserId(loginUser.getUserId());
    }

    public void setModifyUser(LoginUserContext loginUser) {
        this.setModifyUserId(loginUser.getUserId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OrganizationEntity that = (OrganizationEntity) o;
        return rootNode == that.rootNode
                && Objects.equal(code, that.code)
                && Objects.equal(industryType, that.industryType)
                && Objects.equal(parentId, that.parentId)
                && Objects.equal(type, that.type)
                && Objects.equal(name, that.name)
                && Objects.equal(shortName, that.shortName)
                && Objects.equal(status, that.status)
                && Objects.equal(depth, that.depth)
                && Objects.equal(orgShowFlag, that.orgShowFlag)
                && Objects.equal(hiddenMemberPhoneFlag, that.hiddenMemberPhoneFlag);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                super.hashCode(), code, parentId, type, name, industryType, shortName, status, depth, rootNode);
    }

    @Override
    public Map<String, String> toSmsMap(StoreEntity store) {
        Map<String, String> map = Maps.newHashMap();
        map.put("{组织名称}", this.name);
        return map;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("name", this.name);
        map.put("shortName", this.shortName);
        map.put("type", this.type);
        map.put("parentId", this.parentId);
        map.put("code", this.code);
        map.put("status", this.status);
        map.put("industryType", this.industryType);
        map.put("hiddenMemberPhoneFlag", this.hiddenMemberPhoneFlag);
        map.put("orgShowFlag", this.orgShowFlag);
        map.put("linkMan", this.linkMan);
        map.put("linkPhone", this.linkPhone);
        return map;
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("code", code)
                .add("parentId", parentId)
                .add("type", type)
                .add("name", name)
                .add("shortName", shortName)
                .add("status", status)
                .add("depth", depth)
                .add("rootNode", rootNode)
                .add("industryType", industryType)
                .add("orgShowFlag", this.orgShowFlag)
                .add("hiddenMemberPhoneFlag", this.hiddenMemberPhoneFlag)
                .add("subOrgs's size", hasSubOrgs() ? subOrgs.size() : 0)
                .toString();
    }
}
