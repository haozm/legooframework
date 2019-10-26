package com.legooframework.model.statistical.entity.rules;

import com.legooframework.model.statistical.entity.EchartMetaEntity;

class EchartMetaEntityBuilder extends FieldMetaRefSupport {

    private final String id, type, title, axisY1Title, axisY2Title, sql;

    EchartMetaEntityBuilder(String id, String type, String title, String axisY1Title, String axisY2Title, String sql) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.axisY1Title = axisY1Title;
        this.axisY2Title = axisY2Title;
        this.sql = sql;
    }

    EchartMetaEntity building() {
        return new EchartMetaEntity(id, type, title, sql, axisY1Title, axisY2Title, fieldMetaRefs);
    }

}
