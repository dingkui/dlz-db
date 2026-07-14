package com.dlz.test.db.cases.modal.wrapper;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.condition.Condition;
import com.dlz.db.modal.wrapper.TableDelete;
import com.dlz.kit.json.JSONMap;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.Dict;
import org.junit.jupiter.api.Test;

/**
 * TableDelete SQL 生成测试（仅验证 SQL，不执行）
 */
public class TableDeleteTest extends BaseDBTest {
    @Test
    public void tableDeleteTest1() {
        TableDelete delete = DB.table.deleteWrapper("t_b_dict")
                .addPara(Dict::getA2, "1")
                .sql("[id<#{id}]", new JSONMap("id", 123));
        showSql(delete, "tableDeleteTest1", "DELETE FROM t_b_dict where (id<123) AND deleted = 0");
    }

    @Test
    public void tableDeleteTest3() {
        TableDelete delete = DB.table.deleteWrapper("t_b_dict")
                .addPara(Dict::getA2, "1");
        showSql(delete, "tableDeleteTest3", "DELETE FROM t_b_dict where deleted = 0");
    }

    @Test
    public void tableDeleteTest31() {
        TableDelete delete = DB.table.deleteWrapper("sys_menu")
                .addPara(Dict::getA2, "1");
        showSql(delete, "tableDeleteTest3", "DELETE FROM sys_menu where deleted = 0");
    }

    @Test
    public void tableDeleteConditionTest1() {
        TableDelete delete = DB.table.deleteWrapper("t_b_dict")
                .addPara(Dict::getA2, "1")
                .where(Condition.where()
                        .ne(Dict::getA2, "3")
                        .eq(Dict::getA4, "2")
                        .le(Dict::getA6, "10")
                        .ors(o -> o.eq(Dict::getA6, "10").eq(Dict::getA6, "10"))
                        .ands(a -> a.eq(Dict::getA6, "10").eq(Dict::getA6, "10"))
                        .sql("exists (select 1 FROM dual where t_b_dict where 1=#{xx}) ", new JSONMap("xx", 999)));

        showSql(delete, "tableDeleteConditionTest1", "DELETE FROM t_b_dict where XXSS <> '3' AND A4 = '2' AND A6 <= '10' AND (A6 = '10' OR A6 = '10') AND (A6 = '10' AND A6 = '10') AND (exists (select 1 FROM dual where t_b_dict where 1=999) ) AND deleted = 0");
    }

    @Test
    public void tableDeleteConditionTest2() {
        TableDelete delete = DB.table.deleteWrapper("t_b_dict")
                .addPara(Dict::getA2, "1")
                .ors(o -> o
                        .in(Dict::getA2, "3,4,5,6")
                        .in(Dict::getA2, "'31',111,5,6")
                        .in(Dict::getA2, "1")
                        .in(Dict::getA2, "sql:select 2 FROM dual"));
        showSql(delete, "tableDeleteConditionTest2", "DELETE FROM t_b_dict where (XXSS IN (3,4,5,6) OR XXSS IN ('31','111','5','6') OR XXSS IN (1) OR XXSS IN (select 2 FROM dual)) AND deleted = 0");
    }

    @Test
    public void tableDeleteConditionTest3() {
        TableDelete delete = DB.table.deleteWrapper("dh_room")
                .where(Condition.where()
                        .eq("equipment_id", 1)
                        .eq("equipment_id2", 2)
                        .ands(w -> w.eq("xxId2", 3).eq("xxId1", 4))
                        .ors(w -> w.eq("xxId2", 3).eq("xxId1", 4))
                        .eq("xxId3", 5));
        showSql(delete, "tableDeleteConditionTest3", "DELETE FROM dh_room where equipment_id = 1 AND equipment_id2 = 2 AND (XX_ID2 = 3 AND XX_ID1 = 4) AND (XX_ID2 = 3 OR XX_ID1 = 4) AND XX_ID3 = 5");
    }
}
