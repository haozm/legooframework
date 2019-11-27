package com.legooframework.model.covariant.service;


import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MemberQueryHelper {

    private static final Log logger = LogFactory.getLog(MemberQueryHelper.class);

    /**
     * @param companyId 地址
     * @param storeId   蜘蛛
     * @return 查询语句封装
     */
    public static QuerySql builderByOther(Integer companyId, Integer storeId, Map<String, Object> queryParams, JdbcQuerySupport querySupport) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("builderByOther(companyId:%s,outerParams:%s)", companyId, queryParams));
        String other = MapUtils.getString(queryParams, "other", null);
        Map<String, String> params = StringUtils.isNotBlank(other) ? Maps.newHashMap(Splitter.on(';')
                .withKeyValueSeparator(':').split(other)) : Maps.newHashMap();
        boolean igore_customDate = false;
        StringBuilder querySQLClause = new StringBuilder("SELECT a.id FROM crm_member a \n INNER JOIN crm_store_member sm ON a.id = sm.member_id  \n INNER JOIN YG_Statistics.stat_member_rfm rfm ON a.id = rfm.member_id \n");
        StringBuilder whereClause = new StringBuilder(String.format("WHERE a.company_id = %d AND  sm.store_id = %d \n", companyId, storeId));


        if (StringUtils.isNotEmpty(MapUtils.getString(queryParams, "birthday"))) {
            String[] items = StringUtils.split(MapUtils.getString(queryParams, "birthday"), ',');
            String startDate = StringUtils.replace(items[0], "-", "");
            String endDate = StringUtils.replace(items[1], "-", "");
            String query_sql = "SELECT DATE_FORMAT( STR_TO_DATE(lunar_calendar,'%%Y%%m%%d'),'%%m-%%d') AS 'lunarCalendar'  \n FROM YG_Statistics.calendar_g_c_index " +
                    " WHERE gregorian_calendar IN ( '%s' , '%s' )  ORDER BY gregorian_calendar";
            List<String> list = querySupport.getJdbcTemplate().queryForList(String.format(query_sql, startDate, endDate), String.class);
            if (list.size() == 1) {
                whereClause.append("\n AND ( ( DATE_FORMAT(a.birthday, '%m%d') ")
                        .append(String.format(" BETWEEN  '%s' AND  '%s'  AND IFNULL( a.calendarType , 1) = 1 ) ",
                                startDate.substring(4), endDate.substring(4)))
                        .append(" OR ( IFNULL( a.calendarType , 1) = 2 AND DATE_FORMAT(a.lunarBirthday, '%m-%d') BETWEEN ")
                        .append(String.format(" '%s' AND '%s' ", list.get(0), list.get(0))).append(" )) ");
            } else {
                whereClause.append("\n AND ( ( DATE_FORMAT(a.birthday, '%m%d') ")
                        .append(String.format(" BETWEEN  '%s' AND  '%s'  AND IFNULL( a.calendarType , 1) = 1 ) ",
                                startDate.substring(4), endDate.substring(4)))
                        .append(" OR ( IFNULL( a.calendarType , 1) = 2 AND DATE_FORMAT(a.lunarBirthday, '%m-%d') BETWEEN ")
                        .append(String.format(" '%s' AND '%s' ", list.get(0), list.get(1))).append(" )) ");
            }
        }

        if (MapUtils.getString(queryParams, "beginDate") != null && MapUtils.getString(queryParams, "endDate") != null) {
            whereClause.append(String.format(" AND DATE_FORMAT(a.lastVisitTime,'%%Y-%%m-%%d') BETWEEN '%s' AND '%s' ",
                    MapUtils.getString(queryParams, "beginDate"), MapUtils.getString(queryParams, "endDate")));
        }

        if (StringUtils.isNotEmpty(MapUtils.getString(queryParams, "rfm.minReccencyLevel"))) {
            String query_rfm = "\n AND rfm.S_R_Level >= %s and rfm.S_R_Level <= %s and rfm.S_F_Level >= %s and rfm.S_F_Level <= %s and rfm.S_M_Level >= %s and rfm.S_M_Level <= %s ";
            whereClause.append(String.format(query_rfm,
                    MapUtils.getString(queryParams, "rfm.minReccencyLevel"), MapUtils.getString(queryParams, "rfm.maxReccencyLevel"),
                    MapUtils.getString(queryParams, "rfm.minFrenquencyLevel"), MapUtils.getString(queryParams, "rfm.maxFrenquencyLevel"),
                    MapUtils.getString(queryParams, "rfm.minMonetaryLevel"), MapUtils.getString(queryParams, "rfm.maxMonetaryLevel")));
        }
        if (StringUtils.isNotEmpty(MapUtils.getString(params, "quicksearch"))) {
            String[] items = StringUtils.split(MapUtils.getString(params, "quicksearch"), ',');
            for (String $it : items) {
                switch ($it) {
                    case "1":
                        LocalDate today01 = LocalDate.now();
                        String start = today01.dayOfMonth().withMinimumValue().toString("yyyy-MM-dd");
                        String end = today01.dayOfMonth().withMaximumValue().toString("yyyy-MM-dd");
                        params.put("birthday", String.format("%s,%s", start, end));
                        break;
                    case "2":
                        LocalDate today02 = LocalDate.now();
                        String moth03 = today02.plusMonths(-3).toString("yyyy-MM");
                        String moth02 = today02.plusMonths(-2).toString("yyyy-MM");
                        String moth01 = today02.plusMonths(-1).toString("yyyy-MM");
                        String moth00 = today02.toString("yyyy-MM");
                        params.put("purchaseTime", String.format("%s,%s,%s,%s", moth03, moth02, moth01, moth00));
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("非法的入参：%s", MapUtils.getString(params, "quicksearch")));
                }
            }
        }

        if (StringUtils.isNotEmpty(MapUtils.getString(params, "purchaseTime"))) {
            String[] items = StringUtils.split(MapUtils.getString(params, "purchaseTime"), ',');
            if (logger.isDebugEnabled())
                logger.debug("purchaseTime[] is " + Arrays.toString(items));
            if (items.length == 4) {
                String sub_sql = "\n SELECT DISTINCT sal.member_id from acp.crm_salerecord AS sal WHERE sal.company_id= " + companyId + "  " +
                        " AND sal.member_id is not NULL \n" +
                        String.format(" AND DATE_FORMAT(sal.createTime , '%%Y-%%m') BETWEEN '%s' AND '%s' \n", items[0], items[1]) +
                        String.format(" AND DATE_FORMAT(sal.createTime , '%%Y-%%m') NOT BETWEEN '%s' AND '%s' ", items[2], items[3]);
                whereClause.append(String.format("\n AND a.id IN ( %s )", sub_sql));
            }
        }

        String cus_date_sql = "SELECT sll.member_id AS 'mId', count(sll.id) AS 'cusTims',SUM(sll.saleTotalAmount) AS 'saleTotalAmount' " +
                " FROM acp.crm_salerecord sll  " +
                " WHERE sll.company_id = %s " +
                "   AND sll.store_id = %s " +
                "   AND sll.member_id IS NOT NULL " +
                "   AND DATE_FORMAT(sll.createTime, GET_FORMAT(DATETIME, 'iso')) BETWEEN '%s 00:00:00' and '%s 23:59:59' GROUP BY sll.member_id";

        if (StringUtils.isNotEmpty(MapUtils.getString(params, "consumeTotalAmount"))) {
            String[] items = StringUtils.split(MapUtils.getString(params, "consumeTotalAmount"), ',');
            if (items.length == 3) {
                int type = Integer.parseInt(items[0]);
                switch (type) {
                    case 0:
                        querySQLClause.append("\n INNER JOIN YG_Statistics.stat_member_past_year_buy cms01 ON a.id = cms01.member_id ");
                        whereClause.append(String.format("\n AND  cms01.s_py_buy_amount02 >=  %s  AND cms01.s_py_buy_amount02 <= %s  ", items[1], items[2]));
                        break;
                    case 1:
                        querySQLClause.append("\n INNER JOIN YG_Statistics.stat_member_this_year_buy cms01 ON a.id = cms01.member_id ");
                        whereClause.append(String.format("\n AND  cms01.s_ty_buy_amount02 >= %s  AND cms01.s_ty_buy_amount02  <= %s  ", items[1], items[2]));
                        break;
                    case 2:
                        querySQLClause.append("\n INNER JOIN YG_Statistics.stat_member_total_buy cms01 ON a.id = cms01.member_id ");
                        whereClause.append(String.format("\n AND  cms01.s_total_buy_amount02 >= %s AND cms01.s_total_buy_amount02 <= %s  ", items[1], items[2]));
                        break;
                    case 3:
                        Preconditions.checkArgument(StringUtils.isNotEmpty(MapUtils.getString(params, "customDate")),
                                "烦请指定消费时间范围...customDate");
                        String[] dates = StringUtils.split(MapUtils.getString(params, "customDate"), ',');
                        igore_customDate = true;
                        String _sql = String.format(cus_date_sql, companyId, storeId == null ? 0 : storeId, dates[0], dates[1]);
                        _sql = String.format("SELECT temp.mId FROM ( %s ) AS temp WHERE temp.saleTotalAmount >= %s AND temp.saleTotalAmount <= %s",
                                _sql, items[1], items[2]);
                        whereClause.append(String.format(" AND a.id IN ( %s ) ", _sql));
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("非法的入参.....%s", MapUtils.getString(params, "consumeTotalAmount")));
                }
            }
        }

        if (StringUtils.isNotEmpty(MapUtils.getString(params, "consumeTotalTimes"))) {
            String[] items = StringUtils.split(MapUtils.getString(params, "consumeTotalTimes"), ',');
            if (items.length == 3) {
                int type = Integer.parseInt(items[0]);
                switch (type) {
                    case 0:
                        querySQLClause.append("\n INNER JOIN YG_Statistics.stat_member_past_year_buy cms02 ON a.id = cms02.member_id ");
                        whereClause.append(String.format("\n and  IFNULL(cms02.s_py_buy_freq,0) >=  %s AND IFNULL(cms02.s_py_buy_freq,0) <= %s  ", items[1], items[2]));
                        break;
                    case 1:
                        querySQLClause.append("\n INNER JOIN YG_Statistics.stat_member_this_year_buy cms02 ON a.id = cms02.member_id ");
                        whereClause.append(String.format("\n and  IFNULL(cms02.s_ty_buy_freq,0) >= %s AND IFNULL(cms02.s_ty_buy_freq,0) <= %s ", items[1], items[2]));
                        break;
                    case 2:
                        querySQLClause.append("\n INNER JOIN YG_Statistics.stat_member_total_buy cms02 ON a.id = cms02.member_id ");
                        whereClause.append(String.format("\n and IFNULL(cms02.s_total_buy_freq,0) >= %s AND IFNULL(cms02.s_total_buy_freq,0) <= %s  ", items[1], items[2]));
                        break;
                    case 3:
                        Preconditions.checkArgument(StringUtils.isNotEmpty(MapUtils.getString(params, "customDate")),
                                "请指定消费时间范围...customDate");
                        String[] dates = StringUtils.split(MapUtils.getString(params, "customDate"), ',');
                        igore_customDate = true;
                        String _sql = String.format(cus_date_sql, companyId, storeId == null ? 0 : storeId, dates[0], dates[1]);
                        _sql = String.format("SELECT temp.mId FROM ( %s ) AS temp WHERE temp.cusTims >= %s AND temp.cusTims <= %s",
                                _sql, items[1], items[2]);
                        whereClause.append(String.format(" AND a.id IN ( %s ) ", _sql));
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("非法的入参.....%s", MapUtils.getString(params, "consumeTotalTimes")));
                }
            }
        }

        if (!igore_customDate && StringUtils.isNotEmpty(MapUtils.getString(params, "customDate"))) {
            String[] items = StringUtils.split(MapUtils.getString(params, "customDate"), ',');
            if (items.length == 2) {
                String sub_sql;
                if (storeId == null) {
                    sub_sql = String.format(" SELECT DISTINCT sll.member_id FROM  acp.crm_salerecord sll WHERE sll.company_id = %s AND sll.member_id IS NOT NULL AND DATE_FORMAT(sll.createTime, '%%Y-%%m-%%d') BETWEEN  '%s' and  '%s' ",
                            companyId, items[0], items[1]);
                } else {
                    sub_sql = String.format(" SELECT DISTINCT sll.member_id FROM  acp.crm_salerecord sll WHERE sll.company_id = %s  AND sll.member_id IS NOT NULL AND  sll.store_id = %s AND  DATE_FORMAT(sll.createTime, '%%Y-%%m-%%d') BETWEEN  '%s' and  '%s' ",
                            companyId, storeId, items[0], items[1]);
                }
                whereClause.append(String.format(" AND a.id IN ( %s ) ", sub_sql));
            }
        }

        if (StringUtils.isNotEmpty(MapUtils.getString(params, "addMemberDate"))) {
            String[] items = StringUtils.split(MapUtils.getString(params, "addMemberDate"), ',');
            if (items.length == 2) {
                whereClause.append("\n AND DATE_FORMAT(a.createTime, '%Y-%m-%d') ")
                        .append(String.format(" BETWEEN  '%s' and  '%s' ", items[0], items[1]));
            }
        }

        if (StringUtils.isNotEmpty(MapUtils.getString(params, "cusUnitPrice"))) {
            String[] items = StringUtils.split(MapUtils.getString(params, "cusUnitPrice"), ',');
            if (logger.isDebugEnabled())
                logger.debug(String.format("cusUnitPrice:%s", Arrays.toString(items)));
            if (items.length == 2) {
                querySQLClause.append("\n LEFT JOIN YG_Statistics.stat_member_total_buy cup ON cup.company_id = a.company_id AND a.id = cup.member_id ");
                whereClause.append("\n AND cup.c_total_buy_avg_p_o_p ")
                        .append(String.format(" BETWEEN  %s and  %s ", items[0], items[1]));
            }
        }

        if (StringUtils.isNotEmpty(MapUtils.getString(params, "notCustomDate"))) {
            String[] items = StringUtils.split(MapUtils.getString(params, "notCustomDate"), ',');
            if (items.length == 2) {
                String sub_sql = " SELECT DISTINCT sla.member_id FROM acp.crm_salerecord AS sla " +
                        " WHERE sla.company_id = %s AND sla.member_id IS NOT NULL AND DATE_FORMAT(sla.createTime, '%%Y-%%m-%%d') BETWEEN '%s' and  '%s' ";
                sub_sql = String.format(sub_sql, companyId, items[0], items[1]);
                if (storeId != null && storeId != 0)
                    sub_sql += " AND sla.store_id = " + storeId;
                whereClause.append(String.format("\n AND a.id NOT IN ( %s ) ", sub_sql));
            }
        }

        if (StringUtils.isNotEmpty(MapUtils.getString(params, "buildWx"))) {
            String buildWx = MapUtils.getString(params, "buildWx", null);
            String sub_sql = " SELECT DISTINCT awmm.member_id FROM acp.acp_weixin_member AS awmm WHERE awmm.company_id = " + companyId + " AND awmm.member_id IS NOT NULL ";
            if (StringUtils.equals("1", buildWx)) {
                whereClause.append(String.format("\n AND a.id IN ( %s ) ", sub_sql));
            } else if (StringUtils.equals("0", buildWx)) {
                whereClause.append(String.format("\n AND a.id NOT IN ( %s ) ", sub_sql));
            }
        }

        if (StringUtils.isNotEmpty(MapUtils.getString(params, "labelIds"))) {
            String[] items = StringUtils.split(MapUtils.getString(params, "labelIds"), ',');
            if (items.length > 0) {
                String sub_query = String.format(" SELECT ulr.member_id FROM acp.user_label_remark ulr WHERE ulr.company_id = %s AND ulr.member_id IS NOT NULL AND ulr.enabled = 1 AND ulr.label_id IN ( %s )",
                        companyId, StringUtils.join(MapUtils.getString(params, "labelIds")));
                whereClause.append(String.format("\n AND a.id IN ( %s )", sub_query));
            }
        }

        if (StringUtils.isNotEmpty(MapUtils.getString(params, "totalScore"))) {
            String[] items = StringUtils.split(MapUtils.getString(params, "totalScore"), ',');
            if (items.length == 2) {
                whereClause.append(String.format("\n AND  IFNULL(a.totalScore,0) >= %s AND IFNULL(a.totalScore,0) <= %s  ", items[0], items[1]));
            }
        }

        if (StringUtils.isNotEmpty(MapUtils.getString(params, "queryKeyword"))) {
            String queryKeyword = MapUtils.getString(params, "queryKeyword");
            queryKeyword = StringUtils.remove(queryKeyword, '\'');
            whereClause.append(String.format("\n AND  ( a.name LIKE '%%%s%%' OR a.phone LIKE '%%%s%%' ) ", queryKeyword, queryKeyword));
        }

        if (StringUtils.isNotEmpty(MapUtils.getString(params, "cardTypeId"))) {
            int cardTypeId = MapUtils.getInteger(params, "cardTypeId", -1);
            whereClause.append(String.format("\n AND a.memberCardType_id = %s ", cardTypeId));
        }

        QuerySql querySql = new QuerySql(querySQLClause.toString(), whereClause.toString());

        if (logger.isDebugEnabled())
            logger.debug(String.format("builderByOther(%s) retrun %s", other, querySql));
        return querySql;
    }

    public static class QuerySql {
        private String querySQLClause;
        private String whereClause;

        QuerySql(String querySQLClause, String whereClause) {
            this.whereClause = whereClause;
            this.querySQLClause = querySQLClause;
        }

        public String getQuerySQLClause() {
            return querySQLClause;
        }

        public boolean hasWhere() {
            return StringUtils.isNotEmpty(whereClause);
        }

        public boolean hasQuerySQL() {
            return StringUtils.isNotEmpty(querySQLClause);
        }

        public String getWhereClause() {
            return whereClause;
        }

        @Override
        public String toString() {
            return "\n" + querySQLClause + "\n" + whereClause;
        }
    }

}
