package com.legooframework.model.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface BatchSetter {

    void setValues(PreparedStatement ps) throws SQLException;

}
