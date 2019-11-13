package com.legooframework.model.statistical.entity;

import com.google.common.base.Enums;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


public class QueryDTO implements Cloneable {

    private Integer companyId;

    private List<Integer> orgIds;

    private List<Integer> storeIds;

    private List<Integer> employeeIds;

    private List<Integer> unitIds;

    private Integer userId;

    private String order;

    private LocalDate startDate;

    private LocalDate endDate;

    private DateRange dateRange;

    private String searchTimeType;

    private String searchUnitType;

    private String returnType;

    private String queryType;

    private String keywords;

    private String saleType;

    private String rfmType;

    private String orgLevel;

    private Integer orgType;

    private final String stmtId, rid, layout;

    private Map<String, Object> params;

    public static void check(Map<String, Object> requestBody) {
        Preconditions.checkArgument(requestBody.containsKey("date_rang_type"), "入参 date_rang_type 不可以为空值... ");
        Preconditions.checkArgument(requestBody.containsKey("int_search_orgType"), "入参 int_search_orgType 不可以为空值... ");
        Preconditions.checkArgument(requestBody.containsKey("ints_search_orgIds"), "入参 ints_search_orgIds 不可以为空值... ");
        Preconditions.checkArgument(requestBody.containsKey("date_start"), "入参 date_start 不可以为空值... ");
        Preconditions.checkArgument(requestBody.containsKey("date_end"), "入参 date_end 不可以为空值... ");
        Preconditions.checkArgument(requestBody.containsKey("date_rang_type"), "入参 date_rang_type 不可以为空值... ");
    }

    public Optional<String> getStmtId() {
        return Optional.of(stmtId);
    }

    public Optional<Collection<String>> getStmtIds() {
        if (Strings.isNullOrEmpty(this.stmtId)) return Optional.empty();
        return Optional.of(Splitter.on(',').splitToList(this.stmtId));
    }

    public QueryDTO(Map<String, Object> requestBody) {
        this(null, -1, null, null, null, requestBody);
    }


    private void fixedDate() {
        if ("week".equals(this.searchTimeType)) {
            this.startDate = this.startDate.dayOfWeek().withMinimumValue();
            this.endDate = this.endDate.dayOfWeek().withMaximumValue();
        } else if ("year".equals(this.searchTimeType)) {
            this.startDate = this.startDate.dayOfYear().withMinimumValue();
            this.endDate = this.endDate.dayOfYear().withMaximumValue();
        }
    }


