package com.dlz.test.db.cases.modal;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.Role;
import com.dlz.test.db.entity.SysSql;
import org.junit.jupiter.api.Test;


/**
 * 单元测试支撑类<br>
 * @author dk
 */
public class DBPojoUpdateTest extends BaseDBTest {
	@Test
	public void dbSqlTest3() {
		Role role = new Role();
		role.setId(11L);
		role.setRoleName("xx");
		role.setRoleAlias("xx2");

		final PojoUpdate<Role> id = DB.Pojo.updateW(role)
                .eq("ID", role.getId());
		showSql(id,"dbSqlTest2","update sys_role set ROLE_ALIAS='xx2',ROLE_NAME='xx' where ID = 11 and DELETED = 0");
	}

	@Test
	public void updateWrapperTest1() {
		SysSql dict = new SysSql();
		dict.setId(123L);
		dict.setName("123L");

		PojoUpdate<SysSql> eq = DB.Pojo.updateW(dict)
                .eq(SysSql::getId, 123);
		showSql(eq,"updateWrapperTest1","update SYS_SQL set NAME='123L' where ID = 123 and DELETED = 0");
	}
	@Test
	public void updateWrapperTest2() {
		SysSql dict = new SysSql();
		dict.setId(123L);
		dict.setName("123L");

		PojoUpdate<SysSql> eq = DB.Pojo.updateW(dict);
		showSql(eq,"updateWrapperTest2","update SYS_SQL set NAME='123L' where DELETED = 0");
	}
}
