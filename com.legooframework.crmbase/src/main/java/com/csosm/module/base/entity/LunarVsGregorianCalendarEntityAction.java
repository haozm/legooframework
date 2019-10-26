package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntityAction;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class LunarVsGregorianCalendarEntityAction extends BaseEntityAction<LunarVsGregorianCalendarEntity> {

    public LunarVsGregorianCalendarEntityAction() {
        super("LunarVsGregorianCalendarEntity", null);
    }

    public Optional<List<LunarVsGregorianCalendarEntity>> findByIds(String... ids) {
        if (ArrayUtils.isEmpty(ids)) return Optional.absent();
        Map<String, Object> params = Maps.newHashMap();
        params.put("ids", ids);
        List<LunarVsGregorianCalendarEntity> list = getJdbc().query(getExecSql("findByIds", params), params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

    @Override
    protected ResultSetExtractor<LunarVsGregorianCalendarEntity> getResultSetExtractor() {
        return null;
    }

    class RowMapperImpl implements RowMapper<LunarVsGregorianCalendarEntity> {
        @Override
        public LunarVsGregorianCalendarEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new LunarVsGregorianCalendarEntity(
                    resultSet.getString("id"),
                    resultSet.getString("lunarCalendar"),
                    resultSet.getString("lunarYearName"),
                    resultSet.getString("lunarZodiac"),
                    resultSet.getString("lunarMonthName"),
                    resultSet.getString("lunarDayName"),
                    (Integer) resultSet.getObject("weekDay"));
        }
    }
}
