package com.dlz.test.db.cases.modal.wrapper;

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
public class PojoUpdateTest extends BaseDBTest {
	@Test
	public void dbSqlTest3() {
		Role role = new Role();
		role.setId(11L);
		role.setRoleName("xx");
		role.setRoleAlias("xx2");

		final PojoUpdate<Role> id = DB.Pojo.update(Role.class).set(role).ignore((name,value)->"id".equals(name)||value==null).eq("id", role.getId());
		showSql(id,"dbSqlTest2","UPDATE sys_role SET role_name='xx',role_alias='xx2' WHERE id = 11 AND deleted = 0");
	}

	@Test
	public void updateWrapperTest1() {
		SysSql dict = new SysSql();
		dict.setId(123L);
		dict.setName("123L");

		PojoUpdate<SysSql> eq = DB.Pojo.update(dict).eq(SysSql::getId, 123);
		showSql(eq,"updateWrapperTest1","UPDATE sys_sql SET NAME='123L' where id = 123 AND deleted = 0");
	}
	@Test
	public void updateWrapperTest2() {
		SysSql dict = new SysSql();
		dict.setId(123L);
		dict.setName("123L");

		PojoUpdate<SysSql> eq = DB.Pojo.update(dict);
		showSql(eq,"updateWrapperTest2","UPDATE sys_sql SET name='123L' where deleted = 0");
	}
}
