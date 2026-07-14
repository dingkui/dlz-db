package com.dlz.test.db.cases.helper;

import com.dlz.db.modal.DB;
import com.dlz.db.support.bean.TableInfo;
import com.dlz.db.support.helper.HelperScan;
import com.dlz.kit.json.JSONMap;
import com.dlz.kit.util.ValUtil;
import com.dlz.kit.util.system.FieldReflections;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class SqlHelper2Test extends BaseDBTest {

    @Test
    public void getTableInfoTest() {
        TableInfo sys_test = DB.ds.getSqlHelper().getTableInfo("user");
        log.info(ValUtil.toStr(sys_test));
    }
    @Test
    public void scanTest() {
        HelperScan.scan(null);
        HelperScan.scan("xx");
    }
    @Test
    public void createTableTest() {
        DB.jdbc.execute("DROP TABLE IF EXISTS crete_user_test");
        DB.ds.getSqlHelper().createTable("crete_user_test", User.class);
        final User user = new User();
        user.setName("xx");
        user.setAge(1);
        user.setCreateTime(new java.util.Date());
        user.setSex("1");
        DB.table.insert("crete_user_test", new JSONMap(user));
        final User user1 = DB.table.selectWrapper("crete_user_test").setAllowFullQuery(true).queryOne(User.class);
        assertEquals(1L, user1.getId(), "userId 应该为1");

        DB.batch.insert("crete_user_test", Arrays.asList(new JSONMap(user),new JSONMap(user)));
        final long count = DB.table.selectWrapper("crete_user_test").setAllowFullQuery(true).count();
        assertEquals(3L, count, "crete_user_test 应该为3");
    }
    @Test
    public void createColumnTest() {
        DB.jdbc.execute("DROP TABLE IF EXISTS crete_user_test2");
        DB.ds.getSqlHelper().createTable("crete_user_test2", User.class);
        final Field vip = FieldReflections.getField(User.class,"vip", true);
        DB.ds.getSqlHelper().createColumn("crete_user_test2", "xxx", vip);
    }
}