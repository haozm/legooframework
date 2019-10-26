package com.csosm.module.webocx.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

public class OcxItem {

    private final String type, field, name, placeholder, defvalue, dataType;
    private final DataSource dataSource;
    private final boolean required, isAll;
    private final List<Map<String, String>> lables;
    private final OcxItem ocxItem;
    private String width;
    private String display;
    private String position;
    OcxItem(String type, String field, String name, String placeholder, boolean required, String defvalue,
            String dataType, DataSource dataSource, boolean isAll, OcxItem ocxItem,String width,String display,
            String position) {
        this.type = type;
        this.field = field;
        this.placeholder = placeholder;
        this.name = name;
        this.required = required;
        this.dataSource = dataSource;
        this.defvalue = defvalue;
        this.dataType = dataType;
        this.isAll = isAll;
        this.lables = null;
        this.ocxItem = ocxItem;
        this.width = width;
        this.display = display;
        this.position = position;
    }
    
    OcxItem(String type, String field, String name, String placeholder, boolean required, String defvalue,
            String dataType, DataSource dataSource, List<Map<String, String>> lables) {
        this.type = type;
        this.field = field;
        this.placeholder = placeholder;
        this.name = name;
        this.required = required;
        this.dataSource = dataSource;
        this.defvalue = defvalue;
        this.dataType = dataType;
        this.isAll = true;
        this.ocxItem = null;
        this.lables = Lists.newArrayList(lables);
    }

    public Optional<String> getDefvalue() {
        return Optional.fromNullable(defvalue);
    }

    public String getType() {
        return type;
    }

    public String getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    public Optional<OcxItem> getOcxItem() {
        return Optional.fromNullable(ocxItem);
    }

    public List<Map<String, String>> getLables() {
        return lables;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("name", name);
        map.put("fieldName", field);
        map.put("type", type);
        map.put("dataType", dataType);
        map.put("defvalue", defvalue);
        if(!Strings.isNullOrEmpty(this.display)) map.put("display", this.display);
        if(!Strings.isNullOrEmpty(this.position)) map.put("position", this.position);
        if (getOcxItem().isPresent()) map.put("subItem", ocxItem.toViewMap());
        if (!Strings.isNullOrEmpty(placeholder)
        	||!Strings.isNullOrEmpty(this.width)) {
            Map<String, Object> placeholder_map = Maps.newHashMap();
            if(!Strings.isNullOrEmpty(this.width)) placeholder_map.put("width", this.width);
            if(!Strings.isNullOrEmpty(placeholder)) placeholder_map.put("placeholder", placeholder);
            map.put("data", placeholder_map);
        }
        map.put("required", required);
        return map;
    }

    public boolean isAll() {
        return isAll;
    }

    public Optional<String> getPlaceholder() {
        return Optional.fromNullable(placeholder);
    }

    public Optional<DataSource> getDataSource() {
        return Optional.fromNullable(dataSource);
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OcxItem ocxItem = (OcxItem) o;
        return required == ocxItem.required &&
                Objects.equal(type, ocxItem.type) &&
                Objects.equal(field, ocxItem.field) &&
                Objects.equal(name, ocxItem.name) &&
                Objects.equal(placeholder, ocxItem.placeholder) &&
                Objects.equal(defvalue, ocxItem.defvalue) &&
                Objects.equal(dataType, ocxItem.dataType) &&
                Objects.equal(dataSource, ocxItem.dataSource);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, field, name, placeholder, defvalue, dataType, dataSource, required);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("field", field)
                .add("name", name)
                .add("required", required)
                .add("defvalue", defvalue)
                .add("isAll", isAll)
                .add("dataSource", dataSource)
                .toString();
    }
}
