package com.dlz.test.db.config;

import com.dlz.kit.mdc.DlzTrace;
import com.dlz.kit.mdc.MdcContext;
import com.dlz.test.db.Starter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.noear.solon.Solon;

/**
 * Solon 版测试基类（与 Spring 模块下同名 FQN，保持 case 文件源码兼容）。
 * <p>基于 JUnit 5（JUnit Jupiter），启动 Solon 应用一次（幂等）。</p>
 */
@Slf4j
public class BaseDBTest {

    private MdcContext mdcCtx;

    /**
     * 初始化入口（幂等）
     */
    @BeforeAll
    public static void bootstrap() {
        if (Solon.app() == null) {
            try (MdcContext ignore = DlzTrace.trace("bootstrap")) {
                Solon.start(Starter.class, new String[0]);
                // 在这里添加其他全局初始化逻辑
                // 例如：初始化测试数据、清理环境等
                log.info("Solon 应用启动完成，全局初始化完毕");
            }
        }
    }

    @BeforeEach
    public void beforeBase() {
        mdcCtx = DlzTrace.trace(this.getClass().getSimpleName());
    }

    @AfterEach
    public void afterBase() {
        if (mdcCtx != null) {
            mdcCtx.close();
            mdcCtx = null;
        }
    }
}
