package com.legooframework.model.scheduler.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.DataRange;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class JobRunParams {

    private Integer companyId, storeId;
    private Set<Integer> excludeComs;
    private Multimap<Integer, Integer> excludeStores;

    JobRunParams(Integer companyId, Integer storeId) {
        this.companyId = companyId;
        this.storeId = storeId;
    }

    synchronized void setExcludeComs(Integer companyId) {
        if (excludeComs == null) this.excludeComs = Sets.newHashSet();
        this.excludeComs.add(companyId);
    }

    synchronized void setExcludeStore(Integer companyId, Integer storeId) {
        if (excludeStores == null) this.excludeStores = ArrayListMultimap.create();
        this.excludeStores.put(companyId, storeId);
    }

    public boolean isGeneralJob() {
        return Objects.equal(this.companyId, -1) && Objects.equal(this.storeId, -1);
    }

    public boolean isCompanyJob() {
        return !Objects.equal(this.companyId, -1) && Objects.equal(this.storeId, -1);
    }

    public boolean isStoreJob() {
        return !Objects.equal(this.companyId, -1) && !Objects.equal(this.storeId, -1);
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    private boolean filterByStore(DataRange dataRange) {
        return this.companyId.equals(dataRange.getCompanyId()) && this.storeId.equals(dataRange.getStoreId());
    }

    private boolean filterByCompany(DataRange dataRange) {
        if (this.companyId.equals(dataRange.getCompanyId())) {
            if (null == excludeStores) return true;
            Collection<Integer> storeIds = excludeStores.get(dataRange.getCompanyId());
            if (CollectionUtils.isEmpty(storeIds)) return true;
            return !storeIds.contains(dataRange.getStoreId());
        }
        return false;
    }

    private boolean filterByGeneral(DataRange dataRange) {
        if (null != excludeStores) {
            Collection<Integer> storeIds = excludeStores.get(dataRange.getCompanyId());
            if (CollectionUtils.isNotEmpty(storeIds) && storeIds.contains(dataRange.getStoreId())) {
                return false;
            }
        }
        if (null != excludeComs) return !excludeComs.contains(dataRange.getCompanyId());
        return true;
    }

    public <T extends DataRange> Optional<List<T>> filter(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) return Optional.empty();
        if (isStoreJob()) {
            Optional<T> exit_store = collection.stream().filter(this::filterByStore).findFirst();
            return exit_store.map(Lists::newArrayList);
        } else if (isCompanyJob()) {
            List<T> com_list = collection.stream().filter(this::filterByCompany).collect(Collectors.toList());
            return Optional.ofNullable(CollectionUtils.isEmpty(com_list) ? null : com_list);
        } else {
            List<T> gen_list = collection.stream().filter(this::filterByGeneral).collect(Collectors.toList());
            return Optional.ofNullable(CollectionUtils.isEmpty(gen_list) ? null : gen_list);
        }
    }

    public boolean hasExcludeComs() {
        return CollectionUtils.isNotEmpty(excludeComs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobRunParams that = (JobRunParams) o;
        return Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(excludeComs, that.excludeComs) &&
                Objects.equal(excludeStores, that.excludeStores);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, storeId, excludeComs, excludeStores);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("excludeComs", excludeComs)
                .add("excludeStores", excludeStores)
                .toString();
    }
}
