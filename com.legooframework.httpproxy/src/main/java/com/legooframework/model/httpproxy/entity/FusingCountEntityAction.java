package com.legooframework.model.httpproxy.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.springframework.jdbc.core.RowMapper;

import java.util.Objects;

public class FusingCountEntityAction extends BaseEntityAction<FusingCountEntity> {

    public FusingCountEntityAction() {
        super(null);
    }

    void insert(FusingCountEntity instance) {
        String insert_sql = "INSERT INTO PROXY_NET_FUSE (module_name, req_path, fuse_time, req_query, fuse_count) VALUES (?,?,?,?,?)";
        Objects.requireNonNull(getJdbcTemplate()).update(insert_sql, instance::setValues);
    }

    @Override
    protected RowMapper<FusingCountEntity> getRowMapper() {
        return null;
    }
}
