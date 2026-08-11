package com.dlz.db.util;

import com.dlz.caller.DlzCaller;
import com.dlz.caller.DlzCallerProperties;
import com.dlz.caller.DlzCallerResolver;
import com.dlz.db.core.DlzDbProperties;
import com.dlz.db.modal.DB;
import com.dlz.kit.fn.DlzFn2;
import com.dlz.kit.mdc.MdcContext;
import com.dlz.kit.util.ExceptionUtils;
import com.dlz.kit.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.List;

@Slf4j
public class DbLogUtil {
    private static boolean showCaller = false;
    private static long slowSqlThreshold = 0;
    private static boolean showRunSql = false;
    private static boolean showResult = false;

    public static void init(DlzDbProperties properties) {
        showCaller = properties.getLog().isShowCaller();
        showRunSql = properties.getLog().isShowRunSql();
        showResult = properties.getLog().isShowResult();
        slowSqlThreshold = properties.getLog().getSlowSqlThreshold();
        if(showCaller){
            final DlzCallerProperties dlzCallerProperties = new DlzCallerProperties();
            dlzCallerProperties.setInjectCallerMdc(true);
            dlzCallerProperties.addIgnoreCallerPackage("com.dlz.db.", "com.dlz.kit.", "com.dlz.spring.");
            DlzCaller.setProperties(dlzCallerProperties);
        }
    }

    private DbLogUtil() {
    }

    public static <T> String generateSqlMessage(Long l, T reulst, String methodName, String sql, Object[] args) {
        final String usedDataSourceName = DB.ds.getUsedDataSourceName();
        if (usedDataSourceName != null) {
            methodName = "[" + usedDataSourceName + "] " + methodName;
        }
        String sqlMessage = showRunSql ?
                StringUtils.formatMsg("{} {}ms sql:{}", methodName, l, SqlUtil.getRunSqlByJdbc(sql, args)) :
                StringUtils.formatMsg("{} {}ms sql:{} {}", methodName, l, sql, args);
        if (showResult && reulst != null) {
            sqlMessage += StringUtils.formatMsg("\nresult:{}", reulst);
        }
        return sqlMessage;
    }

    public static String generateSqlMessage(Long l, String methodName, String sql, List<Object[]> batchArgs) {
        return StringUtils.formatMsg("{} {}ms sql:{} size:{}", methodName, l, sql, batchArgs.size());
    }

    public static <T> void logInfo(DlzFn2<Long, T, String> msg, Long t, T result, Exception error) {
        if (log.isInfoEnabled() || error != null) {
            final long l = System.currentTimeMillis() - t;
            // showCaller 为 true 时才解析调用方并写入 MDC；MdcContext 为 null 时 try-with-resources 自动跳过。
            // try 结束 MdcContext.close() 自动恢复 MDC，无需手动 clearCaller。
            try (final MdcContext ignore = showCaller ? DlzCaller.caller() : null) {
                if (error != null) {
                    log.error(ExceptionUtils.getStackTrace(error));
                    log.error(msg.apply(l, result));
                } else {
                    if (slowSqlThreshold > 0 && l > slowSqlThreshold) {
                        log.warn(msg.apply(l, result));
                    } else {
                        log.info(msg.apply(l, result));
                    }
                }
            }
        }
    }

    public static void warn(String msg, Exception error) {
        if (log.isWarnEnabled() || error != null) {
            try (final MdcContext ignore = showCaller ? DlzCaller.caller() : null) {
                if (error != null) {
                    log.warn(ExceptionUtils.getStackTrace(error));
                }
                log.warn(msg);
            }
        }
    }
    public static void debug(String msg, Exception error) {
        if (log.isDebugEnabled() || error != null) {
            try (final MdcContext ignore = showCaller ? DlzCaller.caller() : null) {
                if (error != null) {
                    log.debug(ExceptionUtils.getStackTrace(error));
                }
                log.debug(msg);
            }
        }
    }
}
