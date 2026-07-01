package com.dlz.test.db.cases.modal.wrapper;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.wrapper.TableInsert;
import com.dlz.kit.exception.SystemException;
import com.dlz.kit.json.JSONMap;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TableInsert 表插入操作测试
 */
@DisplayName("TableQuery 表查询操作测试")
public class TableInsertTest extends BaseDBTest {
    @Test
    public void tableInsertTest1() {
        TableInsert insert = DB.Table.insertW("t_b_dict")
                .value("dict_name", "测试")
                .value("dict_code", "test_code");
        assertThrows(SystemException.class, insert::jdbcSql);
    }

    @Test
    public void tableInsertTest2() {
        TableInsert insert = DB.Table.insertW("t_b_dict")
                .value("dict_name", "测试")
                .value("dict_code", "test_code")
                .value("dictStatus", 1);
        showSql(insert, "tableInsertTest2", "insert into t_b_dict(DICT_STATUS,DELETED) values('1',0)");
    }

    @Test
    public void tableInsertTest3() {
        JSONMap data = new JSONMap();
        data.put("dict_name", "测试");
        data.put("dict_code", "test_code");
        data.put("dictStatus", 1);
        TableInsert insert = DB.Table.insertW("t_b_dict").value(data);
        showSql(insert, "tableInsertTest3", "insert into t_b_dict(DICT_STATUS,DELETED) values('1',0)");

        TableInsert insert2 = DB.Table.insertW("t_b_dict").value(data);
        showSql(insert2, "tableInsertTest3", "insert into t_b_dict(DICT_STATUS,DELETED) values('1',0)");
    }

}
