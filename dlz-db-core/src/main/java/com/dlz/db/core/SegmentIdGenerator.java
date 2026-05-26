package com.dlz.db.core;

import com.dlz.db.exception.DbException;
import com.dlz.db.modal.DB;
import com.dlz.db.support.DBHolder;
import com.dlz.kit.exception.SystemException;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于数据库号段模式的分布式 ID 生成器，支持零浪费拼接。
 *
 * <p>核心设计：
 * <ul>
 *   <li><b>乐观锁 CAS</b> — WHERE max_id = ? 条件避免多实例并发冲突</li>
 *   <li><b>零浪费拼接</b> — 本地号段不足时，旧号段剩余与新号段首部连续拼接，不丢弃</li>
 *   <li><b>自动建表 + 自愈</b> — 首次使用或表丢失时自动创建 table_id_segment，并从业务表 MAX(id) 接续</li>
 *   <li><b>多数据源跟随</b> — k 仅用 tableName，数据源切换自动路由到对应库</li>
 * </ul>
 */
@Slf4j
public class SegmentIdGenerator {

    private final ConcurrentHashMap<String, SegmentBuffer> bufferMap = new ConcurrentHashMap<>();
    private final int defaultStep;

    public SegmentIdGenerator(int defaultStep) {
        this.defaultStep = defaultStep;
    }

    /**
     * 批量获取连续 ID 的终止值（Max ID）。
     * <p>
     * 调用方可安全使用 {@code [return - count + 1, return]} 范围内的所有 ID。
     *
     * @param tableName 业务表名
     * @param count     需要的连续 ID 数量
     * @return 这批连续 ID 的最后一个值
     */
    public long nextId(String tableName, long count) {
        if (count <= 0) count = 1;
        // 使用 dataSourceId:tableName 作为复合 Key，确保多数据源隔离
        String key = DB.Dynamic.getCurrentConfig().getName() + ":" + tableName;
        SegmentBuffer buffer = bufferMap.computeIfAbsent(key, k -> new SegmentBuffer(tableName, defaultStep));
        return buffer.nextIds(count);
    }

    /**
     * 每个表的号段缓冲区。
     * <p>
     * 维护当前表在内存中的号段范围 [current, max]，以及数据库交互逻辑。
     */
    private class SegmentBuffer {
        private final String tableName;
        private final int step;
        private volatile long max;
        private AtomicLong current;

        SegmentBuffer(String tableName, int step) {
            this.tableName = tableName;
            this.step = step;
            long base = fetchFromDb(step);
            this.max = base + step;
            this.current = new AtomicLong(base + 1);
        }

        // ==================== 数据库操作 ====================

