package com.csosm.module.webocx.entity;

import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WebOcx {
    private final String id, url, title, group, desc;
    private final String[] fullName;
    private final boolean paged, showCount, reject;
    private final List<PageDefined> pageDefineds;
    private final int index;

    WebOcx(String id, String fullName, String url, boolean paged, boolean showCount, String title, int index,
           String group, boolean reject, String desc, List<PageDefined> pageDefineds) {
        this.id = id;
        this.fullName = Strings.isNullOrEmpty(fullName) ? null : StringUtils.split(fullName, '.');
        this.pageDefineds = pageDefineds;
        this.url = url;
        this.index = index;
        this.title = title;
        this.reject = reject;
        this.showCount = showCount;
        this.group = group;
        this.desc = desc;
        this.paged = paged;
    }

    private boolean isReject() {
        return reject;
    }

    public String getSqlModel() {
        Preconditions.checkNotNull(fullName);
        Preconditions.checkPositionIndex(1, fullName.length);
        return fullName[0];
    }

    public String getSqlStmtId() {
        Preconditions.checkNotNull(fullName);
        Preconditions.checkPositionIndex(1, fullName.length);
        return fullName[1];
    }

    public Optional<String> getDesc() {
        return Optional.fromNullable(desc);
    }

    public String getGroup() {
        return group;
    }

    public String getId() {
        return id;
    }

    public Optional<String[]> getFullName() {
        return Optional.fromNullable(fullName);
    }

    public int getIndex() {
        return index;
    }

    Optional<PageDefinedDto> findPageById(String subId) {
        if (Strings.isNullOrEmpty(subId))
            return Optional.of(new PageDefinedDto(id, getSqlModel(), getSqlStmtId(), index, title, showCount,
                    desc, pageDefineds.get(0)));
        PageDefinedDto pg = null;
        for (PageDefined $it : pageDefineds) {
            if (StringUtils.equals($it.getSubId(), subId)) {
                pg = new PageDefinedDto(id, getSqlModel(), getSqlStmtId(), index, title, showCount, desc, $it);
                break;
            }
        }
        return Optional.fromNullable(pg);
    }

    public Optional<List<PageDefinedDto>> loadCompanyPages(OrganizationEntity company) {
        if (isReject()) {
            Optional<List<PageDefinedDto>> _pages = loadComPagesDto(company, true);
            if (_pages.isPresent()) return _pages;
            return loadDefPages(true);
        } else {
            List<PageDefinedDto> res = Lists.newArrayList();
            Optional<List<PageDefinedDto>> _pages = loadComPagesDto(company, false);
            if (_pages.isPresent()) res.addAll(_pages.get());
            _pages = loadDefPages(false);
            if (_pages.isPresent()) res.addAll(_pages.get());
            return Optional.fromNullable(CollectionUtils.isEmpty(res) ? null : res);
        }
    }

    /**
     * 输出属于该门店的定义的可支配的分组信息
     *
     * @param company
     * @param store
     * @return
     */
    public Optional<List<PageDefinedDto>> loadStorePages(OrganizationEntity company, StoreEntity store) {
        Preconditions.checkNotNull(company, "参数 OrganizationEntity company 不可以为空值...");
        Preconditions.checkNotNull(store, "参数 StoreEntity store 不可以为空值...");
        if (isReject()) {
            Optional<List<PageDefinedDto>> _pages = loadStorePages(store, true);
            if (_pages.isPresent()) return _pages;
            _pages = loadComPagesDto(company, true);
            if (_pages.isPresent()) return _pages;
            return loadDefPages(true);
        } else {
            List<PageDefinedDto> res = Lists.newArrayList();
            Optional<List<PageDefinedDto>> _pages = loadStorePages(store, false);
            if (_pages.isPresent()) res.addAll(_pages.get());
            _pages = loadComPagesDto(company, false);
            if (_pages.isPresent()) res.addAll(_pages.get());
            _pages = loadDefPages(false);
            if (_pages.isPresent()) res.addAll(_pages.get());
            return Optional.fromNullable(CollectionUtils.isEmpty(res) ? null : res);
        }
    }

    private Optional<List<PageDefinedDto>> loadComPagesDto(OrganizationEntity company, boolean reject) {
        List<PageDefined> _pages = Lists.newArrayList();
        for (PageDefined page : this.pageDefineds) {
            if (page.isCom(company)) {
                _pages.add(page);
                if (reject) break;
            }
        }
        if (CollectionUtils.isEmpty(_pages)) return Optional.absent();
        List<PageDefinedDto> list = Lists.newArrayList();
        for (PageDefined page : _pages) {
            list.add(new PageDefinedDto(id, getSqlModel(), getSqlStmtId(), index, title, showCount, desc, page));
        }
        return Optional.of(list);
    }

    private Optional<List<PageDefinedDto>> loadStorePages(StoreEntity store, boolean reject) {
        List<PageDefined> _pages = Lists.newArrayList();
        for (PageDefined page : this.pageDefineds) {
            if (page.isStore(store)) {
                _pages.add(page);
                if (reject) break;
            }
        }
        if (CollectionUtils.isEmpty(_pages)) return Optional.absent();
        List<PageDefinedDto> list = Lists.newArrayList();
        for (PageDefined page : _pages) {
            list.add(new PageDefinedDto(id, getSqlModel(), getSqlStmtId(), index, title, showCount, desc, page));
        }
        return Optional.of(list);
    }

    private Optional<List<PageDefinedDto>> loadDefPages(boolean reject) {
        List<PageDefined> _pages = Lists.newArrayListWithCapacity(10);
        for (PageDefined $it : this.pageDefineds) {
            if (Objects.equal($it.getCompanyId(), -1)) {
                _pages.add($it);
                if (reject) break;
            }
        }
        if (CollectionUtils.isEmpty(_pages)) return Optional.absent();
        List<PageDefinedDto> list = Lists.newArrayList();
        for (PageDefined page : _pages) {
            list.add(new PageDefinedDto(id, getSqlModel(), getSqlStmtId(), index, title, showCount, desc, page));
        }
        return Optional.of(list);
    }

    public Optional<String> getTitle() {
        return Optional.fromNullable(title);
    }

    public boolean isShowCount() {
        return showCount;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> model = Maps.newHashMap();
        model.put("url", url);
        model.put("page", paged);
        model.put("view", "grid");
        model.put("showCount", showCount);
        model.put("meta", new String[0]);
        return model;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebOcx webOcx = (WebOcx) o;
        return paged == webOcx.paged &&
                reject == webOcx.reject &&
                Objects.equal(id, webOcx.id) &&
                Arrays.equals(fullName, webOcx.fullName) &&
                Objects.equal(showCount, webOcx.showCount) &&
                Objects.equal(url, webOcx.url);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, fullName, showCount, reject, url, paged);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("fullName", Arrays.toString(fullName))
                .add("url", url)
                .add("paged", paged)
                .add("showCount", showCount)
                .add("reject", reject)
                .add("pageDefineds", pageDefineds.size())
                .toString();
    }
}
