package com.dlz.test.db.service;

import com.dlz.db.modal.DB;
import com.dlz.test.db.entity.User;
import lombok.RequiredArgsConstructor;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Tran;

import java.util.List;

/**
 * Solon @Tran 声明式事务测试 Service
 */
@Component
@RequiredArgsConstructor
public class SolonTxTestService {

    @Tran
    public void testCommit() {
        insertUser("事务用户1", 25, "tx1@example.com");
        insertUser("事务用户2", 30, "tx2@example.com");
    }

    @Tran
    public void testRollbackWithException() {
        insertUser("回滚用户1", 25, "rb1@example.com");
        insertUser("回滚用户2", 30, "rb2@example.com");
        throw new RuntimeException("强制回滚");
    }

    @Tran
    public void testRollbackManual() {
        insertUser("手动回滚用户1", 25, "mr1@example.com");
        throw new RuntimeException("条件触发回滚");
    }

    @Tran
    public void testNestedTransactionSuccess() {
        insertUser("外层用户", 25, "outer@example.com");
        testCommitInner();
    }

    @Tran
    public void testNestedTransactionInnerFail() {
        insertUser("外层用户-内层失败", 25, "outer_fail@example.com");
        try {
            testRollbackWithException();
        } catch (RuntimeException e) {
            // 外层捕获异常，但事务已标记回滚
        }
    }

    @Tran
    public void testNestedTransactionOuterFail() {
        insertUser("外层用户-外层失败", 25, "outer_fail2@example.com");
        testCommitInner();
        throw new RuntimeException("外层失败");
    }

    @Tran
    public Long testTransactionWithReturn() {
        User user = new User();
        user.setName("返回值测试用户");
        user.setAge(25);
        user.setEmail("ret@example.com");
        DB.Pojo.insert(user);
        return user.getId();
    }

    @Tran
    public void testBatchOperation() {
        for (int i = 0; i < 10; i++) {
            insertUser("批量用户" + i, 20 + i, "batch" + i + "@example.com");
        }
    }

    @Tran
    public List<User> testReadOnlyQuery(String namePrefix) {
        return DB.Pojo.selectW(User.class)
                .like(User::getName, namePrefix)
                .queryBeanList();
    }

    @Tran
    public void testUpdateTransaction(Long userId, String newName, int newAge) {
        DB.Pojo.updateW(User.class)
                .set(User::getName, newName)
                .set(User::getAge, newAge)
                .eq(User::getId, userId)
                .execute();
    }

    @Tran
    public void testDeleteTransaction(Long userId) {
        DB.Pojo.deleteW(User.class)
                .eq(User::getId, userId)
                .execute();
    }

    @Tran
    public void testTransfer(String fromName, String toName, int amount) {
        User fromUser = DB.Pojo.selectW(User.class)
                .eq(User::getName, fromName)
                .queryBean();
        User toUser = DB.Pojo.selectW(User.class)
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

        DB.Pojo.updateW(User.class)
                .set(User::getAge, fromUser.getAge() - amount)
                .eq(User::getName, fromName)
                .execute();

        DB.Pojo.updateW(User.class)
                .set(User::getAge, toUser.getAge() + amount)
                .eq(User::getName, toName)
                .execute();
    }

    @Tran
    void testCommitInner() {
        insertUser("内层用户", 28, "inner@example.com");
    }

    private void insertUser(String name, int age, String email) {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        DB.Pojo.insert(user);
    }
}
