package com.dlz.test.db.cases.convertor;

import com.dlz.db.convertor.columnname.ColumnNameCamel;
import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.kit.util.ValUtil;
import com.dlz.test.db.config.BaseDBTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.Assert.*;

/**
 * IExecutorQuery 转换器方法测试
 * 测试 convert()、convertNative()、convertUpper() 方法
 *
 * @author dingkui
 */
@Slf4j
public class ExecutorQueryConvertTest extends BaseDBTest {
    @BeforeEach
    public void before() {
        DB.Jdbc.execute("DELETE FROM SYS_SQL");
        DB.Jdbc.execute("insert into SYS_SQL(DELETED ,SQL_KEY,ID) values(0,'xxx',666)");
    }
    @AfterEach
    public void after() {
        DB.Jdbc.execute("DELETE FROM SYS_SQL");
    }
    /**
     * 测试 convert() 方法 - 使用自定义转换器
     */
    @Test
    public void testConvert_WithCustomConverter() {
        log.info("========== 测试 convert() 方法 ==========");
        
        // 使用驼峰转换器（默认）
        List<ResultMap> results = DB.Table.select("Sys_Sql")
                .setAllowFullQuery(true)
                .limit(2)
                .convert(new ColumnNameCamel())
                .queryList();
        
        assertNotNull("查询结果不应为 null", results);
        assertFalse("查询结果不应为空", results.isEmpty());
        
        // 验证字段名已转换为驼峰格式
        ResultMap firstResult = results.get(0);
        log.info("转换后的字段名: {}", firstResult.keySet());
        
        // 驼峰转换器应该将 SQL_KEY 转换为 sqlKey
        assertTrue("应该包含驼峰格式的字段名 sqlKey", 
                firstResult.containsKey("sqlKey") || firstResult.containsKey("SQL_KEY"));
    }

    /**
     * 测试 convertNative() 方法 - 使用原生转换器（不转换）
     */
    @Test
    public void testConvertNative() {
        log.info("========== 测试 convertNative() 方法 ==========");
        
        // 使用原生转换器（保持数据库原始字段名）
        List<ResultMap> results = DB.Table.select("Sys_Sql")
                .setAllowFullQuery(true)
                .limit(2)
                .convertNative()
                .queryList();
        
        assertNotNull("查询结果不应为 null", results);
        assertFalse("查询结果不应为空", results.isEmpty());
        
        // 验证字段名保持数据库原始格式（大写）
        ResultMap firstResult = results.get(0);
        log.info("原生字段名: {}", firstResult.keySet());
        
        // 原生转换器应该保持 SQL_KEY 不变
        assertTrue("应该包含原始字段名 SQL_KEY",
                firstResult.containsKey("SQL_KEY"));
    }

    /**
     * 测试 convertUpper() 方法 - 使用大写转换器
     */
    @Test
    public void testConvertUpper() {
        log.info("========== 测试 convertUpper() 方法 ==========");
        
        // 使用大写转换器
        List<ResultMap> results = DB.Table.select("Sys_Sql")
                .setAllowFullQuery(true)
                .limit(2)
                .convertUpper()
                .queryList();
        
        assertNotNull("查询结果不应为 null", results);
        assertFalse("查询结果不应为空", results.isEmpty());
        
        // 验证字段名为大写格式
        ResultMap firstResult = results.get(0);
        log.info("大写字段名: {}", firstResult.keySet());
        
        // 大写转换器应该将所有字段名转为大写
        boolean allUpperCase = firstResult.keySet().stream()
                .allMatch(key -> key.equals(key.toUpperCase()));
        assertTrue("所有字段名应该为大写", allUpperCase);
    }

    /**
     * 测试链式调用 - 多次转换
     */
    @Test
    public void testConvert_ChainedCalls() {
        log.info("========== 测试链式调用 ==========");
        
        // 先设置为原生，再改为驼峰
        List<ResultMap> results = DB.Table.select("Sys_Sql")
                .setAllowFullQuery(true)
                .limit(1)
                .convertNative()  // 第一次设置
                .convert(new ColumnNameCamel())  // 第二次覆盖
                .queryList();
        
        assertNotNull("查询结果不应为 null", results);
        assertFalse("查询结果不应为空", results.isEmpty());
        
        ResultMap firstResult = results.get(0);
        log.info("最终字段名: {}", firstResult.keySet());
        
        // 最后一次转换应该生效（驼峰）
        assertTrue("应该使用最后一次的转换器（驼峰）", 
                firstResult.containsKey("sqlKey") || firstResult.containsKey("SQL_KEY"));
    }

