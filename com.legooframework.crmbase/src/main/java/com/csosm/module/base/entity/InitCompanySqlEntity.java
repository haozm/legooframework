package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntity;

import java.util.List;

public class InitCompanySqlEntity extends BaseEntity<Integer> {

    private List<String> execSqls;

    InitCompanySqlEntity() {
        super(0);
    }


}
