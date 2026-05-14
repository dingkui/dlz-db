package com.dlz.test.db.cases.core;

import com.dlz.db.core.JdbcValueUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JdbcValueUtils 工具类测试
 */
@DisplayName("JdbcValueUtils 工具类测试")
class JdbcValueUtilsTest {

    @Test
    @DisplayName("测试 lookupColumnName - 优先使用 columnLabel")
    void testLookupColumnNameWithLabel() throws SQLException {
        ResultSetMetaData mockMeta = createMockMetaData("user_id", "userId");
        
        String columnName = JdbcValueUtils.lookupColumnName(mockMeta, 1);
        
        assertEquals("userId", columnName);
    }

    @Test
    @DisplayName("测试 lookupColumnName - columnLabel 为空时使用 columnName")
    void testLookupColumnNameWithoutLabel() throws SQLException {
        ResultSetMetaData mockMeta = createMockMetaData("user_name", "");
        
        String columnName = JdbcValueUtils.lookupColumnName(mockMeta, 1);
        
        assertEquals("user_name", columnName);
    }

    @Test
    @DisplayName("测试 lookupColumnName - columnLabel 为 null 时使用 columnName")
    void testLookupColumnNameWithNullLabel() throws SQLException {
        ResultSetMetaData mockMeta = createMockMetaData("email", null);
        
        String columnName = JdbcValueUtils.lookupColumnName(mockMeta, 1);
        
        assertEquals("email", columnName);
    }

    /**
     * 创建模拟的 ResultSetMetaData
     */
    private ResultSetMetaData createMockMetaData(String columnName, String columnLabel) throws SQLException {
        return new ResultSetMetaData() {
            @Override
            public int getColumnCount() throws SQLException { return 1; }
            
            @Override
            public boolean isAutoIncrement(int column) throws SQLException { return false; }
            
            @Override
            public boolean isCaseSensitive(int column) throws SQLException { return false; }
            
            @Override
            public boolean isSearchable(int column) throws SQLException { return false; }
            
            @Override
            public boolean isCurrency(int column) throws SQLException { return false; }
            
            @Override
            public int isNullable(int column) throws SQLException { return 0; }
            
            @Override
            public boolean isSigned(int column) throws SQLException { return false; }
            
            @Override
            public int getColumnDisplaySize(int column) throws SQLException { return 0; }
            
            @Override
            public String getColumnLabel(int column) throws SQLException { return columnLabel; }
            
            @Override
            public String getColumnName(int column) throws SQLException { return columnName; }
            
            @Override
            public String getSchemaName(int column) throws SQLException { return ""; }
            
            @Override
            public int getPrecision(int column) throws SQLException { return 0; }
            
            @Override
            public int getScale(int column) throws SQLException { return 0; }
            
            @Override
            public String getTableName(int column) throws SQLException { return ""; }
            
            @Override
            public String getCatalogName(int column) throws SQLException { return ""; }
            
            @Override
            public int getColumnType(int column) throws SQLException { return Types.VARCHAR; }
            
            @Override
            public String getColumnTypeName(int column) throws SQLException { return "VARCHAR"; }
            
            @Override
            public boolean isReadOnly(int column) throws SQLException { return false; }
            
            @Override
            public boolean isWritable(int column) throws SQLException { return false; }
            
            @Override
            public boolean isDefinitelyWritable(int column) throws SQLException { return false; }
            
            @Override
            public String getColumnClassName(int column) throws SQLException { return "java.lang.String"; }
            
            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException { return null; }
            
            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }
        };
    }
}
