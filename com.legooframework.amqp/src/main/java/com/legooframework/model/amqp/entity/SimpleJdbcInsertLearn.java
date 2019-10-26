package com.legooframework.model.amqp.entity;

import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;


public class SimpleJdbcInsertLearn {


    private SimpleJdbcInsert jdbcInsert;


    public SimpleJdbcInsertLearn(DataSource dataSource) {
        this.jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("");
    }

    public void singleInsert() {

    }
}
