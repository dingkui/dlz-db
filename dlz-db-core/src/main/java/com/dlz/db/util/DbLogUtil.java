package com.dlz.db.util;

import com.dlz.db.core.BaseDbProperties;
import com.dlz.db.modal.DB;
import com.dlz.kit.fn.DlzFn2;
import com.dlz.kit.util.ExceptionUtils;
import com.dlz.kit.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DbLogUtil {
    private final static String KEY_CALLER = "caller";
    private static boolean showCaller = false;
    private static boolean showRunSql = false;
    private static boolean showResult = false;
    public static void init(BaseDbProperties properties) {
        showCaller = properties.getLog().isShowCaller();
        showRunSql = properties.getLog().isShowRunSql();
        showResult = properties.getLog().isShowResult();
    }

    private DbLogUtil() {
    }

    public static String setCaller(int level) {
        String caller = MDC.get(KEY_CALLER);
        if (caller == null) {
            caller = getTraceCaller(level + 1);
            MDC.put(KEY_CALLER, caller);
        }
        return caller;
    }

    public static void clearCaller() {
        MDC.remove(KEY_CALLER);
    }

    /**
     * 取得调用者
     */
    public static String getTraceCaller(final int level) {
        StackTraceElement[] trace = new Throwable().getStackTrace();
        int index = level;
        if (index < 1) {
            index = 1;
        }
        String traceInfo;
        List<String> frw_trace = new ArrayList<>();
        while (true) {
            if (index > trace.length - 1) {
                return null;
            }
            traceInfo = trace[index].toString();

            if (traceInfo.indexOf("CGLIB$") > -1 ||
                    traceInfo.indexOf("lambda$") > -1 ||
                    traceInfo.startsWith("sun.") ||
                    traceInfo.startsWith("java.") ||
                    traceInfo.startsWith("org.") ||
                    traceInfo.startsWith("com.intellij.junit5")
            ) {
                index++;
                continue;
            }
            if (traceInfo.startsWith("com.dlz.spring.") ||
                    traceInfo.startsWith("com.dlz.kit.")||
                    traceInfo.startsWith("com.dlz.db.")
            ) {
                index++;
                if(log.isTraceEnabled()){
                    frw_trace.add(traceInfo.replaceAll(".*\\((.*)\\)", "$1").replaceAll("\\.java", ""));
                }
                continue;
            }
            break;
        }
//		String[] split = traceInfo.split("\\.");
//		if(split.length>3){
//			for (int i = 0; i < split.length-3 ; i++) {
//				split[i]=split[i].substring(0,1);
//			}
//		}
        if(!frw_trace.isEmpty()){
            log.trace("< {}", frw_trace.stream().collect(Collectors.joining(" < ")));
        }
        return traceInfo.replaceAll(".*\\((.*)\\)", " caller:($1)");
    }

    public static <T> String generateSqlMessage(Long t, T reulst, String methodName, String sql, Object[] args) {
        final long l = System.currentTimeMillis() - t;
        final String usedDataSourceName = DB.Dynamic.getUsedDataSourceName();
        if(usedDataSourceName!=null){
            methodName = "["+usedDataSourceName+"] "+methodName;
        }
        String sqlMessage = showRunSql ?
                StringUtils.formatMsg("{} {}ms sql:{}", methodName, l, SqlUtil.getRunSqlByJdbc(sql, args)) :
                StringUtils.formatMsg("{} {}ms sql:{} {}", methodName, l, sql, args);
        if(showResult && reulst!=null){
            sqlMessage+=StringUtils.formatMsg("\nresult:{}", reulst);
        }
        return sqlMessage;
    }

    public static String generateSqlMessage(Long t, String methodName, String sql, List<Object[]> batchArgs) {
        final long l = System.currentTimeMillis() - t;
        return StringUtils.formatMsg("{} {}ms sql:{} size:{}", methodName, l, sql, batchArgs.size());
    }

    public static <T> void logInfo(DlzFn2<Long, T, String> msg, Long t, T result, Exception error) {
        if (log.isInfoEnabled()||error != null) {
            if (showCaller) {
                DbLogUtil.setCaller(1);
            }
            try {
                if (error != null) {
                    log.error(ExceptionUtils.getStackTrace(error));
                    log.error(msg.apply(t, result));
                } else {
                    log.info(msg.apply(t, result));
                }
            } finally {
                if (showCaller) {
                    DbLogUtil.clearCaller();
                }
            }
        }
    }
}
