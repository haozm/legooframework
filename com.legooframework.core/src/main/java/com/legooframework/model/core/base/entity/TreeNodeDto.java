package com.legooframework.model.core.base.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 通用 抽象建模
 */
public abstract class TreeNodeDto {
    private final Object id;
    private Object pid;
    private final String label;
    private Map<String, Object> attachData;
    private final List<TreeNodeDto> children;

    public Map<String, Object> toWithChildMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", this.id);
        map.put("value", MapUtils.getString(attachData, "rawId", this.id.toString()));
        map.put("pid", this.pid);
        map.put("label", this.label);
        map.put("attachData", this.attachData);
        if (null != this.attachData && MapUtils.getIntValue(this.attachData, "type") == 3) return map;
        List<Map<String, Object>> maps = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(children))
            for (TreeNodeDto $it : children) maps.add($it.toMap());
        map.put("children", maps);
        return map;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", this.id);
        map.put("value", MapUtils.getString(attachData, "rawId", this.id.toString()));
        map.put("pid", this.pid);
        map.put("label", this.label);
        map.put("attachData", this.attachData);
        if (null != this.attachData && MapUtils.getIntValue(this.attachData, "type") == 3) return map;
        if (!CollectionUtils.isEmpty(this.children)) {
            List<Map<String, Object>> maps = Lists.newArrayList();
            if (!CollectionUtils.isEmpty(children))
                for (TreeNodeDto $it : children) maps.add($it.toMap());
            map.put("children", maps);
        }
        return map;
    }

    public static <T extends TreeNodeDto> void buildTree(T root, List<T> allNodes) {
        LinkedList<TreeNodeDto> linkedList = Lists.newLinkedList();
        linkedList.addLast(root);
        TreeNodeDto parent;
        while (!linkedList.isEmpty()) {
            parent = linkedList.pop();
            for (TreeNodeDto $it : allNodes) {
                if (parent.isMyChild($it)) {
                    parent.addChild($it);
                    linkedList.addLast($it);
                }
            }
        }
    }

    protected void setAttachData(Map<String, Object> attachData) {
        this.attachData = attachData;
    }

    protected TreeNodeDto(Object id, Object pId, String lable) {
        this.id = id;
        this.pid = pId;
        this.label = lable;
        this.children = Lists.newArrayList();
    }

    public boolean isMyChild(TreeNodeDto child) {
        Preconditions.checkNotNull(child);
        return Objects.equal(this.id, child.pid);
    }

    public void addChild(TreeNodeDto child) {
        Preconditions.checkNotNull(child);
        if (child.pid == null) child.pid = this.id;
        Preconditions.checkState(Objects.equal(this.id, child.pid),
                "添加错误的节点%s 到上级节点%s", child, this);
        if (!children.contains(child)) children.add(child);
    }

    public void addChildNotCheck(TreeNodeDto child) {
        Preconditions.checkNotNull(child);
        if (child.pid == null) child.pid = this.id;
        if (!children.contains(child)) children.add(child);
    }

    public Object getId() {
        return id;
    }

    public Object getPid() {
        return pid;
    }

    public String getLabel() {
        return label;
    }

    public List<TreeNodeDto> getChildren() {
        return children;
    }

    public Map<String, Object> getAttachData() {
        return attachData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNodeDto that = (TreeNodeDto) o;
        return Objects.equal(id, that.id)
                && Objects.equal(pid, that.pid)
                && Objects.equal(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, pid, label);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("pid", pid)
                .add("label", label)
                .add("attachData", attachData)
                .toString();
    }
}
