package com.dlz.test.db.cases.convertor;

import com.dlz.db.mapper.dbtype.TableColumnMapper;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;

public class TableColumnMapperTest extends BaseDBTest {

    private static Object invokeCover(Integer dbClass, Object value) throws Exception {
        Method method = TableColumnMapper.class.getDeclaredMethod("cover", Integer.class, Object.class);
        method.setAccessible(true);
        return method.invoke(null, dbClass, value);
    }

    @Test
    public void coverDecimalToBigDecimal() throws Exception {
        Object result = invokeCover(Types.DECIMAL, "123.45");
        assertTrue(result instanceof BigDecimal, "DECIMAL 应转为 BigDecimal");
        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    public void coverNumericToBigDecimal() throws Exception {
        Object result = invokeCover(Types.NUMERIC, "99.99");
        assertTrue(result instanceof BigDecimal, "NUMERIC 应转为 BigDecimal");
        assertEquals(new BigDecimal("99.99"), result);
    }

    @Test
    public void coverIntegerToLong() throws Exception {
        Object result = invokeCover(Types.INTEGER, "100");
        assertTrue(result instanceof Long, "INTEGER 应转为 Long");
        assertEquals(100L, result);
    }

    @Test
    public void coverBigintToLong() throws Exception {
        Object result = invokeCover(Types.BIGINT, "9999999999");
        assertTrue(result instanceof Long, "BIGINT 应转为 Long");
        assertEquals(9999999999L, result);
    }

    @Test
    public void coverDoubleToDouble() throws Exception {
        Object result = invokeCover(Types.DOUBLE, "3.14");
        assertTrue(result instanceof Double, "DOUBLE 应转为 Double");
        assertEquals(3.14, (Double) result, 0.001);
    }

    @Test
    public void coverUnknownReturnsOriginal() throws Exception {
        Object original = new Object();
        Object result = invokeCover(Types.ARRAY, original);
        assertSame(original, result, "未知类型应保持原值");
    }

    @Test
    public void convertObj4Db_unknownColumnReturnsOriginal() {
        TableColumnMapper mapper = new TableColumnMapper();
        Object value = "keep_me";
        Object result = mapper.convertObj4Db("sys_sql", "NOT_EXIST_COLUMN", value);
        assertEquals(value, result, "表中不存在的字段应保持原值");
    }

    @Test
    public void convertObj4Db_integerColumnToLong() {
        TableColumnMapper mapper = new TableColumnMapper();
        Object result = mapper.convertObj4Db("sys_sql", "deleted", "1");
        assertTrue(result instanceof Long, "sys_sql.deleted  为 INTEGER 类型，字符串数字应转为 Long");
        assertEquals(1L, result);
    }
}