    public QueryDTO(String queryType, int companyId, String stmtId, String rid, String layout, Map<String, Object> requestBody) {
        // saleRecord01.summary,saleRecord02.summary,saleRecord03.summary,memberDetail.addNumTotal  summary
        this.params = requestBody;
        this.stmtId = stmtId;
        this.rid = rid;
        this.layout = layout;
        this.userId = MapUtils.getInteger(requestBody, "int_search_userId", null);
        Integer search_orgType = MapUtils.getInteger(requestBody, "int_search_orgType", null);
        this.orgType = search_orgType;
        if (null != search_orgType) {
            String search_orgIds = MapUtils.getString(requestBody, "ints_search_orgIds", null);
            if (!Strings.isNullOrEmpty(search_orgIds)) {
                List<Integer> orgIds = Arrays.stream(StringUtils.split(search_orgIds, ',')).map(Integer::parseInt)
                        .collect(Collectors.toList());
                this.unitIds = orgIds;
                if (!CollectionUtils.isEmpty(orgIds)) {
                    switch (search_orgType) {
                        case 1:
                            this.companyId = orgIds.get(0);
                            break;
                        case 2:
                            this.orgIds = orgIds;
                            break;
                        case 3:
                            this.storeIds = orgIds;
                            break;
                        case 4:
                            this.employeeIds = orgIds;
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        this.searchTimeType = MapUtils.getString(requestBody, "int_searchtime_type", null);
        this.searchUnitType = MapUtils.getString(requestBody, "int_searchunit_type", null);
        this.orgLevel = MapUtils.getString(requestBody, "int_org_level", null);
        String dateStartStr = MapUtils.getString(requestBody, "date_start", null);
        this.startDate = Strings.isNullOrEmpty(dateStartStr) ? null : DateTimeUtils.parseYYYYMMDD(dateStartStr);
        String dateEndStr = MapUtils.getString(requestBody, "date_end");
        this.endDate = Strings.isNullOrEmpty(dateEndStr) ? null : DateTimeUtils.parseYYYYMMDD(dateEndStr);
        String dateRangeType = MapUtils.getString(requestBody, "date_rang_type");
        Preconditions.checkArgument(Enums.getIfPresent(DateRange.class, dateRangeType).isPresent(), "非法 date_rang_type 入参");
        this.dateRange = Enums.getIfPresent(DateRange.class, dateRangeType).get();
        this.queryType = queryType;
        this.returnType = MapUtils.getString(requestBody, "str_returnbytime_type", null);
        this.order = MapUtils.getString(requestBody, "str_search_order");
        this.keywords = MapUtils.getString(requestBody, "str_keywords", null);
        this.saleType = MapUtils.getString(requestBody, "str_sale_type", null);
        this.rfmType = MapUtils.getString(requestBody, "str_rfm_type", null);
        fixedDate();
    }

    public boolean isCurrentOrg() {
        if ("currentOrg".equals(this.searchUnitType)) return true;
        return false;
    }

    public boolean isNextOrg() {
        if ("nextOrg".equals(this.searchUnitType)) return true;
        return false;
    }

    public Optional<Integer> getUserId() {
        return Optional.ofNullable(this.userId);
    }

    public void clearAllUnit() {
        this.companyId = null;
        this.orgIds = Lists.newArrayList();
        this.employeeIds = Lists.newArrayList();
        this.storeIds = Lists.newArrayList();
    }

    public Optional<Integer> getCompanyId() {
        return Optional.ofNullable(companyId);
    }


    public Optional<List<Integer>> getOrgIds() {
        return Optional.ofNullable(orgIds);
    }

    public void setOrgIds(List<Integer> orgIds) {
        this.orgIds = orgIds;
    }

    public void addOrgId(Integer orgId) {
        this.orgIds.add(orgId);
    }

    public Optional<List<Integer>> getStoreIds() {
        return Optional.ofNullable(storeIds);
    }

    public void addStoreId(Integer storeId) {
        this.storeIds.add(storeId);
    }

    public void setStoreIds(List<Integer> storeIds) {
        this.storeIds = storeIds;
    }

    public Optional<List<Integer>> getEmployeeIds() {
        return Optional.ofNullable(employeeIds);
    }

    public void setEmployeeIds(List<Integer> employeeIds) {
        this.employeeIds = employeeIds;
    }

    public void addEmployeeId(Integer employeeId) {
        this.employeeIds.add(employeeId);
    }

    public Optional<String> getOrder() {
        return Optional.ofNullable(order);
    }

    public Optional<LocalDate> getStartDate() {
        return Optional.ofNullable(startDate);
    }

    public Optional<LocalDate> getEndDate() {
        return Optional.ofNullable(endDate);
    }

    public Optional<DateRange> getDateRange() {
        return Optional.ofNullable(dateRange);
    }

    public Optional<String> getSearchTimeType() {
        return Optional.ofNullable(searchTimeType);
    }

    public Optional<String> getSearchUnitType() {
        return Optional.ofNullable(searchUnitType);
    }

    public Optional<String> getReturnType() {
        return Optional.ofNullable(returnType);
    }

    public Optional<String> getQueryType() {
        return Optional.ofNullable(this.queryType);
    }

    public Map<String, Object> toMap(UserAuthorEntity user) {
        Map<String, Object> map = this.params;
        if (null != user) {
            map = user.toViewMap();
        } else {
            map = Maps.newHashMap();
        }
        map.put("companyId", companyId);
        map.put("orgIds", orgIds);
        map.put("storeIds", storeIds);
        map.put("employeeIds", employeeIds);
        map.put("userId", userId);
        map.put("order", order);
        map.put("startDate", null == startDate ? null : startDate.toString("yyyy-MM-dd 00:00:00"));
        map.put("endDate", null == endDate ? null : endDate.toString("yyyy-MM-dd 23:59:59"));
        map.put("searchTimeType", searchTimeType);
        map.put("searchUnitType", searchUnitType);
        map.put("returnType", this.returnType);
        map.put("keywords", this.keywords);
        map.put("saleType", this.saleType);
        map.put("dateRange", this.dateRange);
        map.put("rfmType", this.rfmType);
        map.put("orgLevel", this.orgLevel);
        map.put("unitIds", this.unitIds);
        map.put("orgType", this.orgType);
        return map;

    }

    public QueryDTO copy() {
        try {
            return (QueryDTO) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("复制失败");
        }
    }

    public boolean isSummaryQuery() {
        return "summary".equals(this.queryType);
    }

    public boolean isSubSummaryQuery() {
        return "details".equals(this.queryType);
    }

}
