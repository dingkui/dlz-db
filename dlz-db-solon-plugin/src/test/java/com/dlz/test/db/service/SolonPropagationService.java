package com.dlz.test.db.service;

import com.dlz.db.modal.DB;
import com.dlz.test.db.entity.User;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

/**
 * Solon @Tran 与 DLZ-DB 事务传播性测试 Service
 */
@Component
public class SolonPropagationService {

    @Inject
    private SolonPropagationService self;

    // ==================== @Tran 外层 + DB.Tx.run() 内层 ====================

    @Tran
    public void tranOuterDlzInner() {
        insertUser("Tran外层用户", 25, "to@example.com");
        DB.Tx.run(() -> insertUser("DLZ内层用户", 28, "di@example.com"));
    }

    @Tran
    public void tranOuterDlzInnerRollback() {
        insertUser("Tran外层-回滚", 25, "to_rb@example.com");
        DB.Tx.run(() -> {
            insertUser("DLZ内层-回滚", 28, "di_rb@example.com");
            throw new RuntimeException("内层异常，整体回滚");
        });
    }

    // ==================== DB.Tx.run() 外层 + @Tran 内层 ====================

    public void dlzOuterTranInner() {
        DB.Tx.run(() -> {
            insertUser("DLZ外层用户", 25, "do@example.com");
            self.tranInnerSimple();
        });
    }

    public void dlzOuterTranInnerRollback() {
        DB.Tx.run(() -> {
            insertUser("DLZ外层-回滚", 25, "do_rb@example.com");
            self.tranInnerRollback();
        });
    }

    @Tran
    public void tranInnerSimple() {
        insertUser("@Tran内层用户", 28, "ti@example.com");
    }

    @Tran
    public void tranInnerRollback() {
        insertUser("@Tran内层-回滚", 28, "ti_rb@example.com");
        throw new RuntimeException("@Tran 内层异常，整体回滚");
    }

    // ==================== 嵌套传播 ====================

    @Tran
    public void tranOuterWithNestedDlzAndTran() {
        insertUser("最外层-Tran", 25, "outermost@example.com");
        DB.Tx.run(() -> {
            insertUser("中间层-DLZ", 28, "middle@example.com");
            self.tranInnerMost();
        });
    }

    @Tran
    public void tranInnerMost() {
        insertUser("最内层-Tran", 30, "innermost@example.com");
    }

    // ==================== 双向异常回滚 ====================

    @Tran
    public void tranOuterFailRollbackDlzInner() {
        insertUser("Tran外层-失败", 25, "to_fail@example.com");
        DB.Tx.run(() -> insertUser("DLZ内层-被回滚", 28, "di_fail@example.com"));
        throw new RuntimeException("Tran 外层异常，DLZ 内层也应回滚");
    }

    private void insertUser(String name, int age, String email) {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        DB.Pojo.insert(user);
    }
}
