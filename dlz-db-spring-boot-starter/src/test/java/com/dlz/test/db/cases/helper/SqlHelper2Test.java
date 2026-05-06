package com.dlz.test.db.cases.helper;

import com.dlz.db.helper.bean.TableInfo;
import com.dlz.db.modal.DB;
import com.dlz.test.db.config.SpingDbBaseTest;
import org.junit.Test;


public class SqlHelper2Test extends SpingDbBaseTest {

    @Test
    public void lamdaTest2() {
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
//        dataSource.setUrl("jdbc:mysql://192.168.1.126:3306/elec");
//        dataSource.setUsername("root");
//        dataSource.setPassword("1234qwer");
//
//        SqlHolder.init(new DlzDbProperties());
//        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
//        DlzDao dao = new DlzDao(jdbcTemplate);
//        DbOpMysql dbOpMysql = new DbOpMysql(dao);

        TableInfo sys_test = DB.Dynamic.getSqlHelper().getTableInfo("user");
        System.out.println(sys_test);
    }
}