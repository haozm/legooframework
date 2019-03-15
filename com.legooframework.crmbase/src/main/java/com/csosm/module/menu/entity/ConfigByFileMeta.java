package com.csosm.module.menu.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

class ConfigByFileMeta {

    private final File file;
    private final List<Long> tenantIds;
    private final Map<Long, ResEntity> menuMap;
    private final ListMultimap<Long, ResEntity> pageList;

    ConfigByFileMeta(File file, Map<Long, ResEntity> menuMap, ListMultimap<Long, ResEntity> pageList) {
        Preconditions.checkNotNull(file, "配置文件不可以为空.");
        this.file = file;
        this.menuMap = Maps.newHashMap(menuMap);
        this.tenantIds = Lists.newArrayList(menuMap.keySet());
        this.pageList = ArrayListMultimap.create(pageList);
    }

    File getFile() {
        return file;
    }

    boolean exitsTenant(Long tenantId) {
        return tenantIds.contains(tenantId);
    }

    ResEntity getMenu(Long tenantId) {
        Preconditions.checkArgument(exitsTenant(tenantId));
        return this.menuMap.get(tenantId);
    }

    List<ResEntity> getPage(Long tenantId) {
        Preconditions.checkArgument(exitsTenant(tenantId));
        return this.pageList.get(tenantId);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigByFileMeta)) return false;
        ConfigByFileMeta that = (ConfigByFileMeta) o;
        return StringUtils.equals(file.getAbsolutePath(), that.file.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(file.getAbsolutePath());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("file", file.getAbsolutePath())
                .add("menuMap", menuMap == null ? "0" : menuMap.size())
                .add("pageList", pageList.size())
                .toString();
    }
}
