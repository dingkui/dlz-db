package com.dlz.test.db.cases.modal;

import com.dlz.db.modal.DbTable;
import com.dlz.db.modal.wrapper.TableDelete;
import com.dlz.db.modal.wrapper.TableInsert;
import com.dlz.db.modal.wrapper.TableQuery;
import com.dlz.db.modal.wrapper.TableUpdate;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbTable 表名操作测试
 */
@DisplayName("DbTable 表名操作测试")
class DbTableTest extends BaseDBTest {

    private DbTable dbTable;

    @BeforeEach
    void setUp() {
        dbTable = new DbTable();
    }

    @Test
    @DisplayName("测试 insert 方法")
    void testInsert() {
        TableInsert insert = dbTable.insert("user");
        
        assertNotNull(insert);
        assertTrue(insert instanceof TableInsert);
    }

    @Test
    @DisplayName("测试 delete 方法")
    void testDelete() {
        TableDelete delete = dbTable.delete("user");
        
        assertNotNull(delete);
        assertTrue(delete instanceof TableDelete);
    }

    @Test
    @DisplayName("测试 update 方法")
    void testUpdate() {
        TableUpdate update = dbTable.update("user");
        
        assertNotNull(update);
        assertTrue(update instanceof TableUpdate);
    }

    @Test
    @DisplayName("测试 select 方法")
    void testSelect() {
        TableQuery query = dbTable.select("user");
        
        assertNotNull(query);
        assertTrue(query instanceof TableQuery);
    }

    @Test
    @DisplayName("测试不同表名")
    void testDifferentTableNames() {
        TableInsert insert1 = dbTable.insert("user");
        TableInsert insert2 = dbTable.insert("order");
        TableInsert insert3 = dbTable.insert("product_category");
        
        assertNotNull(insert1);
        assertNotNull(insert2);
        assertNotNull(insert3);
    }

    @Test
    @DisplayName("测试空表名")
    void testEmptyTableName() {
        // 空表名可能会触发 WrapperBuildUtil 初始化错误
        assertThrows(Throwable.class, () -> {
            dbTable.insert("");
        });
    }

    @Test
    @DisplayName("测试 null 表名")
    void testNullTableName() {
        // null 表名可能会触发 ValidateException 初始化错误
        assertThrows(Throwable.class, () -> {
            dbTable.insert(null);
        });
    }
}
