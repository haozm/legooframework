package com.legooframework.model.core.web;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class TreeNode {

    private final Object id, pid;
    private final String lable;
    private String path;
    private final Map<String, Object> attachData;
    private final List<TreeNode> children;

    public TreeNode(Object id, Object pid, String lable, Map<String, Object> attachData) {
        this.id = id;
        this.pid = pid;
        this.lable = lable;
        this.attachData = attachData;
        this.children = Lists.newArrayList();
    }

    public Object getId() {
        return id;
    }

    public Object getPid() {
        return pid;
    }

    public String getLable() {
        return lable;
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
        return children;
    }

    void addChild(TreeNode node) {
        this.children.add(node);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreeNode)) return false;
        TreeNode treeNode = (TreeNode) o;
        return Objects.equal(id, treeNode.id) &&
                Objects.equal(pid, treeNode.pid) &&
                Objects.equal(lable, treeNode.lable) &&
                Objects.equal(attachData, treeNode.attachData) &&
                Objects.equal(children, treeNode.children);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, pid, lable, attachData, children);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("pid", pid)
                .add("lable", lable)
                .add("path", path)
                .add("attachData", attachData)
                .add("children.size", children.size())
                .toString();
    }
}