        private int update(String sql, Object... args) {
            return DBHolder.getSqlExecutor().doDb(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    NativeSqlUtil.bindArgs(ps, args);
                    return ps.executeUpdate();
                }
            }, null);
        }

        private Long getId(String sql, Object... args) {
            return DBHolder.getSqlExecutor().doDb(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    NativeSqlUtil.bindArgs(ps, args);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getLong(1); // 修正：JDBC 索引从 1 开始
                        }
                    }
                    return null;
                }
            }, null);
        }

        private Long getTableMaxId() {
            try {
                final Long id = getId("SELECT MAX(id) FROM `" + tableName + "`");
                if (id != null) {
                    log.info("Real MAX(id) of table {} is {}", tableName, id);
                } else {
                    log.info("Table {} has no records", tableName);
                }
                return id;
            } catch (Exception e) {
                if (isNoSuchTableError(e)) {
                    return 0l;
                } else {
                    log.error("Failed to getTableMaxId for table {}", tableName);
                }
                return 0l;
            }
        }


        /**
         * 乐观锁（CAS）方式从数据库获取号段基准值。
         *
         * <p>优先使用内存缓存的 {@link #max} 做 CAS（一次 DB 操作），仅在冲突或记录不存在时回退到 SELECT + 重试。
         *
         * <p>流程：
         * <ol>
         *   <li><b>乐观路径</b>：UPDATE ... WHERE max_id = #{this.max}（一事务）</li>
         *   <li>成功（rows=1）→ 返回 this.max，完成</li>
         *   <li><b>悲观路径</b>：rows=0 → SELECT 当前值</li>
         *   <li>SELECT=null → INSERT 初始记录；有值 → CAS 重试</li>
         *   <li>表不存在异常 → initTableSegment()</li>
         * </ol>
         */
        private long fetchFromDb(int step) {
            int retries = 0;
            int maxRetries = 5;
            while (true) {
                try {
                    // ========== 乐观路径：本地有缓存才直接用 max CAS ==========
                    int rows = 0;
                    if (this.max > 0) {
                        String sql = "UPDATE sys_seq SET id = id + ? WHERE k = ? AND id = ?";
                        rows = update(sql, step, tableName, this.max);
                        if (rows == 1) {
                            long oldMax = this.max;
                            this.max += step; // 关键：同步更新内存中的边界
                            return oldMax;
                        }
                        if (rows > 1) {
                            throw new SystemException("CAS conflict for table " + tableName);
                        }
                    }

                    // ========== 悲观路径：本地无缓存 / 冲突 / 记录不存在 ==========
                    Long currentMax = getId("SELECT id FROM sys_seq WHERE k = ?", tableName);

                    if (currentMax == null) {
                        // 记录不存在 → 插入初始记录
                        try {
                            Long maxId = getTableMaxId();
                            maxId = maxId == null ? 0 : maxId;
                            this.max = maxId + step;
                            // 插入时，id 应该代表当前已使用的最大值
                            update("INSERT INTO sys_seq (k, id) VALUES (?, ?)", tableName, this.max);
                            return maxId;
                        } catch (Exception e) {
                            log.debug("Insert conflict for table {}, retrying...", tableName);
                        }
                    } else {
                        // 记录存在但缓存已过时 → 用真实值 CAS 重试
                        rows = update("UPDATE sys_seq SET id = id + ? WHERE k = ? AND id = ?", step, tableName, currentMax);
                        if (rows == 1) {
                            this.max = currentMax + step;
                            return currentMax;
                        }
                        log.debug("CAS conflict for table {} (currentMax={}), retrying...", tableName, currentMax);
                    }

                    retries++;
                    if (retries < maxRetries) {
                        Thread.sleep(10 * retries);
                    } else {
                        throw new DbException("Failed to fetch segment for table " + tableName +
                                " after " + maxRetries + " retries", 1004);
                    }

                } catch (Exception e) {
                    if (isNoSuchTableError(e)) {
                        initTableSegment();
                    } else {
                        log.error("Failed to fetch segment for table {}", tableName, e);
                        if (e instanceof DbException) {
                            throw (DbException) e;
                        }
                    }

                    retries++;
                    if (retries < maxRetries) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }

        private boolean isNoSuchTableError(Exception e) {
            final String message = e.getMessage();
            return message != null &&
                    (message.contains("no such table") || message.contains("doesn't exist"));
        }

        /**
         * 创建 table_id_segment 表，并从业务表当前 MAX(id) 接续，避免新建表后从 0 开始导致 ID 碰撞。
         */
        private void initTableSegment() {
            log.info("Creating {}sys_seq and initializing for table {}...", tableName);

            // 1. 建表（幂等）
            String ddl = "CREATE TABLE IF NOT EXISTS sys_seq (k VARCHAR(64) PRIMARY KEY, id BIGINT NOT NULL DEFAULT 0, step BIGINT NOT NULL DEFAULT 1000)";
            update(ddl);

            // 2. 取业务表实际 MAX(id)，使新号段从现有数据之后开始
            long realMax = 0;
            Long maxId = getTableMaxId();
            if (maxId != null) realMax = maxId;

            // 3. 插入初始记录（已存在则忽略）
            try {
                update("INSERT INTO sys_seq (k, id) VALUES (?, ?)", tableName, realMax);
            } catch (Exception e) {
                log.debug("Initial record for table {} already exists, skipped", tableName);
            }
        }

        // ==================== ID 分发 ====================

        /**
         * 批量获取 N 个连续 ID 的终止值。
         * <p>
         * <b>本地充足</b>：直接切分，零数据库开销。<br>
         * <b>本地不足 + DB 连续性成立</b>（oldMaxFromDb == max）：零浪费拼接。
         * 旧号段剩余 [cur, max] + 新号段 [max+1, newMax] 自然连续，无 ID 浪费。<br>
         * <b>本地不足 + DB 连续性被破坏</b>（如 table_id_segment 记录被外部重置）：
         * 放弃旧号段残余，全部从新号段分配。
         */
        synchronized long nextIds(long count) {
            long cur = current.get();
            long remaining = max - cur + 1;

            if (remaining >= count) {
                current.addAndGet(count);
                return cur + count - 1;
            }

            int loadStep = (int) Math.max(this.step, count);
            long oldMaxFromDb = fetchFromDb(loadStep);
            cur = oldMaxFromDb + 1;
            this.current = new AtomicLong(cur);
            current.addAndGet(count);
            return cur + count - 1;
        }
    }
}
