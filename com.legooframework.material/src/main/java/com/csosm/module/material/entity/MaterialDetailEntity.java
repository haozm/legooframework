package com.csosm.module.material.entity;

import com.csosm.commons.adapter.LoginUserContext;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.util.MyWebUtil;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaterialDetailEntity extends BaseEntity<Long> {

    private final int group;
    private final String groupName;
    private final int rangge;
    private final Integer orgId, companyId;
    private int type, size;
    private boolean enabled, blacked = false;
    private LocalDate deadline;
    private int useTimes;
    private List<Material> materials;

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> data = super.toMap();
        data.put("group", group);
        data.put("range", rangge);
        data.put("orgId", orgId);
        data.put("companyId", companyId);
        data.put("type", type);
        data.put("useTimes", useTimes);
        data.put("size", size);
        data.put("enabled", enabled ? 1 : 0);
        data.put("deadline", deadline == null ? null : deadline.toDate());
        data.put("materials", MyWebUtil.toJson(materials));
        return data;
    }

    public Map<String, Object> toViewBean() {
        return this.toMap();
    }

    MaterialDetailEntity(Long id, MaterialGroupEntity group, int range, Integer orgId, Integer companyId,
                         List<Material> materials, boolean enabled, Date deadline) {
        super(id);
        Preconditions.checkArgument(range >= 1 && range <= 4);
        Preconditions.checkNotNull(group);
        this.group = group.getId();
        this.groupName = null;
        this.rangge = range;
        this.orgId = orgId;
        this.deadline = deadline == null ? null : LocalDate.fromDateFields(deadline);
        this.enabled = enabled;
        this.companyId = companyId;
        setMaterials(materials);
    }

    MaterialDetailEntity(Long id, int group, int rangge, Integer orgId, Integer companyId, int type, int size,
                         boolean enabled, Date deadline, List<Material> materials, int useTimes) {
        super(id);
        this.group = group;
        this.groupName = null;
        this.rangge = rangge;
        this.orgId = orgId;
        this.companyId = companyId;
        this.type = type;
        this.useTimes = useTimes;
        this.size = size;
        this.enabled = enabled;
        this.deadline = deadline == null ? null : LocalDate.fromDateFields(deadline);
        this.materials = materials;
    }

    // 门店创建
    static MaterialDetailEntity createByStore(long id, MaterialGroupEntity group, StoreEntity store,
                                              List<Material> materials, Date deadline, LoginUserContext userContext) {
        Preconditions.checkNotNull(store);
        Preconditions.checkNotNull(userContext);
        Preconditions.checkState(store.getCompanyId().isPresent());
        MaterialDetailEntity m = new MaterialDetailEntity(id, group, 4, store.getId(), store.getCompanyId().get(),
                materials, true, deadline);
        m.init4Create(userContext.getUserId());
        return m;
    }

    // // 超管创建
    // static MaterialDetailEntity createByAdmin(long id, MaterialGroupEntity group,
    // List<Material> materials,
    // Date deadline, LoginUserContext userContext) {
    // Preconditions.checkNotNull(userContext);
    // MaterialDetailEntity m = new MaterialDetailEntity(id, group, 4, -1, -1,
    // materials, true, deadline);
    // m.init4Create(userContext.getUserId());
    // return m;
    // }

    // 超管创建
    static MaterialDetailEntity createByCsosm(long id, MaterialGroupEntity group, List<Material> materials,
                                              Date deadline) {
        MaterialDetailEntity m = new MaterialDetailEntity(id, group, 1, -1, -1, materials, true, deadline);
        m.init4Create(-1L);
        return m;
    }

    // 组织创建（含公司 与 组织）
    static MaterialDetailEntity createByOrg(long id, MaterialGroupEntity group, OrganizationEntity organization,
                                            List<Material> materials, Date deadline, LoginUserContext userContext) {
        Preconditions.checkNotNull(organization);
        MaterialDetailEntity m;
        if (organization.isCompany()) {
            m = new MaterialDetailEntity(id, group, 2, organization.getId(), organization.getMyCompanyId(), materials,
                    true, deadline);
        } else {
            m = new MaterialDetailEntity(id, group, 3, organization.getId(), organization.getMyCompanyId(), materials,
                    true, deadline);
        }
        m.init4Create(userContext.getUserId());
        return m;
    }

    boolean isSameOrg(LoginUserContext user) {
        if (user.getEmployee().isAdmin()) {
            return isCsosm();
        }
        if (user.getStore().isPresent() && isStore()) {
            return user.getStore().get().getId().equals(this.orgId);
        }
        if (user.getOrganization().isPresent() && isOrg()) {
            return user.getOrganization().get().getId().equals(this.orgId);
        }
        return false;
    }

    Optional<MaterialDetailEntity> enabled() {
        if (isEnabled())
            return Optional.absent();
        MaterialDetailEntity clone = cloneMe();
        clone.enabled = true;
        return Optional.of(clone);
    }

    public boolean isBlacked() {
        return blacked;
    }

    public void setBlacked() {
        this.blacked = true;
    }

    Optional<MaterialDetailEntity> disabled() {
        if (!isEnabled())
            return Optional.absent();
        MaterialDetailEntity clone = cloneMe();
        clone.enabled = false;
        return Optional.of(clone);
    }

    Optional<MaterialDetailEntity> editInfo(List<Material> materials, Date deadline) {
        Preconditions.checkState(isEnabled(), "状态停用，无法编辑...");
        Preconditions.checkState(isInEffectiveDate(), "超过有效期范围，无法编辑....");
        LocalDate localDate = deadline == null ? null : LocalDate.fromDateFields(deadline);
        if (ListUtils.isEqualList(this.materials, materials) && Objects.equal(this.deadline, localDate))
            return Optional.absent();
        MaterialDetailEntity clone = cloneMe();
        clone.setMaterials(materials);
        clone.deadline = localDate;
        return Optional.of(clone);
    }

    void setMaterials(List<Material> materials) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(materials), "素材不可以为空....");
        this.materials = Lists.newArrayList(materials);
        this.size = materials.size();
        if (materials.size() == 1) {
            this.type = materials.get(0).type;
        } else {
            Set<Integer> types = Sets.newHashSet();
            for (Material $it : materials)
                types.add($it.type);
            this.type = types.size() == 1 ? materials.get(0).type : 99;
        }
    }

    public boolean hasDeadline() {
        return null != this.deadline;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isInEffectiveDate() {
        if (null == this.deadline)
            return true;
        return DateTime.now().toLocalDate().isBefore(this.deadline);
    }

    public Integer getOrgId() {
        return orgId;
    }

    public int getGroup() {
        return group;
    }

    public String getGroupName() {
        return groupName;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public int getSize() {
        return size;
    }

    public boolean isMix() {
        return 99 == this.type;
    }

    protected MaterialDetailEntity cloneMe() {
        try {
            MaterialDetailEntity clone = (MaterialDetailEntity) super.clone();
            clone.materials = Lists.newArrayList(this.materials);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isCsosm() {
        return 1 == this.rangge;
    }

    public boolean isCompany() {
        return 2 == this.rangge;
    }

    public boolean isOrg() {
        return 3 == this.rangge;
    }

    public boolean isStore() {
        return 4 == this.rangge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MaterialDetailEntity that = (MaterialDetailEntity) o;
        return type == that.type && size == that.size && group == that.group && rangge == that.rangge
                && enabled == that.enabled && Objects.equal(orgId, that.orgId) && Objects.equal(deadline, that.deadline)
                && Objects.equal(companyId, that.companyId) && Objects.equal(materials, that.materials);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), group, rangge, orgId, companyId, type, size, materials);
    }

    public boolean isReplaceAble() {
        boolean isReplace = true;
        for (Material m : materials)
            isReplace = isReplace & m.isReplaceAble();
        return isReplace;
    }
    
    public void incrementUseTimes() {
    	this.useTimes ++;
    }
    
    public int getUseTimes() {
    	return this.useTimes;
    }
    
    public class Material implements Cloneable {

        private int type;
        private String content;

        public Material(int type, String content) {
            this.type = type;
            this.content = content;
        }

        public boolean isReplaceAble() {
            if (type != 0 && type != 9)
                return false;
            if (CollectionUtils.isEmpty(getReplaceWords())) return false;
            return true;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getType() {
            return type;
        }

        public String getContent() {
            return content;
        }

        public List<String> getReplaceWords() {
            List<String> list = Lists.newArrayList();
            Pattern p = Pattern.compile("(\\{[^\\}]*\\})");
            Matcher m = p.matcher(this.content);
            while (m.find()) {
                list.add(m.group().substring(1, m.group().length() - 1));
            }
            return list;
        }

        public String execToSmsContent(Map<String, String> params) {
            if (params.isEmpty()) return this.content;
            for (String key : getReplaceWords()) {
                String word = String.format("{%s}", key);
                if (!params.containsKey(word)) continue;
                String val = params.get(word);
                if (val == null) val = "";
                this.content = this.content.replaceAll(word, val);
            }
            return this.content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Material material = (Material) o;
            return type == material.type && Objects.equal(content, material.content);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(type, content);
        }

		@Override
		public String toString() {
			return "Material [type=" + type + ", content=" + content + "]";
		}

      
    }

	@Override
	public String toString() {
		return "MaterialDetailEntity [group=" + group + ", groupName=" + groupName + ", rangge=" + rangge + ", orgId="
				+ orgId + ", companyId=" + companyId + ", type=" + type + ", size=" + size + ", enabled=" + enabled
				+ ", blacked=" + blacked + ", deadline=" + deadline + ", useTimes=" + useTimes + ", materials="
				+ materials + "]";
	}

    
}
