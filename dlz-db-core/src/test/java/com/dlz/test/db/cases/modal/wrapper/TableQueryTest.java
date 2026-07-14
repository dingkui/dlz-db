package com.dlz.test.db.cases.modal.wrapper;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.wrapper.TableDelete;
import com.dlz.db.modal.wrapper.TableInsert;
import com.dlz.db.modal.wrapper.TableQuery;
import com.dlz.db.modal.wrapper.TableUpdate;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TableQuery 表查询操作测试
 */
@DisplayName("TableQuery 表查询操作测试")
class TableQueryTest extends BaseDBTest {
    @Test
    @DisplayName("测试 insert 方法")
    void testInsertW() {
        TableInsert insert = DB.table.insertWrapper("user");
        
        assertNotNull(insert);
        assertTrue(insert instanceof TableInsert);
    }

    @Test
    @DisplayName("测试 delete 方法")
    void testDeleteW() {
        TableDelete delete = DB.table.deleteWrapper("user");
        
        assertNotNull(delete);
        assertTrue(delete instanceof TableDelete);
    }

    @Test
    @DisplayName("测试  UPDATE 方法")
    void testUpdateW() {
        TableUpdate update = DB.table.updateWrapper("user");
        
        assertNotNull(update);
        assertTrue(update instanceof TableUpdate);
    }

    @Test
    @DisplayName("测试 select 方法")
    void testSelectW() {
        TableQuery query = DB.table.selectWrapper("user");
        
        assertNotNull(query);
        assertTrue(query instanceof TableQuery);
    }

    @Test
    @DisplayName("测试不同表名")
    void testDifferentTableNames() {
        TableInsert insert1 = DB.table.insertWrapper("user");
        TableInsert insert2 = DB.table.insertWrapper("order");
        TableInsert insert3 = DB.table.insertWrapper("product_category");
        
        assertNotNull(insert1);
        assertNotNull(insert2);
        assertNotNull(insert3);
    }

    @Test
    @DisplayName("测试空表名")
    void testEmptyTableName() {
        // 空表名可能会触发 WrapperBuildUtil 初始化错误
        assertThrows(Throwable.class, () -> {
            DB.table.insertWrapper("");
        });
    }

    @Test
    @DisplayName("测试 null 表名")
    void testNullTableName() {
        // null 表名可能会触发 ValidateException 初始化错误
        assertThrows(Throwable.class, () -> {
            DB.table.insertWrapper(null);
        });
    }
}
