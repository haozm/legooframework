package com.legooframework.model.core.web;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class TreeUtil {

    public static <T extends TreeNode> T buildTree(T root, List<T> nodes) {
        LinkedList<TreeNode> linkedList = Lists.newLinkedList();
        linkedList.addLast(root);
        root.setPath(root.getId().toString());
        TreeNode parent;
        while (!linkedList.isEmpty()) {
            parent = linkedList.pop();
            for (TreeNode $it : nodes) {
                if (Objects.equals(parent.getId(), $it.getPid())) {
                    parent.addChild($it);
                    $it.setPath(String.format("%s-%s", parent.getPath(), $it.getId()));
                    linkedList.addLast($it);
                }
            }
        }
        return root;
    }

    public static <T extends TreeNode> T buildTree(Object rootId, List<T> nodes) {
        Optional<T> root = nodes.stream().filter(x -> x.getId().equals(rootId)).findFirst();
        Preconditions.checkState(root.isPresent(), "id=%s 对应的根节点不存在...");
        return buildTree(root.get(), nodes);
    }
}