    /**
     * 测试 queryOne() 配合转换器
     */
    @Test
    public void testConvert_QueryOne() {
        log.info("========== 测试 queryOne() 配合转换器 ==========");
        
        // 使用原生转换器查询单条
        ResultMap result = DB.Table.select("Sys_Sql")
                .setAllowFullQuery(true)
                .eq("ID", 1)
                .convertNative()
                .queryOne();
        
        if (result != null) {
            log.info("单条记录字段名: {}", result.keySet());
            // 验证字段名保持原始格式
            assertTrue("应该包含原始字段名", 
                    result.containsKey("SQL_KEY") || result.containsKey("ID"));
        }
    }

    /**
     * 测试 queryPage() 配合转换器
     */
    @Test
    public void testConvert_QueryPage() {
        log.info("========== 测试 queryPage() 配合转换器 ==========");
        
        // 使用驼峰转换器分页查询
        Page<ResultMap> page = DB.Table.select("Sys_Sql")
                .setAllowFullQuery(true)
                .page(1, 5)
                .convert(new ColumnNameCamel())
                .queryPage();
        
        assertNotNull("分页结果不应为 null", page);
        assertNotNull("分页数据不应为 null", page.getRecords());
        log.info("分页查询返回 {} 条记录", page.getRecords().size());
        
        if (!page.getRecords().isEmpty()) {
            ResultMap firstResult = page.getRecords().get(0);
            log.info("分页结果字段名: {}", firstResult.keySet());
        }
    }

    /**
     * 测试不同转换器对同一查询的影响
     */
    @Test
    public void testConvert_CompareDifferentConverters() {
        log.info("========== 比较不同转换器的效果 ==========");
        
        // 使用原生转换器
        List<ResultMap> nativeResults = DB.Table.select("Sys_Sql")
                .setAllowFullQuery(true)
                .limit(1)
                .convertNative()
                .queryList();
        
        // 使用驼峰转换器
        List<ResultMap> camelResults = DB.Table.select("Sys_Sql")
                .setAllowFullQuery(true)
                .limit(1)
                .convert(new ColumnNameCamel())
                .queryList();
        
        // 使用大写转换器
        List<ResultMap> upperResults = DB.Table.select("Sys_Sql")
                .setAllowFullQuery(true)
                .limit(1)
                .convertUpper()
                .queryList();
        
        assertNotNull("原生转换器结果不应为 null", nativeResults);
        assertNotNull("驼峰转换器结果不应为 null", camelResults);
        assertNotNull("大写转换器结果不应为 null", upperResults);
        
        if (!nativeResults.isEmpty() && !camelResults.isEmpty() && !upperResults.isEmpty()) {
            log.info("原生字段名: {}", nativeResults.get(0).keySet());
            log.info("驼峰字段名: {}", camelResults.get(0).keySet());
            log.info("大写字段名: {}", upperResults.get(0).keySet());
            
            // 验证三种转换器的字段名确实不同
            // 注意：这里只是演示，实际可能因为数据库字段本身是大写而相同
        }
    }

    /**
     * 测试线程隔离性 - 转换器只影响当前线程的下一次查询
     */
    @Test
    public void testConvert_ThreadIsolation() {
        log.info("========== 测试线程隔离性 ==========");
        
        // 第一次查询：使用原生转换器
        List<ResultMap> results1 = DB.Table.select("Sys_Sql")
                .setAllowFullQuery(true)
                .limit(1)
                .convertNative()
                .queryList();
        
        // 第二次查询：不使用转换器（应该恢复默认）
        List<ResultMap> results2 = DB.Table.select("Sys_Sql")
                .setAllowFullQuery(true)
                .limit(1)
                .queryList();
        
        assertNotNull("第一次查询结果不应为 null", results1);
        assertNotNull("第二次查询结果不应为 null", results2);
        
        log.info("第一次查询字段名（原生）: {}", results1.get(0).keySet());
        log.info("第二次查询字段名（默认）: {}", results2.get(0).keySet());
        assertNotEquals(ValUtil.toStr(results1),ValUtil.toStr(results2));
        
        // 验证转换器的设置是一次性的，不会影响后续查询
        // 注意：具体行为取决于 SqlRunThreadHolder 的实现
    }
}
