package com.csosm.commons.jdbc.sqlcfg;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

public class ColumnMeta {

	private final String id, name, desc, type, showType, sublist;
	private boolean fixed;
	private final boolean freeze;
	private final boolean sort;
	private final boolean category;
	private final boolean legend;
	private final boolean sum;
	private final List<ColumnMeta> children;
	private final Map<String, String> addDatas;
	private List<For> fors;

	static class For {

		private final String type;

		private final boolean fixed;

		private For(String type, boolean fixed) {
			this.type = type;
			this.fixed = fixed;
		}

		public static For allFixed() {
			return new For("*", true);
		}

		public static For allUnfixed() {
			return new For("*", false);
		}

		public static List<For> parse(String forStr) {
			List<For> result = Lists.newArrayList();
			if (Strings.isNullOrEmpty(forStr))
				return result;
			for (String split : forStr.split(",")) {
				String[] items = split.split(":");
				result.add(new For(items[0], "0".equals(items[1]) ? false : true));
			}
			return result;
		}

		public String getType() {
			return type;
		}

		public boolean isFixed() {
			return fixed;
		}

	}

	public ColumnMeta(String id, String name, String desc, String type, boolean fixed, String showType, boolean sorted,
			boolean category, boolean legend, boolean freeze, boolean sum, String sublist, Map<String, String> addDatas,
			String forStr) {
		this.id = id;
		this.name = name;
		this.sum = sum;
		this.desc = desc;
		this.type = type;
		this.fixed = fixed;
		this.showType = showType;
		this.sort = sorted;
		this.category = category;
		this.freeze = freeze;
		this.legend = legend;
		this.sublist = sublist;
		this.children = Lists.newArrayList();
		this.addDatas = addDatas;
		if (this.fixed) {
			this.fors = Arrays.asList(For.allFixed());
		} else {
			if(!Strings.isNullOrEmpty(forStr)) {
				this.fors = For.parse(forStr);
			}else {
				this.fors = Arrays.asList(For.allUnfixed());
			}
		}
		
	}

	public String getSublist() {
		return sublist;
	}

	public boolean isFreeze() {
		return freeze;
	}

	public boolean isSum() {
		return sum;
	}

	public Map<String, String> getAddDatas() {
		return addDatas;
	}

	public boolean isPercentage() {
		return "percentage".equalsIgnoreCase(type);
	}

	public boolean isSort() {
		return sort;
	}

	public boolean isCategory() {
		return category;
	}

	public boolean isLegend() {
		return legend;
	}

	public boolean isFixed() {
		return fixed;
	}

	public Optional<String> getShowType() {
		return Optional.fromNullable(showType);
	}

	public boolean isMoney() {
		return "money".equalsIgnoreCase(type);
	}

	public boolean isInt() {
		return "int".equalsIgnoreCase(type);
	}

	public boolean isLong() {
		return "long".equalsIgnoreCase(type);
	}

	public List<ColumnMeta> getChildren() {
		return children;
	}

	public boolean isDouble() {
		return "float".equalsIgnoreCase(type) || "double".equalsIgnoreCase(type);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public void addColumnMeta(ColumnMeta columnMeta) {
		this.children.add(columnMeta);
	}

	public String getType() {
		return type;
	}

	public List<For> getFors() {
		return fors;
	}

	public For getFor(String type) {
		 for(For item: fors)
			 if("*".equals(item.type) || item.type.equals(type)) return item;
		 return null;
	}

	public ColumnMeta get(String type) {
		For item = getFor(type);
		if(null == item) return null;
		this.fixed = item.fixed;
		return this;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", id).add("name", name).add("desc", desc).add("type", type)
				.add("showType", showType).add("sublist", sublist).add("fixed", fixed).add("freeze", freeze)
				.add("sort", sort).add("category", category).add("legend", legend).add("sum", sum)
				.add("children", children).toString();
	}
}
