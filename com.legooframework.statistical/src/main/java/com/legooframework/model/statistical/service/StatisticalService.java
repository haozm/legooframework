package com.legooframework.model.statistical.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.legooframework.model.core.jdbc.AsyncResult;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.statistical.entity.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class StatisticalService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticalService.class);

    public Map<String, Object> loadHomePage(UserAuthorEntity user, String layoutType) {
        Optional<StatisticalLayoutEntity> layout = getBean(StatisticalLayoutEntityAction.class)
                .loadPageByUser(user, layoutType);
        Preconditions.checkState(layout.isPresent(), "不存在 %s 对应的报表定义...", user.getCompanyId());
        return layout.get().buildLayout(user, getBean(StatisticalDefinedFactory.class));
    }

    public Map<String, Object> loadSubPage(UserAuthorEntity user, String layoutType, String rid) {
        Optional<StatisticalLayoutEntity> layout = getBean(StatisticalLayoutEntityAction.class)
                .loadSubPageByUser(user, layoutType, rid);
        Preconditions.checkState(layout.isPresent(), "缺少Id=%s 对应的SUBPAGE定义...", rid);
        return layout.get().buildLayout(user, getBean(StatisticalDefinedFactory.class));
    }

    private Map<String, Object> parseDataRange(DateRange dateRange, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("dataRange", dateRange.toString());
        LocalDate thisDateMin = startDate, thisDateMax = endDate;
        LocalDate hbDateMin = startDate, hbDateMax = endDate;
        LocalDate tbDateMin = startDate, tbDateMax = endDate;
        switch (dateRange) {
            case CUSTOMIZE:
                thisDateMin = startDate;
                thisDateMax = endDate;
                int days = Days.daysBetween(startDate, endDate).getDays();
                hbDateMin = thisDateMin.plusDays(-(days + 1));
                hbDateMax = thisDateMax.plusDays(-(days + 1));
                tbDateMin = thisDateMin.plusYears(-1);
                tbDateMax = thisDateMax.plusYears(-1);
                break;
            case JIDU:
                startDate = LocalDate.now();
                if (startDate.getMonthOfYear() <= 3) {
                    thisDateMin = new LocalDate(startDate.getYear(), 1, 1);
                    thisDateMax = new LocalDate(startDate.getYear(), 3, 31);
                } else if (startDate.getMonthOfYear() <= 6) {
                    thisDateMin = new LocalDate(startDate.getYear(), 4, 1);
                    thisDateMax = new LocalDate(startDate.getYear(), 6, 30);
                } else if (startDate.getMonthOfYear() <= 9) {
                    thisDateMin = new LocalDate(startDate.getYear(), 7, 1);
                    thisDateMax = new LocalDate(startDate.getYear(), 9, 30);
                } else {
                    thisDateMin = new LocalDate(startDate.getYear(), 10, 1);
                    thisDateMax = new LocalDate(startDate.getYear(), 12, 31);
                }
                hbDateMin = thisDateMin.plusMonths(-3);
                hbDateMax = thisDateMax.plusMonths(-3);
                tbDateMin = thisDateMin.plusYears(-1);
                tbDateMax = thisDateMax.plusYears(-1);
                break;
            case TODAY:
                startDate = LocalDate.now();
                thisDateMin = startDate;
                thisDateMax = startDate;
                hbDateMin = startDate.plusDays(-1);
                hbDateMax = startDate.plusDays(-1);
                tbDateMin = startDate.plusYears(-1);
                tbDateMax = startDate.plusYears(-1);
                break;
            case WEEK:
                startDate = LocalDate.now();
                thisDateMin = startDate.dayOfWeek().withMinimumValue();
                thisDateMax = startDate.dayOfWeek().withMaximumValue();
                hbDateMin = startDate.plusWeeks(-1).dayOfWeek().withMinimumValue();
                hbDateMax = startDate.plusWeeks(-1).dayOfWeek().withMaximumValue();
                tbDateMin = startDate.plusYears(-1).dayOfWeek().withMinimumValue();
                tbDateMax = startDate.plusYears(-1).dayOfWeek().withMaximumValue();
                break;
            case BEFWEEK:
                startDate = LocalDate.now().plusWeeks(-1);
                thisDateMin = startDate.dayOfWeek().withMinimumValue();
                thisDateMax = startDate.dayOfWeek().withMaximumValue();
                hbDateMin = startDate.plusWeeks(-1).dayOfWeek().withMinimumValue();
                hbDateMax = startDate.plusWeeks(-1).dayOfWeek().withMaximumValue();
                tbDateMin = startDate.plusYears(-1).dayOfWeek().withMinimumValue();
                tbDateMax = startDate.plusYears(-1).dayOfWeek().withMaximumValue();
                break;
            case YEAR:
                startDate = LocalDate.now();
                thisDateMin = startDate.dayOfYear().withMinimumValue();
                thisDateMax = startDate.dayOfYear().withMaximumValue();
                hbDateMin = startDate.plusYears(-1).dayOfYear().withMinimumValue();
                hbDateMax = startDate.plusYears(-1).dayOfYear().withMaximumValue();
                tbDateMin = startDate.plusYears(-1).dayOfYear().withMinimumValue();
                tbDateMax = startDate.plusYears(-1).dayOfYear().withMaximumValue();
                break;
            case MONTH:
                startDate = LocalDate.now();
                thisDateMin = startDate.dayOfMonth().withMinimumValue();
                thisDateMax = startDate.dayOfMonth().withMaximumValue();
                hbDateMin = startDate.plusMonths(-1).dayOfMonth().withMinimumValue();
                hbDateMax = startDate.plusMonths(-1).dayOfMonth().withMaximumValue();
                tbDateMin = startDate.plusYears(-1).dayOfMonth().withMinimumValue();
                tbDateMax = startDate.plusYears(-1).dayOfMonth().withMaximumValue();
                break;
            case BEFMONTH:
                startDate = LocalDate.now().plusMonths(-1);
                thisDateMin = startDate.dayOfMonth().withMinimumValue();
                thisDateMax = startDate.dayOfMonth().withMaximumValue();
                hbDateMin = startDate.plusMonths(-1).dayOfMonth().withMinimumValue();
                hbDateMax = startDate.plusMonths(-1).dayOfMonth().withMaximumValue();
                tbDateMin = startDate.plusYears(-1).dayOfMonth().withMinimumValue();
                tbDateMax = startDate.plusYears(-1).dayOfMonth().withMaximumValue();
                break;
            case HALFYEAR:
                startDate = LocalDate.now();
                int _temp_month = startDate.getMonthOfYear();
                if (_temp_month <= 6) {
                    thisDateMin = new LocalDate(startDate.getYear(), 1, 1);
                    thisDateMax = new LocalDate(startDate.getYear(), 6, 30);
                } else {
                    thisDateMin = new LocalDate(startDate.getYear(), 7, 1);
                    thisDateMax = new LocalDate(startDate.getYear(), 12, 31);
                }
                hbDateMin = thisDateMin.plusMonths(-6);
                hbDateMax = thisDateMax.plusMonths(-6);
                tbDateMin = thisDateMin.plusYears(-1);
                tbDateMax = thisDateMin.plusYears(-1);
                break;
            default:
                throw new IllegalArgumentException(String.format("不支持 %s 的报表范围统计...", dateRange.toString()));
        }
        params.put("thisDate", new String[]{thisDateMin.toString("yyyy-MM-dd 00:00:00"), thisDateMax.toString("yyyy-MM-dd 23:59:59")});
        params.put("huanbiDate", new String[]{hbDateMin.toString("yyyy-MM-dd 00:00:00"), hbDateMax.toString("yyyy-MM-dd 23:59:59")});
        params.put("tongbiDate", new String[]{tbDateMin.toString("yyyy-MM-dd 00:00:00"), tbDateMax.toString("yyyy-MM-dd 23:59:59")});
        return params;
    }

    public Optional<List<Map<String, Object>>> query4Detail(UserAuthorEntity user, QueryDTO query) {
        Optional<String> stmtId = query.getStmtId();
        if (!stmtId.isPresent()) return Optional.empty();
        String[] args = StringUtils.split(stmtId.get(), '.');
        if (query.isNextOrg()) query = queryNextUnit(query);
        return getJdbcQuerySupport().queryForList(args[0], args[1], query.toMap(user));
    }

    Optional<Map<String, Object>> query4SubSummary(String reportId, DateRange dateRange, LocalDate startDate,
                                                   LocalDate endDate, Map<String, Object> params) {
        params.putAll(parseDataRange(dateRange, startDate, endDate));
        Optional<String> query_sqls = getBean(StatisticalDefinedFactory.class).getSubSummerySql(reportId);
        if (!query_sqls.isPresent())
            return Optional.empty();
        String[] args = StringUtils.split(query_sqls.get(), '.');
        return getJdbcQuerySupport().queryForMap(args[0], args[1], params);
    }

    public Map<String, Map<String, Object>> query4Summary(UserAuthorEntity user, QueryDTO query) {
        Optional<Collection<String>> stmtIds = query.getStmtIds();
        Preconditions.checkArgument(stmtIds.isPresent(), "stmtIds 不能为空");
        List<AsyncResult> asyncResults = query4Summary(stmtIds.get(), query.getDateRange().get(),
                query.getStartDate().isPresent() ? query.getStartDate().get() : null,
                query.getEndDate().isPresent() ? query.getEndDate().get() : null, query.toMap(user));
        Map<String, Map<String, Object>> result = Maps.newHashMap();
        asyncResults.forEach(res -> res.getMapIfExits().ifPresent(map -> {
            List<String> _temp_ids = (List<String>) res.getParams().get("reportIds");
            _temp_ids.forEach(id -> result.put(id, map));
        }));
        return result;
    }

    List<AsyncResult> query4Summary(Collection<String> summary_full_ids, DateRange dateRange, LocalDate startDate,
                                    LocalDate endDate, Map<String, Object> params) {
        params.putAll(parseDataRange(dateRange, startDate, endDate));
        Optional<List<String>> query_sqls = getBean(StatisticalDefinedFactory.class).getSummerySqls(summary_full_ids);
        if (!query_sqls.isPresent()) return null;
        Multimap<String, String> multimap = ArrayListMultimap.create();
        for (String $it : query_sqls.get()) {
            String[] args = StringUtils.split($it, ':');
            multimap.put(args[1], args[0]);
        }
        List<CompletableFuture<AsyncResult>> cfs = Lists.newArrayList();
        for (String $key : multimap.keySet()) {
            String[] args = StringUtils.split($key, '.');
            Map<String, Object> _temp = Maps.newHashMap();
            _temp.put("reportIds", multimap.get($key));
            _temp.put("single", multimap.get($key).size() == 1);
            _temp.putAll(params);
            cfs.add(getJdbcQuerySupport().queryMapSupplyAsync(args[0], args[1], _temp));
        }
        CompletableFuture.allOf(cfs.toArray(new CompletableFuture[0]));
        return cfs.stream().map(c -> {
            try {
                return c.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("CompletableFuture 4 query has error", e);
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    /**
     * 查询下级单位
     *
     * @return
     */
    public QueryDTO queryNextUnit(QueryDTO dto) {
        QueryDTO copy = dto.copy();
        Map<String, Object> params = Maps.newHashMap();
        copy.clearAllUnit();
        if (dto.getCompanyId().isPresent() || dto.getOrgIds().isPresent()) {
            if (dto.getCompanyId().isPresent())
                params.put("orgIds", Arrays.asList(dto.getCompanyId().get()));
            if (dto.getOrgIds().isPresent())
                params.put("orgIds", dto.getOrgIds().get());
            Optional<List<Map<String, Object>>> resultOpt = getJdbcQuerySupport().queryForList("condition",
                    "query_next_orgs", params);
            if (resultOpt.isPresent()) {
                resultOpt.get().stream().forEach(x -> {
                    String type = MapUtils.getString(x, "type");
                    Integer id = MapUtils.getInteger(x, "id");
                    if ("org".equals(type)) {
                        copy.addOrgId(id);
                    } else if ("store".equals(type)) {
                        copy.addStoreId(id);
                    }
                });
            }

        } else if (dto.getStoreIds().isPresent()) {
            params.put("storeIds", dto.getStoreIds().get());
            Optional<List<Map<String, Object>>> resultOpt = getJdbcQuerySupport().queryForList("condition",
                    "query_next_emps", params);
            if (resultOpt.isPresent()) {
                resultOpt.get().stream().forEach(x -> {
                    Integer id = MapUtils.getInteger(x, "id");
                    copy.addEmployeeId(id);
                });
            }
        }
        return copy;
    }

    public Map<String, Object> queryNextOrgDetail(Map<String, Object> params) {
        Integer orgType = MapUtils.getInteger(params, "orgType");
        return null;
    }


    public void fillSubOrgMap(Map<String, Object> requestBody) {
        String unitType = MapUtils.getString(requestBody, "int_searchunit_type", null);
        String returnbytimeType = MapUtils.getString(requestBody, "str_returnbytime_type", null);
        if (null != unitType && unitType.equals("nextOrg")) {
            if (null != returnbytimeType && returnbytimeType.equals("total")) {
                requestBody.put("str_returnbytime_type", "detail");
                requestBody.put("int_searchtime_type", null);
            }
            Optional<Map<String, Object>> mapOpt = getJdbcQuerySupport().queryForMap("condition", "query_search_params",
                    requestBody);
            if (mapOpt.isPresent())
                requestBody.putAll(mapOpt.get());
        }
    }

    private JdbcQuerySupport getJdbcQuerySupport() {
        return getBean("statisticalQuerySupport", JdbcQuerySupport.class);
    }

}
