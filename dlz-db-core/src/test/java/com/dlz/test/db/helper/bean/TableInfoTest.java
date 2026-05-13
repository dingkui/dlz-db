package com.dlz.test.db.helper.bean;

import com.dlz.db.helper.bean.ColumnInfo;
import com.dlz.db.helper.bean.TableInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TableInfo 测试类
 */
@DisplayName("TableInfo 测试")
class TableInfoTest {

    private TableInfo tableInfo;

    @BeforeEach
    void setUp() {
        tableInfo = new TableInfo();
    }

    @Test
    @DisplayName("测试 TableInfo 基本属性")
    void testBasicProperties() {
        tableInfo.setTableName("user");
        tableInfo.setTableComment("用户表");
        
        assertEquals("user", tableInfo.getTableName());
        assertEquals("用户表", tableInfo.getTableComment());
    }

    @Test
    @DisplayName("测试主键列表")
    void testPrimaryKeys() {
        List<String> primaryKeys = Arrays.asList("id");
        tableInfo.setPrimaryKeys(primaryKeys);
        
        assertNotNull(tableInfo.getPrimaryKeys());
        assertEquals(1, tableInfo.getPrimaryKeys().size());
        assertEquals("id", tableInfo.getPrimaryKeys().get(0));
    }

    @Test
    @DisplayName("测试复合主键")
    void testCompositePrimaryKeys() {
        List<String> primaryKeys = Arrays.asList("user_id", "role_id");
        tableInfo.setPrimaryKeys(primaryKeys);
        
        assertEquals(2, tableInfo.getPrimaryKeys().size());
        assertTrue(tableInfo.getPrimaryKeys().contains("user_id"));
        assertTrue(tableInfo.getPrimaryKeys().contains("role_id"));
    }

    @Test
    @DisplayName("测试列信息列表")
    void testColumnInfos() {
        List<ColumnInfo> columnInfos = new ArrayList<>();
        
        ColumnInfo col1 = new ColumnInfo();
        col1.setColumnName("id");
        col1.setColumnType("bigint");
        columnInfos.add(col1);
        
        ColumnInfo col2 = new ColumnInfo();
        col2.setColumnName("name");
        col2.setColumnType("varchar(255)");
        columnInfos.add(col2);
        
        tableInfo.setColumnInfos(columnInfos);
        
        assertNotNull(tableInfo.getColumnInfos());
        assertEquals(2, tableInfo.getColumnInfos().size());
        assertEquals("id", tableInfo.getColumnInfos().get(0).getColumnName());
        assertEquals("name", tableInfo.getColumnInfos().get(1).getColumnName());
    }

    @Test
    @DisplayName("测试完整的表信息")
    void testCompleteTableInfo() {
        tableInfo.setTableName("user");
        tableInfo.setTableComment("用户信息表");
        tableInfo.setPrimaryKeys(Arrays.asList("id"));
        
        List<ColumnInfo> columnInfos = new ArrayList<>();
        ColumnInfo idCol = new ColumnInfo();
        idCol.setColumnName("id");
        idCol.setColumnType("bigint");
        idCol.setColumnComment("主键ID");
        columnInfos.add(idCol);
        
        ColumnInfo nameCol = new ColumnInfo();
        nameCol.setColumnName("name");
        nameCol.setColumnType("varchar(100)");
        nameCol.setColumnComment("姓名");
        columnInfos.add(nameCol);
        
        tableInfo.setColumnInfos(columnInfos);
        
        // 验证所有属性
        assertEquals("user", tableInfo.getTableName());
        assertEquals("用户信息表", tableInfo.getTableComment());
        assertEquals(1, tableInfo.getPrimaryKeys().size());
        assertEquals(2, tableInfo.getColumnInfos().size());
    }

    @Test
    @DisplayName("测试 null 值处理")
    void testNullValues() {
        assertNull(tableInfo.getTableName());
        assertNull(tableInfo.getTableComment());
        assertNull(tableInfo.getPrimaryKeys());
        assertNull(tableInfo.getColumnInfos());
    }

    @Test
    @DisplayName("测试空列表")
    void testEmptyLists() {
        tableInfo.setPrimaryKeys(new ArrayList<>());
        tableInfo.setColumnInfos(new ArrayList<>());
        
        assertNotNull(tableInfo.getPrimaryKeys());
        assertTrue(tableInfo.getPrimaryKeys().isEmpty());
        assertNotNull(tableInfo.getColumnInfos());
        assertTrue(tableInfo.getColumnInfos().isEmpty());
    }
}
