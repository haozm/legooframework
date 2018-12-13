package com.legooframework.model.commons.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// 树形结构数据模型
public class DefautTreeStructure implements TreeStructure {
    private Object id, pid, rawId;
    private String label;
    private Map<String, Object> attachData;
    private List<TreeStructure> children;
    private final List<Object> path;

    public DefautTreeStructure(Object id, Object pid, String label, Object rawId) {
        setId(id);
        setPid(pid);
        setLabel(label);
        setRawId(rawId);
        this.path = Lists.newArrayList();
        this.attachData = Maps.newHashMap();
        this.children = Lists.newArrayList();
    }

    public DefautTreeStructure(Object id, Object pid, String label) {
        this(id, pid, label, id);
    }

    void setId(Object id) {
        Preconditions.checkNotNull(id);
        this.id = id;
    }

    @Override
    public boolean hasChild() {
        return CollectionUtils.isNotEmpty(this.children);
    }

    @Override
    public Optional<List<TreeStructure>> getAllChildren() {
        if (hasChild()) {
            List<TreeStructure> sub_child = Lists.newArrayList();
            this.children.forEach(x -> {
                sub_child.add(x);
                x.getAllChildren().ifPresent(sub_child::addAll);
            });
            return Optional.of(sub_child);
        }
        return Optional.empty();
    }

    void setPid(Object pid) {
        Preconditions.checkNotNull(pid);
        this.pid = pid;
    }

    List<Object> getPath() {
        if (CollectionUtils.isEmpty(this.path)) this.path.add(this.id);
        return path;
    }

    void setPath(List<Object> paths) {
        this.path.clear();
        this.path.addAll(paths);
        this.path.add(this.id);
    }

    void setLabel(String label) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(label));
        this.label = label;
    }

    private void setRawId(Object rawId) {
        Preconditions.checkNotNull(rawId);
        this.rawId = rawId;
    }

    @Override
    public boolean isLogicRoot() {
        return "root".equals(this.pid);
    }

    @Override
    public void addChild(TreeStructure child) {
        Preconditions.checkNotNull(child);
        Preconditions.checkState(Objects.equal(getId(), child.getPid()),
                "添加错误的节点%s 到上级节点%s", child, this);
        if (!children.contains(child)) {
            children.add(child);
        }
    }

    @Override
    public Optional<TreeStructure> getChildById(Object id) {
        if (CollectionUtils.isNotEmpty(this.children)) {
            for (TreeStructure next : this.children) {
                if (Objects.equal(next.getId(), id)) return Optional.of(next);
                Optional<TreeStructure> _temp = next.getChildById(id);
                if (_temp.isPresent()) return _temp;
            }
        }
        return Optional.empty();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", this.id);
        map.put("rawId", this.rawId);
        map.put("pid", this.pid);
        map.put("label", this.label);
        map.put("attachData", this.attachData);
        if (!CollectionUtils.isEmpty(children)) {
            List<Map<String, Object>> maps = Lists.newArrayListWithCapacity(children.size());
            for (TreeStructure $it : children) maps.add($it.toMap());
            map.put("children", maps);
        }
        return map;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public Object getPid() {
        return pid;
    }

    public void setAttachData(String key, Object attachData) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "key 不可以为空值...");
        this.attachData.put(key, attachData);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Object getRawId() {
        return rawId;
    }

    public Map<String, Object> getAttachData() {
        return attachData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefautTreeStructure that = (DefautTreeStructure) o;
        return Objects.equal(id, that.id) &&
                Objects.equal(pid, that.pid) &&
                Objects.equal(rawId, that.rawId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, pid, rawId);
    }

    @Override
    public List<TreeStructure> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("pid", pid)
                .add("label", label)
                .add("rawId", rawId)
                .add("attachData", attachData.size())
                .add("children", children.size())
                .toString();
    }
}
