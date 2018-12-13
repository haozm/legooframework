package com.legooframework.model.dict.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LunarIndexSet {

    private final List<LunarIndexEntity> lunarIndexs;
    private final int min, max;
    private final static String yyyyMMdd = "yyyyMMdd";

    LunarIndexSet(List<LunarIndexEntity> lunarIndexs) {
        this.lunarIndexs = ImmutableList.copyOf(lunarIndexs);
        this.min = this.lunarIndexs.get(0).getId();
        this.max = this.lunarIndexs.get(this.lunarIndexs.size() - 1).getId();
    }

    public Optional<LunarIndexEntity> getByDate(Date date) {
        Preconditions.checkNotNull(date);
        int value = NumberUtils.createInteger(DateFormatUtils.format(date, yyyyMMdd));
        if (value < this.min || value > this.max) return Optional.empty();
        return lunarIndexs.stream().filter(x -> value == x.getId()).findFirst();
    }

    public Map<String, Object> getCustomeInfo(Date date) {
        Optional<LunarIndexEntity> lunarIndex = getByDate(date);
        Preconditions.checkArgument(lunarIndex.isPresent(), "非法的日期值 %s,无法获取对应的农历信息...", date);
        Map<String, Object> info = Maps.newHashMap();
        info.put("date", date);
        info.put("lunar", lunarIndex.get().getLunarVal());
        info.put("zodiac", lunarIndex.get().getZodiac()); // 生肖
        DateTime dateTime = new DateTime(date);
        int start = dateTime.plusYears(-1).year().get() * 10000;
        int end = dateTime.plusYears(20).year().get() * 10000;
        List<Integer> list = lunarIndexs.stream().filter(x -> x.getId() >= start && x.getId() <= end
                && x.getMmddOfLunar().equals(lunarIndex.get().getMmddOfLunar())).map(LunarIndexEntity::getId).collect(Collectors.toList());
        info.put("afters", Joiner.on(',').join(list));
        return info;
    }

    public Optional<LunarIndexEntity> getByLunar(String yyyyMMdd) {
        Preconditions.checkNotNull(yyyyMMdd);
        int value = NumberUtils.createInteger(yyyyMMdd);
        if (value < this.min || value > this.max) return Optional.empty();
        return lunarIndexs.stream().filter(x -> value == x.getLunarVal()).findFirst();
    }


    public Optional<List<LunarIndexEntity>> getByMMddLunar(String mmdd) {
        Preconditions.checkNotNull(yyyyMMdd);
        int cuur_year = NumberUtils.createInteger(DateTime.now().toString("yyyy0101"));
        List<LunarIndexEntity> list = this.lunarIndexs.stream().filter(x -> x.getId() >= cuur_year)
                .filter(x -> StringUtils.equals(x.getMmddOfLunar(), mmdd)).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(list) ? null : list);
    }
}
