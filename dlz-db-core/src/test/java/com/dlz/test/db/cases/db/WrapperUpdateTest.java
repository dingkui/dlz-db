package com.dlz.test.db.cases.db;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.kit.exception.ValidateException;
import com.dlz.kit.json.JSONMap;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.Role;
import com.dlz.test.db.entity.SysSql;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * 单元测试支撑类<br>
 * @author dk
 */
public class WrapperUpdateTest extends BaseDBTest {
	@Test
	public void dbSqlTest3() {
		Role role = new Role();
		role.setId(11L);
		role.setRoleName("xx");
		role.setRoleAlias("xx2");

		final PojoUpdate<Role> id = DB.Pojo.update(role)
                .eq("ID", role.getId());
		showSql(id,"dbSqlTest2","update sys_role set ROLE_ALIAS='xx2',ROLE_NAME='xx' where ID = 11 and DELETED = 0");
	}

	@Test
	public void updateWrapperTest1() {
		SysSql dict = new SysSql();
		dict.setId(123L);
		dict.setName("123L");

		PojoUpdate<SysSql> eq = DB.Pojo.update(dict)
                .eq(SysSql::getId, 123);
		showSql(eq,"updateWrapperTest1","update SYS_SQL set NAME='123L' where ID = 123 and DELETED = 0");
	}
	@Test
	public void updateWrapperTest2() {
		SysSql dict = new SysSql();
		dict.setId(123L);
		dict.setName("123L");

		PojoUpdate<SysSql> eq = DB.Pojo.update(dict);
		showSql(eq,"updateWrapperTest2","update SYS_SQL set NAME='123L' where DELETED = 0");
	}

	@Test
	public void testSetSqlWithoutParams() {
		PojoUpdate<Role> update = DB.Pojo.update(Role.class)
				.setSql("sort = sort + 1")
				.eq(Role::getId, 1);
		showSql(update, "testSetSqlWithoutParams",
				"update sys_role set SORT=sort + 1 where ID = 1 and DELETED = 0");
	}

	@Test
	public void testSetSqlWithParams() {
		PojoUpdate<Role> update = DB.Pojo.update(Role.class)
				.setSql("sort = sort + #{score}", new JSONMap("SCORE", 2))
				.eq(Role::getId, 1);
		showSql(update, "testSetSqlWithParams",
				"update sys_role set SORT=SORT + 2 where ID = 1 and DELETED = 0");
	}

	@Test
	public void testSetSqlWithMultipleParams() {
		PojoUpdate<Role> update = DB.Pojo.update(Role.class)
				.setSql("sort = sort + #{amount}", new JSONMap("AMOUNT", 5))
				.eq(Role::getId, 1);
		showSql(update, "testSetSqlWithMultipleParams",
				"update sys_role set SORT=SORT + 5 where ID = 1 and DELETED = 0");
	}

	@Test
	public void testSetSqlInvalidFormat() {
		assertThrows(ValidateException.class, () ->
				DB.Pojo.update(Role.class)
						.setSql("invalid_no_equals", new JSONMap("SCORE", 2)));
	}
}
