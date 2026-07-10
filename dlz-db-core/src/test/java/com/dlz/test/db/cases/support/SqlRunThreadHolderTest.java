package com.dlz.test.db.cases.support;

import com.dlz.db.convertor.columnname.INameConverter;
import com.dlz.db.convertor.columnname.IConvertorToFieldName;
import com.dlz.db.convertor.dbtype.ITableColumnMapper;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.ds.DataSourceProperty;
import com.dlz.db.support.SqlRunThreadHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SqlRunThreadHolder 线程级配置测试")
class SqlRunThreadHolderTest {

    @AfterEach
    void cleanup() {
        SqlRunThreadHolder.removeConfig();
        SqlRunThreadHolder.removeConvertorToFieldName();
        SqlRunThreadHolder.removeColumnNameConvertor();
        SqlRunThreadHolder.removeTableColumnMapper();
        SqlRunThreadHolder.removeLogicDeleteSetting();
    }

    @Test
    @DisplayName("config - set/get/remove")
    void testConfig() {
        assertNull(SqlRunThreadHolder.getConfig());
        DataSourceProperty prop = new DataSourceProperty();
        prop.setUrl("jdbc:sqlite::memory:");
        DataSourceConfig config = new DataSourceConfig(prop);
        SqlRunThreadHolder.setConfig(config);
        assertSame(config, SqlRunThreadHolder.getConfig());
        SqlRunThreadHolder.removeConfig();
        assertNull(SqlRunThreadHolder.getConfig());
    }

    @Test
    @DisplayName("ConvertorToFieldName - 未设置时返回默认值并缓存")
    void testConvertorToFieldNameDefault() {
        IConvertorToFieldName defaultConvertor = dbKey -> dbKey;
        IConvertorToFieldName result = SqlRunThreadHolder.getConvertorToFieldName(defaultConvertor);
        assertSame(defaultConvertor, result);
        // 再次获取应返回缓存的同一实例
        IConvertorToFieldName result2 = SqlRunThreadHolder.getConvertorToFieldName(k -> "other");
        assertSame(defaultConvertor, result2);
    }

    @Test
    @DisplayName("ConvertorToFieldName - 已设置时返回设置的值")
    void testConvertorToFieldNameSet() {
        IConvertorToFieldName custom = dbKey -> dbKey.toLowerCase();
        SqlRunThreadHolder.setConvertorToFieldName(custom);
        IConvertorToFieldName result = SqlRunThreadHolder.getConvertorToFieldName(k -> "default");
        assertSame(custom, result);
    }

    @Test
    @DisplayName("ColumnNameConvertor - 未设置时返回默认值")
    void testColumnNameConvertorDefault() {
        INameConverter defaultConvertor = new INameConverter() {
            @Override public String toFieldName(String dbKey) { return dbKey; }
            @Override public String toDbName(String beanKey) { return beanKey; }
        };
        INameConverter result = SqlRunThreadHolder.getColumnNameConvertor(defaultConvertor);
        assertSame(defaultConvertor, result);
    }

    @Test
    @DisplayName("ColumnNameConvertor - set/remove")
    void testColumnNameConvertorSetRemove() {
        INameConverter convertor = new INameConverter() {
            @Override public String toFieldName(String dbKey) { return dbKey; }
            @Override public String toDbName(String beanKey) { return beanKey; }
        };
        SqlRunThreadHolder.setColumnNameConvertor(convertor);
        SqlRunThreadHolder.removeColumnNameConvertor();
        // After remove, should return default
        INameConverter def = new INameConverter() {
            @Override public String toFieldName(String dbKey) { return "def"; }
            @Override public String toDbName(String beanKey) { return "def"; }
        };
        assertSame(def, SqlRunThreadHolder.getColumnNameConvertor(def));
    }

    @Test
    @DisplayName("TableColumnMapper - 默认null时设置传入的默认值")
    void testTableColumnMapperDefault() {
        ITableColumnMapper defaultMapper = (tableName, columnName, type) -> type;
        ITableColumnMapper result = SqlRunThreadHolder.getTableColumnMapper(defaultMapper);
        assertSame(defaultMapper, result);
    }

    @Test
    @DisplayName("TableColumnMapper - 传入null默认值时返回null")
    void testTableColumnMapperNullDefault() {
        ITableColumnMapper result = SqlRunThreadHolder.getTableColumnMapper(null);
        assertNull(result);
    }

    @Test
    @DisplayName("IgnoreLogicDelete - 默认false")
    void testIgnoreLogicDeleteDefault() {
        assertFalse(SqlRunThreadHolder.isIgnoreLogicDelete());
    }

    @Test
    @DisplayName("IgnoreLogicDelete - 设置为true")
    void testIgnoreLogicDeleteTrue() {
        SqlRunThreadHolder.setIgnoreLogicDelete(true);
        assertTrue(SqlRunThreadHolder.isIgnoreLogicDelete());
    }

    @Test
    @DisplayName("IgnoreLogicDelete - remove后恢复false")
    void testIgnoreLogicDeleteRemove() {
        SqlRunThreadHolder.setIgnoreLogicDelete(true);
        SqlRunThreadHolder.removeLogicDeleteSetting();
        assertFalse(SqlRunThreadHolder.isIgnoreLogicDelete());
    }
}
