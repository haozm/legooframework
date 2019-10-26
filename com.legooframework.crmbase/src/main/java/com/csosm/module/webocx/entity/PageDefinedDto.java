package com.csosm.module.webocx.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class PageDefinedDto {

    private final String ocxId, model, stmtId, title, fullName, desc;
    private final int index;
    private final boolean showCount;
    private final PageDefined pageDefined;

    PageDefinedDto(String ocxId, String model, String stmtId, int index, String title, boolean showCount,
                   String desc, PageDefined pageDefined) {
        this.ocxId = ocxId;
        this.model = model;
        this.desc = desc;
        this.stmtId = stmtId;
        this.showCount = showCount;
        this.title = pageDefined.getTitle().isPresent() ? pageDefined.getTitle().get() : title;
        this.index = index;
        this.fullName = String.format("%s.%s", ocxId, pageDefined.getSubId());
        this.pageDefined = pageDefined;
    }

    public Optional<Map<String, Object>> getQueryParams() {
        Optional<List<CdnItem>> cdns = pageDefined.getCdnItems();
        if (!cdns.isPresent()) return Optional.absent();
        Map<String, Object> params = Maps.newHashMap();
        for (CdnItem $it : cdns.get()) {
            params.put($it.getName(), $it.getValue());
        }
        return Optional.of(params);
    }


    public String getTitle() {
        return title;
    }

    public boolean isShowCount() {
        return showCount;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("fullName", fullName);
        param.put("title", title);
        param.put("desc", pageDefined.getDesc().isPresent() ? pageDefined.getDesc().get() :
                Strings.isNullOrEmpty(desc) ? desc : title);
        return param;
    }

    public String getFullName() {
        return fullName;
    }

    public int getIndex() {
        return index;
    }

    public String getSqlModel() {
        return model;
    }

    public String getSqlStmtId() {
        return stmtId;
    }

    public PageDefined getPageDefined() {
        return pageDefined;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fullName", fullName)
                .add("ocxId", ocxId)
                .add("model", model)
                .add("stmtId", stmtId)
                .add("index", index)
                .add("title", title)
                .toString();
    }
}
