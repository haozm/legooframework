package com.legooframework.model.jobs.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public class JobTrackListEnity extends BaseEntity<Long> {

    private final String code, name;
    private final Date start, end, timetamp;
    private boolean success;
    private final Integer recnums;

    JobTrackListEnity(String code, String name, Date start, Date end, Date timetamp, boolean success,
                      Integer recnums) {
        super(-1L);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(code), "任务编码不可以为空值...");
        this.code = code;
        this.name = name;
        this.start = start == null ? new Date() : start;
        this.end = end == null ? start : end;
        Preconditions.checkNotNull(timetamp, "记录最后一次时间戳不可以为空...");
        this.timetamp = timetamp;
        this.success = success;
        this.recnums = recnums;
    }

    public JobTrackListEnity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.code = ResultSetUtil.getString(res, "code");
            this.name = ResultSetUtil.getOptString(res, "name", null);
            this.start = res.getDate("start");
            this.end = res.getDate("end");
            this.timetamp = res.getDate("recTime");
            this.recnums = res.getInt("recordNum");
        } catch (SQLException e) {
            throw new RuntimeException("Restore JobTrackListEnity has SQLException", e);
        }
    }

    public String getCode() {
        return code;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> data = super.toParamMap("success");
        data.put("success", success ? 1 : 0);
        return data;
    }

    public String getName() {
        return name;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public Integer getRecnums() {
        return recnums;
    }

    public Date getTimetamp() {
        return timetamp;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("code", code)
                .add("name", name)
                .add("start", start)
                .add("end", end)
                .add("timetamp", timetamp)
                .add("success", success)
                .add("recnums", recnums)
                .toString();
    }
}
