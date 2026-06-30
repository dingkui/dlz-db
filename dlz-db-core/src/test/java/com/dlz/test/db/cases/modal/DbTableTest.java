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
    void testInsertW() {
        TableInsert insert = dbTable.insertW("user");
        
        assertNotNull(insert);
        assertTrue(insert instanceof TableInsert);
    }

    @Test
    @DisplayName("测试 delete 方法")
    void testDeleteW() {
        TableDelete delete = dbTable.deleteW("user");
        
        assertNotNull(delete);
        assertTrue(delete instanceof TableDelete);
    }

    @Test
    @DisplayName("测试 update 方法")
    void testUpdateW() {
        TableUpdate update = dbTable.updateW("user");
        
        assertNotNull(update);
        assertTrue(update instanceof TableUpdate);
    }

    @Test
    @DisplayName("测试 select 方法")
    void testSelectW() {
        TableQuery query = dbTable.selectW("user");
        
        assertNotNull(query);
        assertTrue(query instanceof TableQuery);
    }

    @Test
    @DisplayName("测试不同表名")
    void testDifferentTableNames() {
        TableInsert insert1 = dbTable.insertW("user");
        TableInsert insert2 = dbTable.insertW("order");
        TableInsert insert3 = dbTable.insertW("product_category");
        
        assertNotNull(insert1);
        assertNotNull(insert2);
        assertNotNull(insert3);
    }

    @Test
    @DisplayName("测试空表名")
    void testEmptyTableName() {
        // 空表名可能会触发 WrapperBuildUtil 初始化错误
        assertThrows(Throwable.class, () -> {
            dbTable.insertW("");
        });
    }

    @Test
    @DisplayName("测试 null 表名")
    void testNullTableName() {
        // null 表名可能会触发 ValidateException 初始化错误
        assertThrows(Throwable.class, () -> {
            dbTable.insertW(null);
        });
    }
}
