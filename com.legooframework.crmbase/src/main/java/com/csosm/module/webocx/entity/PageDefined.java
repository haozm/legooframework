package com.csosm.module.webocx.entity;

import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PageDefined {

    private final Integer companyId;
    private final Set<Integer> storeIds;
    private final String subId;
    private final List<ColumMeta> metas;
    private final List<OcxItem> ocxItems;
    private final List<CdnItem> cdnItems;
    private final List<Operate> operates;
    private final List<Operate> buttons;
    private final String active, title, desc;
    private final List<SubPageDefined> subPageDefineds;

    PageDefined(String subId, Integer companyId, Set<Integer> storeIds, List<ColumMeta> metas, List<OcxItem> ocxItems,
                List<CdnItem> cdnItems, String active, String title, String desc,
                List<Operate> operates,
                List<Operate> buttons, List<SubPageDefined> subPageDefineds) {
        this.companyId = companyId;
        this.subId = subId;
        this.metas = metas;
        this.storeIds = storeIds;
        this.ocxItems = ocxItems;
        this.active = active;
        this.desc = desc;
        this.cdnItems = cdnItems;
        this.operates = operates;
        this.buttons = buttons;
        this.title = title;
        this.subPageDefineds = subPageDefineds;
    }

    public Optional<List<Operate>> getOperates() {
        return Optional.fromNullable(operates);
    }

    String getSubId() {
        return subId;
    }

    public Optional<List<Operate>> getButtons() {
        return Optional.fromNullable(buttons);
    }

    public Optional<String> getTitle() {
        return Optional.fromNullable(title);
    }

    public String getActive() {
        return active;
    }

    public Optional<List<SubPageDefined>> getSubPageDefineds() {
        return Optional.fromNullable(subPageDefineds);
    }

    public Optional<String> getDesc() {
        return Optional.fromNullable(desc);
    }

    public Optional<List<ColumMeta>> getMetas() {
        return Optional.fromNullable(CollectionUtils.isEmpty(metas) ? null : metas);
    }

    public Optional<List<CdnItem>> getCdnItems() {
        return Optional.fromNullable(cdnItems);
    }

    public void holdParam(Map<String, Object> params) {
        if (CollectionUtils.isEmpty(cdnItems)) return;
        for (CdnItem item : cdnItems) {
            item.holdParam(params);
        }
    }

    boolean isOnly4Com(OrganizationEntity company) {
        return (!this.getStoreIds().isPresent() && this.companyId.equals(company.getId())) || this.companyId == -1;
    }

    boolean isCom(OrganizationEntity company) {
        return !this.getStoreIds().isPresent() && this.companyId.equals(company.getId());
    }

    boolean isStore(StoreEntity store) {
        return this.getStoreIds().isPresent() && this.getStoreIds().get().contains(store.getId()) &&
                this.companyId.equals(store.getCompanyId().orNull());
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Optional<Set<Integer>> getStoreIds() {
        return Optional.fromNullable(storeIds);
    }

    public List<OcxItem> getOcxItems() {
        return ocxItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageDefined that = (PageDefined) o;
        return Objects.equal(companyId, that.companyId) &&
                Objects.equal(metas, that.metas) &&
                Objects.equal(ocxItems, that.ocxItems);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, metas, ocxItems);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("metas", metas)
                .add("ocxItems", ocxItems)
                .add("cdnItems", cdnItems)
                .toString();
    }
}
