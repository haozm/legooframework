package com.legooframework.model.picturemgn.entity;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class AbstractLabel{
	
	//标签唯一标识
	private final Object id;
	//标签父节点ID
	private Object pid;
	//标签
	private final String label;
	//标签描述
	private String desc;
	//存储标签树数据的MAP
	private final Map<String,Object> treeMap = Maps.newConcurrentMap();
	//标签子节点，也是标签
	private final List<AbstractLabel> children = Lists.newLinkedList();
	
	//创建子节点
	protected AbstractLabel(Object id, Object pid, String label) {
		super();
		this.id = id;
		this.pid = pid;
		this.label = label;
	}
	
	//创建根节点
	protected AbstractLabel(Object id,String label) {
		super();
		this.id = id;
		this.pid = id;
		this.label = label;
	}
	
	/**
	 * 判断标签是否是当前标签的子标签
	 * @param label 标签
	 * @return  是否使当前标签的子节点
	 */
	public boolean isChild(AbstractLabel label) {
		if(null == label) return false;
		return this.id.equals(label.pid);
	}
	
	/**
	 * 判断入参label是否是当前标签的父标签
	 * @param label
	 * @return
	 */
	public boolean isParent(AbstractLabel label) {
		if(null == label) return false;
		return label.id.equals(this.pid);
	}
	/**
	 * 判断当前标签是否是子标签
	 * @return
	 */
	public boolean isChildLabel() {
		return !this.id.equals(this.pid);
	}
	/**
	 * 判断当前标签是否是根标签
	 * @return
	 */
	public boolean isRootLabel() {
		return this.id.equals(this.pid);
	}
	/**
	 * 判断当前标签是否使叶子标签，即级别最低的标签
	 * @return
	 */
	public boolean isLeafLabel() {
		return this.children.isEmpty();
	}
	
	/**
	 * 当前标签添加子标签
	 * @param label
	 */
	public void addChild(AbstractLabel label) {
		if(null == label) throw new IllegalArgumentException(String.format("当前标签[%s]无法添加空标签", this.id));
		if(this.children.contains(label)) return;
		label.pid = this.id;
		this.children.add(label);
	}
	
	/**
	 * 创建以root为根标签，labels 为子标签的标签树
	 * @param root 根标签
	 * @param labels 子标签
	 */
	public static <T extends AbstractLabel> void buildTree(T root,List<T> labels) {
		LinkedList<AbstractLabel> linkedList = Lists.newLinkedList();
        linkedList.addLast(root);
        AbstractLabel parent;
        while (!linkedList.isEmpty()) {
            parent = linkedList.pop();
            for (AbstractLabel label : labels) {
                if (parent.isChild(label)) {
                    parent.addChild(label);
                    linkedList.addLast(label);
                }
            }
        }
	}
	
	
}
