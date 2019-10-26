package com.legooframework.model.statistical.entity.rules;

import org.apache.commons.digester3.Rule;

abstract class BaseParseRule extends Rule {

    abstract String[] getPatterns();

    final static String ROOT_PATH = "statisticals/statistical";
    final static String META_PATH = ROOT_PATH + "/meta/field";

    final static String TABLE_PATH = ROOT_PATH + "/table";
    final static String TABLE_HEADER_FIELD_PATH = TABLE_PATH + "/header/field";

    final static String SUMMARY_PATH = ROOT_PATH + "/summary";
    final static String SUMMARY_FIELD_PATH = SUMMARY_PATH + "/field";

    final static String SUBSUMMARY_PATH = ROOT_PATH + "/subsummary";
    final static String SUBSUMMARY_FIELD_PATH = SUBSUMMARY_PATH + "/field";

    final static String ECHART_PATH = ROOT_PATH + "/echarts/echart";
    final static String ECHART_FIELD_PATH = ECHART_PATH + "/field";

}
