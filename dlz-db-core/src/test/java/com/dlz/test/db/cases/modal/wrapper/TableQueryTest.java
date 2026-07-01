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
        TableInsert insert = DB.Table.insertW("user");
        
        assertNotNull(insert);
        assertTrue(insert instanceof TableInsert);
    }

    @Test
    @DisplayName("测试 delete 方法")
    void testDeleteW() {
        TableDelete delete = DB.Table.deleteW("user");
        
        assertNotNull(delete);
        assertTrue(delete instanceof TableDelete);
    }

    @Test
    @DisplayName("测试 update 方法")
    void testUpdateW() {
        TableUpdate update = DB.Table.updateW("user");
        
        assertNotNull(update);
        assertTrue(update instanceof TableUpdate);
    }

    @Test
    @DisplayName("测试 select 方法")
    void testSelectW() {
        TableQuery query = DB.Table.selectW("user");
        
        assertNotNull(query);
        assertTrue(query instanceof TableQuery);
    }

    @Test
    @DisplayName("测试不同表名")
    void testDifferentTableNames() {
        TableInsert insert1 = DB.Table.insertW("user");
        TableInsert insert2 = DB.Table.insertW("order");
        TableInsert insert3 = DB.Table.insertW("product_category");
        
        assertNotNull(insert1);
        assertNotNull(insert2);
        assertNotNull(insert3);
    }

    @Test
    @DisplayName("测试空表名")
    void testEmptyTableName() {
        // 空表名可能会触发 WrapperBuildUtil 初始化错误
        assertThrows(Throwable.class, () -> {
            DB.Table.insertW("");
        });
    }

    @Test
    @DisplayName("测试 null 表名")
    void testNullTableName() {
        // null 表名可能会触发 ValidateException 初始化错误
        assertThrows(Throwable.class, () -> {
            DB.Table.insertW(null);
        });
    }
}
