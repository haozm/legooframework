package com.csosm.module.webocx.entity;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class WebOcxBuilder {

    private String webocxId, url, stmtId, title, group, desc;
    private boolean paged, showCount, reject;
    private List<PageDefined> pageDefineds;
    private int index;
    private String width;
    private String display;
    private String position;
    
    private WebOcxBuilder(String webocxId, String url, String stmtId, boolean paged, boolean showCount, String title,
                          int index, String group, boolean reject, String desc) {
        this.webocxId = webocxId;
        this.url = url;
        this.paged = paged;
        this.stmtId = stmtId;
        this.showCount = showCount;
        this.group = group;
        this.reject = reject;
        this.desc = desc;
        this.title = title;
        this.index = index;
        this.pageDefineds = Lists.newArrayList();
    }

    public void setGroup(PageDefined pageDefined) {
        this.pageDefineds.add(pageDefined);
    }

    public static WebOcxBuilder createWebocxBuilder(String webocxId, String url, String stmtId, boolean paged,
                                                    boolean showCount, String title, int index, String group,
                                                    boolean reject, String desc) {
        return new WebOcxBuilder(webocxId, url, stmtId, paged, showCount, title, index, group, reject, desc);
    }

    public WebOcx buildWebOcx() {
        return new WebOcx(webocxId, stmtId, url, paged, showCount, title, index, group, reject, desc, pageDefineds);
    }
    // -------------------------

    private String type, field, name, placeholder;
    private boolean required;
    private String defvalue;
    private String dataType;
    private String dsType;
    private String dsCtx;
    private boolean isAll;
    private List<String> datas;
    private List<Map<String, String>> lables;
    private OcxItem ocxSubItem;

    private WebOcxBuilder(String type, String field, String placeholder, String defvalue, boolean required,
                          String dataType, List<Map<String, String>> maps) {
        this.type = type;
        this.field = field;
        this.name = "";
        this.placeholder = placeholder;
        this.defvalue = defvalue;
        this.required = required;
        this.dataType = dataType;
        this.isAll = false;
        this.datas = Lists.newArrayList();
        this.lables = Lists.newArrayList(maps);
    }

    public OcxItem buildOcxSubItem() {
        DataSource ds = null;
        if (dsType != null)
            ds = new DataSource(dsType, dsCtx, datas);
        return new OcxItem(type, field, name, placeholder, required, defvalue, dataType, ds, lables);
    }

    private WebOcxBuilder(String type, String field, String name, String placeholder, String defvalue, boolean required,
                          String dataType, boolean isAll,String width,String display,String position) {
        this.type = type;
        this.field = field;
        this.name = name;
        this.placeholder = placeholder;
        this.defvalue = defvalue;
        this.required = required;
        this.dataType = dataType;
        this.isAll = isAll;
        this.datas = Lists.newArrayList();
        this.lables = Lists.newArrayList();
        this.width = width;
        this.display = display;
        this.position = position;
    }

    public void setDatas(String label, String value, String checked,String unit) {
        String res = String.format("%s:%s:%s:%s", label, value, checked == null ? "null" : checked,null == unit?"null":unit);
        this.datas.add(res);
    }

    public void setOcxSubItem(OcxItem ocxItem) {
        this.ocxSubItem = ocxItem;
    }

    public void setDataSource(String dsType, String context) {
        this.dsType = dsType;
        this.dsCtx = context;
    }

    public static WebOcxBuilder createOcxItemBuilder(String type, String field, String name, String placeholder,
                                                     String defvalue, boolean required, String dataType, boolean isAll,String width,
                                                     String display,String position) {
        return new WebOcxBuilder(type, field, name, placeholder, defvalue, required, dataType, isAll,width,display,position);
    }

    public static WebOcxBuilder createOcxSubItemBuilder(String type, String field, String placeholder,
                                                        String defvalue, boolean required, String dataType,
                                                        List<Map<String, String>> maps) {
        return new WebOcxBuilder(type, field, placeholder, defvalue, required, dataType, maps);
    }


    public OcxItem buildOcxItem() {
        DataSource ds = null;
        if (dsType != null) ds = new DataSource(dsType, dsCtx, datas);
        return new OcxItem(type, field, name, placeholder, required, defvalue, dataType, ds, isAll, ocxSubItem,width,display,position);
    }

    public static ColumMeta buildMeta(String id, String name, String type, boolean fixed) {
        return new ColumMeta(id, name, type, fixed);
    }

    public static Operate buildOperate(String name, String title, String type, String url, String keys,String roles) {
    	Operate operate = new Operate(name, title, type, url, 
        		Strings.isNullOrEmpty(keys) ? null : StringUtils.split(keys, ','));
    	if(!Strings.isNullOrEmpty(roles))operate.addRoles(StringUtils.split(roles, ','));
    	return operate;
    }

    private WebOcxBuilder(String name, String defvalue) {
        this.name = name;
        this.defvalue = defvalue;
    }

    public static WebOcxBuilder createCdnItem(String name, String defvalue) {
        return new WebOcxBuilder(name, defvalue);
    }

    public CdnItem buildCdnItem() {
        return new CdnItem(name, defvalue);
    }

}
