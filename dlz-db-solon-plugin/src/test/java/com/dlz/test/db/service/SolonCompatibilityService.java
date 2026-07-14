package com.dlz.test.db.service;

import com.dlz.db.modal.DB;
import com.dlz.test.db.entity.User;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.util.List;

/**
 * Solon @Tran 与 DLZ-DB 兼容性测试 Service
 */
@Component
public class SolonCompatibilityService {

    @Inject
    private SolonCompatibilityService self;

    // ==================== Solon @Tran + DLZ-DB API ====================

    @Tran
    public void solonTranWithDlzDb() {
        insertUser("Solon事务用户1", 25, "s1@example.com");
        insertUser("Solon事务用户2", 30, "s2@example.com");
    }

    @Tran
    public void solonTranRollback() {
        insertUser("Solon回滚用户1", 25, "sr1@example.com");
        insertUser("Solon回滚用户2", 30, "sr2@example.com");
        throw new RuntimeException("Solon 事务回滚测试");
    }

    @Tran
    public List<User> solonTranQuery(String namePrefix) {
        return DB.pojo.selectWrapper(User.class)
                .like(User::getName, namePrefix)
                .queryBeanList();
    }

    @Tran
    public void solonTranUpdate(Long userId, String newName) {
        DB.pojo.updateWrapper(User.class)
                .set(User::getName, newName)
                .eq(User::getId, userId)
                .execute();
    }

    @Tran
    public void solonTranDelete(Long userId) {
        DB.pojo.deleteWrapper(User.class)
                .eq(User::getId, userId)
                .execute();
    }

    // ==================== Solon @Tran + DLZ-DB.Tx.run() ====================

    @Tran
    public void solonTranWithDlzNestedTx() {
        insertUser("Solon外层用户", 25, "outer@example.com");
        DB.tx.run(() -> insertUser("DLZ内层用户", 28, "inner@example.com"));
    }

    @Tran
    public void solonTranFailRollbackDlz() {
        insertUser("Solon用户-会回滚", 25, "s_rb@example.com");
        DB.tx.run(() -> insertUser("DLZ用户-也会回滚", 28, "d_rb@example.com"));
        throw new RuntimeException("Solon 外层异常，整体回滚");
    }

    // ==================== DLZ-DB.Tx.run() + Solon Service ====================

    public void dlzTxCallSolonMethod() {
        DB.tx.run(() -> {
            insertUser("DLZ调用用户1", 25, "dc1@example.com");
            self.solonMethodInner();
        });
    }

    public void dlzTxCallSolonTranMethod() {
        DB.tx.run(() -> {
            insertUser("DLZ外层用户", 25, "d_outer@example.com");
            self.solonTranMethodInner();
        });
    }

    public void solonMethodInner() {
        insertUser("DLZ调用用户2", 30, "dc2@example.com");
    }

    @Tran
    public void solonTranMethodInner() {
        insertUser("@Transaction用户", 28, "tran_inner@example.com");
    }

    // ==================== 转账业务 ====================

    @Tran
    public void transferBySolonTran(String fromName, String toName, int amount) {
        doTransfer(fromName, toName, amount);
    }

    public void transferByDlzTx(String fromName, String toName, int amount) {
        DB.tx.run(() -> doTransfer(fromName, toName, amount));
    }

    private void doTransfer(String fromName, String toName, int amount) {
        User fromUser = DB.pojo.selectWrapper(User.class)
                .eq(User::getName, fromName)
                .queryBean();
        User toUser = DB.pojo.selectWrapper(User.class)
                .eq(User::getName, toName)
                .queryBean();

        if (fromUser == null || toUser == null) {
            throw new RuntimeException("账户不存在");
        }
        if (fromUser.getAge() < amount) {
            throw new RuntimeException("余额不足");
        }
        if (amount > 100) {
            throw new RuntimeException("单笔转账金额不能超过100");
        }

        DB.pojo.updateWrapper(User.class)
                .set(User::getAge, fromUser.getAge() - amount)
                .eq(User::getName, fromName)
                .execute();

        DB.pojo.updateWrapper(User.class)
                .set(User::getAge, toUser.getAge() + amount)
                .eq(User::getName, toName)
                .execute();
    }

    // ==================== 批量操作 ====================

    @Tran
    public void batchInsertBySolonTran(int count) {
        for (int i = 0; i < count; i++) {
            insertUser("Solon批量用户" + i, 20 + i, "sb" + i + "@example.com");
        }
    }

    public void batchInsertByDlzTx(int count) {
        DB.tx.run(() -> {
            for (int i = 0; i < count; i++) {
                insertUser("DLZ批量用户" + i, 20 + i, "db" + i + "@example.com");
            }
        });
    }

    private void insertUser(String name, int age, String email) {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        DB.pojo.add(user);
    }
}
