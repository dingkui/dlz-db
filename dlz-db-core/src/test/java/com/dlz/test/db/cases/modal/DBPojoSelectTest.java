package com.dlz.test.db.cases.modal;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.wrapper.PojoQuery;
import com.dlz.kit.json.JSONMap;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.Menu;
import com.dlz.test.db.entity.Role;
import com.dlz.test.db.entity.SysSql;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class DBPojoSelectTest extends BaseDBTest {
    @Test
    public void conditionWhereTest3_2() {
        Menu menu = new Menu();
        menu.setCode("qsm");
        menu.setName("全生命周期项目");
        final PojoQuery<Menu> wrapper = DB.Pojo.selectW(Menu.class)
                .ne(menu.getId() != null, Menu::getId, menu.getId())
                .ors(w -> w
                        .eq(Menu::getCode, menu.getCode())
                        .ands(s -> s.eq(Menu::getName, menu.getName()).eq(Menu::getCategory, "1")));
        showSql(wrapper, "conditionWhereTest3_2", "select * from SYS_MENU t where (CODE = 'qsm' or (NAME = '全生命周期项目' and CATEGORY = 1)) and DELETED = 0");
    }

    @Test
    public void conditionWhereTest3_3() {
        Menu menu = new Menu();
        menu.setCode("qsm");
        menu.setName("全生命周期项目");
        final PojoQuery<Menu> menuQueryWrapper = DB.Pojo.selectW(Menu.class)
                .ne(menu.getId() != null, Menu::getId, menu.getId())
                .eq(Menu::getCategory, "1")
                .ors(w -> w
                        .eq(Menu::getCode, menu.getCode())
                        .eq(Menu::getName, "1"));
        showSql(menuQueryWrapper, "conditionWhereTest3_3", "select * from SYS_MENU t where CATEGORY = 1 and (CODE = 'qsm' or NAME = '1') and DELETED = 0");
    }

    @Test
    public void conditionWhereTest3_4() {
        Menu menu = new Menu();
        menu.setCode("qsm");
        menu.setName("全生命周期项目");
        final PojoQuery<Menu> menuQueryWrapper = DB.Pojo.selectW(Menu.class)
                .ne(menu.getId() != null, Menu::getId, menu.getId())
                .eq(Menu::getCategory, "1")
                .ors(xx -> xx
                        .eq(Menu::getCode, menu.getCode())
                        .eq(Menu::getName, "1"));
        showSql(menuQueryWrapper, "conditionWhereTest3_4", "select * from SYS_MENU t where CATEGORY = 1 and (CODE = 'qsm' or NAME = '1') and DELETED = 0");
    }

    @Test
    public void conditionWhereTest3_5() {
        Menu menu = new Menu();
        menu.setCode("qsm");
        menu.setName("全生命周期项目");
        final PojoQuery<Menu> menuQueryWrapper = DB.Pojo.selectW(Menu.class)
                .ne(menu.getId() != null, Menu::getId, menu.getId())
                .ors(w -> w
                        .eq(Menu::getCode, menu.getCode())
                        .ands(xx1 -> xx1
                                .eq(Menu::getName, menu.getName())
                                .eq(Menu::getCategory, "1")));
        showSql(menuQueryWrapper, "conditionWhereTest3_5", "select * from SYS_MENU t where (CODE = 'qsm' or (NAME = '全生命周期项目' and CATEGORY = 1)) and DELETED = 0");
    }

    @Test
    public void conditionWhereTest4_2() {
        Menu menu = new Menu();
        menu.setId(1L);
        menu.setCode("qsm");
        menu.setName("全生命周期项目");
        final PojoQuery<Menu> menuQueryWrapper = DB.Pojo.selectW(Menu.class)
                .ne(menu.getId() != null, Menu::getId, menu.getId())
                .sql("xx in (select x from dual where 1=#{a} and 2=#{b})", new JSONMap("a", 1, "b", 2));
        showSql(menuQueryWrapper, "conditionWhereTest4_2", "select * from SYS_MENU t where ID <> 1 and (xx in (select x from dual where 1=1 and 2=2)) and DELETED = 0");
    }


    @Test
    public void searchWrapperTest1() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        PojoQuery select = DB.Pojo.selectW(SysSql.class)
                .select(SysSql::getId);
        showSql(select, "searchWrapperTest1", "select ID from SYS_SQL t where DELETED = 0");
    }

    @Test
    public void searchWrapperTest2() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        PojoQuery<SysSql> query = DB.Pojo.selectW(SysSql.class);
        showSql(query, "searchWrapperTest2", "select * from SYS_SQL t where DELETED = 0");
    }

    @Test
    public void searchWrapperTest3() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        PojoQuery<SysSql> query = DB.Pojo.selectW(SysSql.class)
                .eq(SysSql::getId, 123);
        showSql(query, "searchWrapperTest3", "select * from SYS_SQL t where ID = 123 and DELETED = 0");
    }


    @Test
    public void dbSqlTest1() {
        final PojoQuery eq = DB.Pojo.selectW(Role.class)
                .select(Role::getRoleAlias)
                .in(Role::getId, "11,22")
                .eq(Role::getDeleted, 0);
        showSql(eq, "dbSqlTest1", "select ROLE_ALIAS from sys_role t where ID in (11,22) and DELETED = 0");
    }


    @Test
    public void dbSqlTest2() {
        final PojoQuery eq = DB.Pojo.selectW(Role.class)
                .select(Role::getRoleAlias)
                .in(Role::getId, "a11,x22")
                .eq(Role::getDeleted, 0);
        showSql(eq, "dbSqlTest2", "select ROLE_ALIAS from sys_role t where ID in ('a11','x22') and DELETED = 0");
    }

    @Test
    public void dbSqlTest21() {
        final PojoQuery eq = DB.Pojo.selectW(Role.class)
                .select(Role::getId)
                .in(Role::getId, "a11,x22")
                .eq(Role::getDeleted, 0);
        showSql(eq, "dbSqlTest2", "select ID from sys_role t where ID in ('a11','x22') and DELETED = 0");
    }
}