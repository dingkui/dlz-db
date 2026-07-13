package com.dlz.test.db.service;

import com.dlz.db.modal.DB;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 事务测试服务类
 * <p>
 * 提供带有 @Transactional 注解的 Spring 事务方法，用于测试 Spring 事务与 dlz-db 事务的集成。
 * 该服务类封装了数据库操作逻辑，支持不同的异常场景以测试事务回滚行为。
 * </p>
 *
 * @author dlz-db-test
 */
@Service
public class TransactionTestService {

    /**
     * 在 Spring 事务中插入用户数据
     * <p>
     * 使用 @Transactional 注解启用 Spring 声明式事务。
     * 该方法会在 tx_test_user 表中插入一条记录。
     * </p>
     *
     * @param id   用户ID
     * @param name 用户名称
     * @param age  用户年龄
     */
    @Transactional
    public void insertUserInSpringTx(int id, String name, int age) {
        DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)",
                id, name, age);
    }

    /**
     * 在 Spring 事务中插入用户数据并抛出异常
     * <p>
     * 该方法在插入数据后会抛出 RuntimeException，用于测试 Spring 事务的回滚机制。
     * 由于 @Transactional 默认会回滚 RuntimeException，插入的数据应该被回滚。
     * </p>
     *
     * @param id   用户ID
     * @param name 用户名称
     * @param age  用户年龄
     * @throws RuntimeException 模拟业务异常
     */
    @Transactional
    public void insertUserInSpringTxWithException(int id, String name, int age) {
        DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)",
                id, name, age);
        throw new RuntimeException("Spring transaction exception");
    }

    /**
     * 在 Spring 事务中更新用户年龄
     * <p>
     * 使用 @Transactional 注解启用 Spring 声明式事务。
     * 该方法会更新指定 id 用户的年龄字段。
     * </p>
     *
     * @param id     用户ID
     * @param newAge 新的年龄值
     */
    @Transactional
    public void updateUserInSpringTx(int id, int newAge) {
        DB.Jdbc.execute("UPDATE tx_test_user SET age = ? WHERE id = ?", newAge, id);
    }

    /**
     * 在 Spring 事务中插入用户数据并抛出 Checked Exception
     * <p>
     * 该方法使用 @Transactional(rollbackFor = Exception.class) 配置，
     * 确保 Checked Exception 也会触发事务回滚。
     * 默认情况下，@Transactional 只回滚 RuntimeException 和 Error，
     * 不会回滚 Checked Exception，因此需要显式配置 rollbackFor。
     * </p>
     *
     * @param id   用户ID
     * @param name 用户名称
     * @param age  用户年龄
     * @throws Exception 模拟 Checked Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertUserWithCheckedException(int id, String name, int age) throws Exception {
        DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)",
                id, name, age);
        throw new Exception("Checked exception");
    }

    /**
     * Spring 外层事务包含 DLZ 内层事务 - 内层异常导致两层回滚
     */
    @Transactional
    public void springContainsDlzInnerExceptionBothRollback() {
        DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 1, "spring_outer", 30);
        DB.Tx.run(() -> {
            DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 2, "dlz_inner", 25);
            throw new RuntimeException("DLZ inner exception");
        });
    }

    /**
     * Spring 外层事务包含 DLZ 内层事务 - 外层异常导致两层回滚
     */
    @Transactional
    public void springContainsDlzOuterExceptionBothRollback() {
        DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 1, "spring_outer", 30);
        DB.Tx.run(() -> {
            DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 2, "dlz_inner", 25);
        });
        throw new RuntimeException("Spring outer exception");
    }

    /**
     * Spring 外层事务包含 DLZ 内层事务 - 无异常时两层都提交
     */
    @Transactional
    public void springContainsDlzNoExceptionBothCommit() {
        DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 1, "spring_outer", 30);
        DB.Tx.run(() -> {
            DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 2, "dlz_inner", 25);
        });
    }
}
