package com.legooframework.model.core.web;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

public class TreeNode {

    private final Object id, pid;
    private String label;
    private String path;
    private final Map<String, Object> attachData;
    private final List<TreeNode> children;

    public TreeNode(Object id, Object pid, String lable, Map<String, Object> attachData) {
        this.id = id;
        this.pid = pid;
        this.label = lable;
        this.attachData = attachData;
        this.children = Lists.newArrayList();
    }

    public Object getId() {
        return id;
    }

    public Object getPid() {
        return pid;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    public Map<String, Object> getAttachData() {
        return attachData;
    }

    public List<TreeNode> getChildren() {
        return CollectionUtils.isEmpty(children) ? null : children;
    }

    void addChild(TreeNode node) {
        this.children.add(node);
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        params.put("pid", pid);
        params.put("label", label);
        params.put("attachData", attachData);
        if (!CollectionUtils.isEmpty(this.children)) {
            List<Map<String, Object>> list = Lists.newArrayList();
            this.children.forEach(x -> list.add(x.toViewMap()));
            params.put("children", list);
        }
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreeNode)) return false;
        TreeNode treeNode = (TreeNode) o;
        return Objects.equal(id, treeNode.id) &&
                Objects.equal(pid, treeNode.pid) &&
                Objects.equal(label, treeNode.label) &&
                Objects.equal(attachData, treeNode.attachData) &&
                Objects.equal(children, treeNode.children);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, pid, label, attachData, children);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("pid", pid)
                .add("lable", label)
                .add("path", path)
                .add("attachData", attachData)
                .add("children.size", children.size())
                .toString();
    }
}
