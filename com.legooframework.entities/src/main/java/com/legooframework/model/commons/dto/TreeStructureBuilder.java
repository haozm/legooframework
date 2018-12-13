package com.legooframework.model.commons.dto;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class TreeStructureBuilder {

    public TreeStructure createLogicRoot(String id, String label) {
        return new DefautTreeStructure(id, "root", Strings.isNullOrEmpty(label) ? "根目录" : label);
    }

    public static <T extends TreeStructure> void buildTree(T root, List<T> allNodes) {
        Preconditions.checkNotNull(root);
        if (CollectionUtils.isEmpty(allNodes)) return;
        LinkedList<TreeStructure> linkedList = Lists.newLinkedList();
        linkedList.addLast(root);
        TreeStructure pop;
        while (!linkedList.isEmpty()) {
            pop = linkedList.pop();
            for (TreeStructure $it : allNodes) {
                if (Objects.equal(pop.getId(), $it.getPid())) {
                    pop.addChild($it);
                    linkedList.addLast($it);
                }
            }
        }
    }

    public static <T extends TreeStructure> Optional<T> buildTree(String rootId, List<T> allNodes) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(rootId));
        if (CollectionUtils.isEmpty(allNodes)) return Optional.empty();
        Optional<T> root_opt = allNodes.stream().filter(x -> Objects.equal(rootId, x.getId())).findFirst();
        if (!root_opt.isPresent()) return Optional.empty();
        buildTree(root_opt.get(), allNodes);
        return root_opt;
    }

    public static <T extends TreeStructure> Optional<T> buildTree(List<T> allNodes) {
        if (CollectionUtils.isEmpty(allNodes)) return Optional.empty();
        Optional<T> root_opt = allNodes.stream().filter(TreeStructure::isLogicRoot).findFirst();
        if (!root_opt.isPresent()) return Optional.empty();
        buildTree(root_opt.get(), allNodes);
        return root_opt;
    }
}
