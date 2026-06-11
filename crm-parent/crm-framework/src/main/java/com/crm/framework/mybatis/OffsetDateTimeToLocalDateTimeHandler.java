package com.crm.framework.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * PostgreSQL TIMESTAMPTZ → Java LocalDateTime 类型转换
 * 处理 MyBatis-Plus 查询时 TIMESTAMPTZ 无法自动转为 LocalDateTime 的问题
 */
@MappedTypes(LocalDateTime.class)
public class OffsetDateTimeToLocalDateTimeHandler extends BaseTypeHandler<LocalDateTime> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.atZone(ZoneId.systemDefault()).toOffsetDateTime());
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object val = rs.getObject(columnName);
        return convert(val);
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object val = rs.getObject(columnIndex);
        return convert(val);
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object val = cs.getObject(columnIndex);
        return convert(val);
    }

    private LocalDateTime convert(Object val) {
        if (val == null) return null;
        if (val instanceof LocalDateTime) return (LocalDateTime) val;
        if (val instanceof OffsetDateTime) return ((OffsetDateTime) val).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        if (val instanceof java.sql.Timestamp) return ((java.sql.Timestamp) val).toLocalDateTime();
        return null;
    }
}
