package com.dlz.test.db.config;

import com.dlz.kit.mdc.DlzTrace;
import com.dlz.kit.mdc.MdcContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;

/**
 * 测试方法环绕 trace 扩展（基于 JUnit 5 {@link InvocationInterceptor}）。
 *
 * <p>在测试方法执行前开启 traceId，方法体执行后自动恢复/清除，配对成对，
 * 无需手工 {@code @BeforeEach}/{@code @AfterEach} 设置清理，也避免手工清理遗漏。
 * 通过 {@code try-with-resources} 保证方法抛异常时 traceId 也会被正确清理。</p>
 *
 * <p>用法：在测试基类或测试类上标注 {@code @ExtendWith(TraceExtension.class)}。</p>
 */
public class TraceExtension implements InvocationInterceptor {
    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        String traceName = invocationContext.getTargetClass().getSimpleName();
        // 环绕：方法体执行前开 trace，结束后自动恢复/清除
        try (MdcContext ignored = DlzTrace.trace(traceName)) {
            invocation.proceed();
        }
    }
}
