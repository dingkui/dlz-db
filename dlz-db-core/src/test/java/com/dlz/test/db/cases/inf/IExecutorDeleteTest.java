package com.dlz.test.db.cases.inf;

import com.dlz.db.modal.wrapper.TableDelete;
import com.dlz.db.support.SqlRunThreadHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IExecutorDelete 接口测试
 */
@DisplayName("IExecutorDelete 接口测试")
class IExecutorDeleteTest {

    private TableDelete delete;

    @BeforeEach
    void setUp() {
        delete = new TableDelete("user");
        SqlRunThreadHolder.removeLogicDeleteSetting();
    }

    @Test
    @DisplayName("测试 ignoreLogicDelete(true)")
    void testIgnoreLogicDeleteTrue() {
        TableDelete result = delete.ignoreLogicDelete(true);
        
        assertNotNull(result);
        assertSame(delete, result);
        assertTrue(SqlRunThreadHolder.isIgnoreLogicDelete());
    }

    @Test
    @DisplayName("测试 ignoreLogicDelete(false)")
    void testIgnoreLogicDeleteFalse() {
        TableDelete result = delete.ignoreLogicDelete(false);
        
        assertNotNull(result);
        assertSame(delete, result);
        assertFalse(SqlRunThreadHolder.isIgnoreLogicDelete());
    }

    @Test
    @DisplayName("测试 getTableName()")
    void testGetTableName() {
        String tableName = delete.getTableName();
        
        assertEquals("user", tableName);
    }

    @Test
    @DisplayName("测试链式调用")
    void testChainedCall() {
        TableDelete result = delete
                .ignoreLogicDelete(true)
                .eq("id", 1);
        
        assertNotNull(result);
        assertSame(delete, result);
    }
}
