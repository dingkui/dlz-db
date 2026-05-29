package com.dlz.test.db.cases.convertor;

import com.dlz.db.convertor.dbtype.TableColumnMapper;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Types;

import static org.junit.Assert.*;

public class TableColumnMapperTest extends BaseDBTest {

    private static Object invokeCover(Integer dbClass, Object value) throws Exception {
        Method method = TableColumnMapper.class.getDeclaredMethod("cover", Integer.class, Object.class);
        method.setAccessible(true);
        return method.invoke(null, dbClass, value);
    }

    @Test
    public void coverDecimalToBigDecimal() throws Exception {
        Object result = invokeCover(Types.DECIMAL, "123.45");
        assertTrue("DECIMAL 应转为 BigDecimal", result instanceof BigDecimal);
        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    public void coverNumericToBigDecimal() throws Exception {
        Object result = invokeCover(Types.NUMERIC, "99.99");
        assertTrue("NUMERIC 应转为 BigDecimal", result instanceof BigDecimal);
        assertEquals(new BigDecimal("99.99"), result);
    }

    @Test
    public void coverIntegerToLong() throws Exception {
        Object result = invokeCover(Types.INTEGER, "100");
        assertTrue("INTEGER 应转为 Long", result instanceof Long);
        assertEquals(100L, result);
    }

    @Test
    public void coverBigintToLong() throws Exception {
        Object result = invokeCover(Types.BIGINT, "9999999999");
        assertTrue("BIGINT 应转为 Long", result instanceof Long);
        assertEquals(9999999999L, result);
    }

    @Test
    public void coverDoubleToDouble() throws Exception {
        Object result = invokeCover(Types.DOUBLE, "3.14");
        assertTrue("DOUBLE 应转为 Double", result instanceof Double);
        assertEquals(3.14, (Double) result, 0.001);
    }

    @Test
    public void coverUnknownReturnsOriginal() throws Exception {
        Object original = new Object();
        Object result = invokeCover(Types.ARRAY, original);
        assertSame("未知类型应保持原值", original, result);
    }

    @Test
    public void converObj4Db_unknownColumnReturnsOriginal() {
        TableColumnMapper mapper = new TableColumnMapper();
        Object value = "keep_me";
        Object result = mapper.converObj4Db("SYS_SQL", "NOT_EXIST_COLUMN", value);
        assertEquals("表中不存在的字段应保持原值", value, result);
    }

    @Test
    public void converObj4Db_integerColumnToLong() {
        TableColumnMapper mapper = new TableColumnMapper();
        Object result = mapper.converObj4Db("SYS_SQL", "DELETED", "1");
        assertTrue("SYS_SQL.DELETED  为 INTEGER 类型，字符串数字应转为 Long", result instanceof Long);
        assertEquals(1L, result);
    }
}
