package com.dlz.db.modal;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.dto.BatchResult;
import com.dlz.db.modal.dto.BatchStatus;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.db.modal.wrapper.TableInsert;
import com.dlz.db.modal.wrapper.TableUpdate;
import com.dlz.db.support.DBHolder;
import com.dlz.kit.json.JSONMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbBatch {
    public <T> BatchResult insert(List<T> entities) {
        return insert(entities, 1000);
    }

    public <T> BatchResult insert(List<T> entities, int batchSize) {
        RequireUtil.requireList(entities);
        RequireUtil.requireBatchSize(batchSize);
        if (entities.isEmpty()) return empty(batchSize);
        return executePojo(entities, batchSize, true);
    }

    public <T> BatchResult update(List<T> entities) {
        return update(entities, 1000);
    }

    public <T> BatchResult update(List<T> entities, int batchSize) {
        RequireUtil.requireList(entities);
        RequireUtil.requireBatchSize(batchSize);
        if (entities.isEmpty()) return empty(batchSize);
        return executePojo(entities, batchSize, false);
    }

    public BatchResult insert(String table, List<?> values) {
        return insert(table, values, 1000);
    }

    public BatchResult insert(String table, List<?> values, int batchSize) {
        RequireUtil.requireList(values);
        RequireUtil.requireBatchSize(batchSize);
        if (values.isEmpty()) return empty(batchSize);
        List<JSONMap> maps = RequireUtil.requireMaps(values);
        return executeTable(table, maps, batchSize, true);
    }

    public BatchResult update(String table, List<?> values) {
        return update(table, values, 1000);
    }

    public BatchResult update(String table, List<?> values, int batchSize) {
        RequireUtil.requireList(values);
        RequireUtil.requireBatchSize(batchSize);
        if (values.isEmpty()) return empty(batchSize);
        List<JSONMap> maps = RequireUtil.requireMaps(values);
        return executeTable(table, maps, batchSize, false);
    }

    public BatchResult execute(String sql, List<Object[]> params) {
        return execute(sql, params, 1000);
    }

    public BatchResult execute(String sql, List<Object[]> params, int batchSize) {
        RequireUtil.requireJdbcSql(sql);
        RequireUtil.requireList(params);
        RequireUtil.requireBatchSize(batchSize);
        return executeSql(sql, params, batchSize);
    }

    @SuppressWarnings("unchecked")
    private <T> BatchResult executePojo(List<T> values, int batchSize, boolean insert) {
        Class<T> type = (Class<T>) values.get(0).getClass();
        return insert
                ? new PojoInsert<>(type).batch(values, batchSize)
                : new PojoUpdate<>(type).batch(values, batchSize);
    }

    private BatchResult executeTable(String table, List<JSONMap> values, int batchSize, boolean insert) {
        return insert
                ? new TableInsert(table).batch(values, batchSize)
                : new TableUpdate(table).batch(values, batchSize);
    }

    private BatchResult executeSql(String sql, List<Object[]> values, int batchSize) {
        BatchResult.Builder result = BatchResult.builder(values.size(), batchSize);
        for (int start = 0; start < values.size(); start += batchSize) {
            int end = Math.min(values.size(), start + batchSize);
            try {
                int[] counts = DBHolder.getSqlExecutor().batch(
                        sql,
                        new ArrayList<>(values.subList(start, end))
                );
                result.completed(start, counts);
                if (result.hasFailure()) break;
            } catch (Throwable cause) {
                result.failed(start, cause);
                break;
            }
        }
        return result.build();
    }

    private BatchResult empty(int size) {
        return BatchResult.of(0, size, 0, 0, 0, 0, Collections.<Integer>emptyList(), BatchStatus.SUCCESS, null);
    }
}
