package com.dlz.db.option.point;

import com.dlz.db.option.point.context.ParameterContext;

import java.sql.SQLException;

/** PreparedStatement 参数绑定的排他桩点。 */
public interface JdbcParameterPoint extends OptionPoint {
    void bindJdbcParameter(ParameterContext context) throws SQLException;
}
