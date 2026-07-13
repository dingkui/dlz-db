package com.dlz.test.db.service;

import com.dlz.db.modal.DB;
import com.dlz.test.db.entity.User;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Tran;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Solon 原生事务 + dlz-db API 测试服务
 * 验证 Solon @Tran 能否正确管理 dlz-db 的操作
 */
@Component
public class SolonNativeTxService {

    private final DataSource dataSource;

    public SolonNativeTxService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 使用 Solon @Tran + dlz-db API 插入数据并提交
     */
    @Tran
    public void insertWithDlzDbAndCommit() {
        User user = new User();
        user.setName("Solon事务包含DB提交用户");
        user.setAge(25);
        user.setEmail("solon_dlz_commit@example.com");
        DB.Pojo.add(user);
    }

    /**
     * 使用 Solon @Tran + dlz-db API 插入数据并抛出异常回滚
     */
    @Tran
    public void insertWithDlzDbAndRollback() {
        User user1 = new User();
        user1.setName("Solon事务包含DB回滚用户1");
        user1.setAge(30);
        user1.setEmail("solon_dlz_rollback1@example.com");
        DB.Pojo.add(user1);

        User user2 = new User();
        user2.setName("Solon事务包含DB回滚用户2");
        user2.setAge(35);
        user2.setEmail("solon_dlz_rollback2@example.com");
        DB.Pojo.add(user2);

        throw new RuntimeException("Solon 事务包含 dlz-db 操作回滚测试");
    }

    /**
     * 查询记录数
     */
    public int countByName(String name) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM USER WHERE name LIKE ?")) {
            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
