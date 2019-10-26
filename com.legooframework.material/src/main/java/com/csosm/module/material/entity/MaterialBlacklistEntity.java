package com.csosm.module.material.entity;

import java.util.Map;

import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class MaterialBlacklistEntity extends BaseEntity<Integer> {

    private Set<Long> blacklist;
    private Set<Long> whitelist;
    private final int range, companyId;

    MaterialBlacklistEntity(Integer id, int range, int companyId, Set<Long> blacklist, Set<Long> whitelist) {
        super(id);
        this.blacklist = blacklist == null ? Sets.<Long>newHashSet() : blacklist;
        this.whitelist = whitelist == null ? Sets.<Long>newHashSet() : whitelist;
        this.range = range;
        this.companyId = companyId;
    }

    static MaterialBlacklistEntity createAnyListByUser(LoginUserContext user, MaterialDetailEntity material, boolean isblist) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(material);
        Set<Long> matelist = Sets.newHashSet();
        matelist.add(material.getId());
        if (user.getStore().isPresent()) {
            return new MaterialBlacklistEntity(user.getStore().get().getId(), 4, user.getStore().get()
                    .getCompanyId().or(-1), isblist ? matelist : null, isblist ? null : matelist);
        } else if (user.getOrganization().isPresent()) {
            return new MaterialBlacklistEntity(user.getOrganization().get().getId(), user.getOrganization().get().isCompany() ? 2 : 3,
                    user.getOrganization().get().getMyCompanyId(), isblist ? matelist : null, isblist ? null : matelist);
        } else {
            throw new IllegalArgumentException("非法的用户，同时赋值门店与组织....");
        }

    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> data = super.toMap();
        if (CollectionUtils.isEmpty(blacklist)) {
            data.put("blacklist", null);
        } else {
            data.put("blacklist", Joiner.on(',').join(blacklist));
        }
        if (CollectionUtils.isEmpty(whitelist)) {
            data.put("whitelist", null);
        } else {
            data.put("whitelist", Joiner.on(',').join(whitelist));
        }
        data.put("companyId", companyId);
        data.put("range", range);
        return data;
    }

    public Optional<MaterialBlacklistEntity> addBlackMaterial(MaterialDetailEntity material) {
        if (this.contains(material)) return Optional.absent();
        MaterialBlacklistEntity clone = this.cloneMe();
        clone.blacklist.add(material.getId());
        return Optional.of(clone);
    }

    public Optional<MaterialBlacklistEntity> removeBlackMaterial(MaterialDetailEntity material) {
        if (!this.contains(material)) return Optional.absent();
        MaterialBlacklistEntity clone = this.cloneMe();
        clone.blacklist.remove(material.getId());
        return Optional.of(clone);
    }

    public Optional<MaterialBlacklistEntity> addFansMaterial(MaterialDetailEntity material) {
        if (this.whitelist.contains(material.getId())) return Optional.absent();
        MaterialBlacklistEntity clone = this.cloneMe();
        clone.whitelist.add(material.getId());
        return Optional.of(clone);
    }

    public Optional<MaterialBlacklistEntity> removeFansMaterial(MaterialDetailEntity material) {
        if (!this.whitelist.contains(material.getId())) return Optional.absent();
        MaterialBlacklistEntity clone = this.cloneMe();
        clone.whitelist.remove(material.getId());
        return Optional.of(clone);
    }

    public boolean canBeDel() {
        return CollectionUtils.isEmpty(blacklist) && CollectionUtils.isEmpty(whitelist);
    }

    protected MaterialBlacklistEntity cloneMe() {
        try {
            MaterialBlacklistEntity clone = (MaterialBlacklistEntity) this.clone();
            clone.blacklist = CollectionUtils.isEmpty(this.blacklist) ? Sets.<Long>newHashSet() : Sets.newHashSet(this.blacklist);
            clone.whitelist = CollectionUtils.isEmpty(this.whitelist) ? Sets.<Long>newHashSet() : Sets.newHashSet(this.whitelist);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isBlackEmpty() {
        return CollectionUtils.isEmpty(blacklist);
    }

    public boolean isWhiteEmpty() {
        return CollectionUtils.isEmpty(whitelist);
    }

    public Set<Long> getWhitelist() {
        return whitelist;
    }

    public Set<Long> getBlacklist() {
        return blacklist;
    }

    public boolean contains(MaterialDetailEntity material) {
        return this.blacklist.contains(material.getId());
    }

    public int getRange() {
        return range;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MaterialBlacklistEntity that = (MaterialBlacklistEntity) o;
        return range == that.range &&
                companyId == that.companyId &&
                SetUtils.isEqualSet(blacklist, that.blacklist);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), companyId, blacklist, range);
    }

	@Override
	public String toString() {
		return "MaterialBlacklistEntity [blacklist=" + blacklist + ", whitelist=" + whitelist + ", range=" + range
				+ ", companyId=" + companyId + "]";
	}
   
}
