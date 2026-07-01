package com.dlz.test.db.cases.modal;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.condition.Condition;
import com.dlz.db.modal.wrapper.TableUpdate;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.Test;


/**
 * 单元测试支撑类<br>
 * @author dk
 */
public class DBTableUpdateTest extends BaseDBTest {
	@Test
	public void UpdateParaMapTest(){
		TableUpdate where = DB.Table.updateW("Sys_Sql")
				.set("sql_key", "1")
				.where(Condition.where()
						.eq("equipment_id", 1)
						.eq("equipment_id2", 1)
				);
//		where.execute();
		showSql(where,"UpdateParaMapTest","update Sys_Sql set sql_key='1' where equipment_id = 1 and equipment_id2 = 1 and DELETED = 0");
	}
}
