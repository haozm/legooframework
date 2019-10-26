package com.csosm.module.webocx.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;

public class PageDefinedBuilder {

    private final Integer companyId;
    private final Set<Integer> storeIds;
    private List<ColumMeta> metas;
    private List<OcxItem> ocxItems;
    private List<CdnItem> cdnItems;
    private List<Operate> operates;
    private List<Operate> buttons;
    private String active, title, subId, desc;
    private List<SubPageDefined> subPageDefineds;

    public PageDefinedBuilder(String subId, Integer companyId, Set<Integer> storeIds, String title, String desc) {
        this.companyId = companyId;
        this.storeIds = CollectionUtils.isEmpty(storeIds) ? null : Sets.newHashSet(storeIds);
        this.metas = Lists.newArrayList();
        this.title = title;
        this.subId = subId;
        this.desc = desc;
        this.ocxItems = Lists.newArrayList();
        this.cdnItems = Lists.newArrayList();
    }

    public void setActive(String active) {
        this.active = active;
    }

    public PageDefined building() {
        return new PageDefined(subId, companyId, storeIds, metas, ocxItems, cdnItems, active, title, desc, operates, buttons,
                subPageDefineds);
    }

    public void setSubPageDefineds(SubPageDefined subPageDefined) {
        if (this.subPageDefineds == null)
            this.subPageDefineds = Lists.newArrayList();
        this.subPageDefineds.add(subPageDefined);
    }

    public void setOperates(Operate operate) {
        if (operates == null) operates = Lists.newArrayList();
        operates.add(operate);
    }

    public void setButtons(Operate button) {
        if (buttons == null) buttons = Lists.newArrayList();
        buttons.add(button);
    }

    public void setCdnItems(CdnItem cdnItem) {
        this.cdnItems.add(cdnItem);
    }

    public void setMetas(ColumMeta meta) {
        this.metas.add(meta);
    }

    public void setOcxItems(OcxItem ocxItem) {
        this.ocxItems.add(ocxItem);
    }

    public static SubPageDefined buildSubPage(boolean paged, String id, String name, String stmtId, String url, String[] keys) {
        return new SubPageDefined(paged, id, name, stmtId, url, keys);
    }

}
