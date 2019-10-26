package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

public class TemplateEntity extends UseRangeEntity {

    //  权益
    public static final String CLASSIFIES_RIGHTS_AND_INTERESTS = "100";
    private String classifies, title, content;
    private DateTime expireDate;
    private boolean defaulted;

    boolean isClassifies(String classifies) {
        return StringUtils.equals(this.classifies, classifies);
    }

    TemplateEntity(OrgEntity company, String classifies, String content) {
        super(company);
        setClassifies(classifies);
        this.expireDate = null;
        this.defaulted = false;
        this.title = "title";
        setContent(content);
    }

    TemplateEntity(StoEntity store, String classifies, String content) {
        super(store);
        this.expireDate = null;
        this.defaulted = false;
        setClassifies(classifies);
        this.title = "title";
        setContent(content);
    }

    private void setContent(String content) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(content), "模板内容不可为空....");
        this.content = content;
    }

    private void setClassifies(String classifies) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(classifies), "模板分类不可为空....");
        this.classifies = classifies;
    }

    public String replace(Map<String, Object> params) throws TemplateReplaceException {
        if (MapUtils.isEmpty(params)) return this.content;
        try {
            StringSubstitutor substitutor = new StringSubstitutor(params, "{", "}");
            return substitutor.replace(this.content);
        } catch (Exception e) {
            throw new TemplateReplaceException(String.format("模板 %s 替换发送异常...%s", this.content, params), e);
        }
    }

    public String getContent() {
        return content;
    }

    TemplateEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.classifies = res.getString("classifies");
            this.title = res.getString("temp_title");
            this.content = res.getString("temp_context");
            this.defaulted = ResultSetUtil.getBooleanByInt(res, "is_default");
            this.expireDate = ResultSetUtil.getDateTime(res, "expire_date");
        } catch (SQLException e) {
            throw new RuntimeException("Restore TemplateEntity has SQLException", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TemplateEntity that = (TemplateEntity) o;
        return defaulted == that.defaulted &&
                Objects.equals(classifies, that.classifies) &&
                Objects.equals(title, that.title) &&
                Objects.equals(content, that.content) &&
                Objects.equals(expireDate, that.expireDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), classifies, title, content, expireDate, defaulted);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("super", super.toString())
                .add("classifies", classifies)
                .add("title", title)
                .add("content", content)
                .add("expireDate", expireDate)
                .add("defaulted", defaulted)
                .toString();
    }
}
