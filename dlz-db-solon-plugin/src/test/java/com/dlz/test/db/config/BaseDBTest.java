package com.dlz.test.db.config;

import com.dlz.db.inf.ISqlPara;
import com.dlz.db.modal.items.JdbcItem;
import com.dlz.db.modal.para.AParaPojo;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.util.id.TraceUtil;
import com.dlz.test.db.Starter;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.noear.solon.Solon;

/**
 * Solon 版测试基类（与 Spring 模块下同名 FQN，保持 case 文件源码兼容）。
 * <p>同时支持 JUnit 4 和 JUnit 5：</p>
 * <ul>
 *   <li>JUnit 4: 使用 {@link BeforeClass}</li>
 *   <li>JUnit 5: 使用 {@link BeforeAll}</li>
 * </ul>
 * <p>启动 Solon 应用一次（幂等），等价于 Spring 的
 * {@code @RunWith(SpringRunner.class) + @SpringBootTest}。</p>
 */
@Slf4j
public class BaseDBTest {

    /**
     * JUnit 5 初始化入口
     */
    @BeforeAll
    public static void bootstrapJunit5() {
        bootstrap();
    }

    /**
     * JUnit 4 初始化入口
     */
    @BeforeClass
    public static void bootstrapJunit4() {
        bootstrap();
    }

    /**
     * 统一的初始化逻辑（幂等）
     */
    private static void bootstrap() {
        if (Solon.app() == null) {
            TraceUtil.setTraceId("bootstrap");
            Solon.start(Starter.class, new String[0]);
            // 在这里添加其他全局初始化逻辑
            // 例如：初始化测试数据、清理环境等
            log.info("Solon 应用启动完成，全局初始化完毕");
            TraceUtil.clearTraceId();
        }
    }

    @BeforeEach
    public void beforeBase() {
        TraceUtil.setTraceId(this.getClass().getSimpleName());
    }

    @AfterEach
    public void afterBase() {
        TraceUtil.clearTraceId();
    }
}
