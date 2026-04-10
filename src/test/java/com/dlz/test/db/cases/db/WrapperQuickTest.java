package com.dlz.test.db.cases.db;

import com.dlz.db.ds.DataSourceProperty;
import com.dlz.db.helper.support.HelperScan;
import com.dlz.db.helper.support.SqlHelper;
import com.dlz.db.modal.DB;
import com.dlz.test.db.config.SpingDbBaseTest;
import com.dlz.test.db.entity.SysSql;
import com.dlz.test.db.entity.Yc1Record;
import com.dlz.test.db.entity.YcRecord;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class WrapperQuickTest extends SpingDbBaseTest{

    @Test
    public void insertOrUpdateTest1() {
        SysSql dict = new SysSql();
        dict.setName("xx");
        DB.Pojo.insertOrUpdate(dict);
    }

    @Test
    public void saveTest1() {
        SysSql dict = new SysSql();
        dict.setName("xx");
        DB.Pojo.save(dict);
    }
    @Test
    public void saveTest2() {
        YcRecord dict = new Yc1Record();
        dict.setRe("xx");
        dict.setPcid("xx");
        dict.setSta(1);
        DB.Pojo.save(dict);

        YcRecord yc1Record = new Yc1Record();
        DB.Pojo.insert(yc1Record).execute();
    }
    @Test
    public void updateByIdTest1() {
        SysSql dict = new SysSql();
        dict.setId(1l);
        dict.setName("xx");
        DB.Pojo.updateById(dict);
    }

    @Test
    public void removeByIds1() {
        DB.Pojo.removeByIds(SysSql.class, "1,2,3");
    }

    @Test
    public void getById1() {
        DB.Pojo.getById(SysSql.class, "1");
    }
    @Test
    public void getUseDbById1() {

        final DataSourceProperty properties = new DataSourceProperty();
        properties.setName("test");
        properties.setDriverClassName("org.sqlite.JDBC");
        properties.setUrl("jdbc:sqlite:testdb_dynamic.sqlite3");
        DB.Dynamic.setDataSource(properties);


        DB.Dynamic.use("test",()-> {
            final SqlHelper helper = DB.Dynamic.getSqlHelper();
            HelperScan.initTable(SysSql.class,helper);
            DB.Pojo.getById(SysSql.class, "1");
            DB.Pojo.getById(SysSql.class, "2");
            return null;
        });

        DB.Pojo.getById(SysSql.class, "1");
        DB.Dynamic.use("default",()-> {
            DB.Pojo.getById(SysSql.class, "1");
            DB.Pojo.getById(SysSql.class, "2");
            return null;
        });

    }

}