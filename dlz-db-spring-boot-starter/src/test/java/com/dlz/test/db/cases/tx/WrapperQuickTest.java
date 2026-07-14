package com.dlz.test.db.cases.tx;

import com.dlz.db.ds.DataSourceProperty;
import com.dlz.db.modal.DB;
import com.dlz.db.support.helper.HelperScan;
import com.dlz.db.support.helper.SqlHelper;
import com.dlz.test.config.BaseDBTest;
import com.dlz.test.db.entity.SysSql;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class WrapperQuickTest extends BaseDBTest {

    @Test
    public void getUseDbById1() {
        final DataSourceProperty properties = new DataSourceProperty();
        properties.setName("test");
        properties.setDriverClassName("org.sqlite.JDBC");
        properties.setUrl("jdbc:sqlite:./test/testdb_dynamic.sqlite3");
        DB.ds.setDataSource(properties);

        DB.ds.use("test",()-> {
            final SqlHelper helper = DB.ds.getSqlHelper();
            HelperScan.initTable(SysSql.class,helper);
            DB.pojo.selectById(SysSql.class, "1");
            DB.pojo.selectById(SysSql.class, "2");
            return null;
        });

        DB.pojo.selectById(SysSql.class, "1");
        DB.ds.use("default",()-> {
            DB.pojo.selectById(SysSql.class, "1");
            DB.pojo.selectById(SysSql.class, "2");
            return null;
        });

    }

}