package com.dlz.db.modal;

import com.dlz.db.modal.dto.BatchResult;
import com.dlz.db.modal.dto.BatchStatus;
import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.db.modal.wrapper.TableInsert;
import com.dlz.db.modal.wrapper.TableUpdate;
import com.dlz.db.support.DBHolder;
import com.dlz.kit.json.JSONMap;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbBatch {
    public <T> BatchResult insert(List<T> entities) { return insert(entities, 1000); }
    public <T> BatchResult insert(List<T> entities, int batchSize) {
        requireList(entities); requireBatchSize(batchSize);
        if (entities.isEmpty()) return empty(batchSize);
        return executePojo(entities, batchSize, true);
    }
    public <T> BatchResult update(List<T> entities) { return update(entities, 1000); }
    public <T> BatchResult update(List<T> entities, int batchSize) {
        requireList(entities); requireBatchSize(batchSize);
        if (entities.isEmpty()) return empty(batchSize);
        return executePojo(entities, batchSize, false);
    }
    public BatchResult insert(String table, List<?> values) { return insert(table, values, 1000); }
    public BatchResult insert(String table, List<?> values, int batchSize) {
        requireList(values); requireBatchSize(batchSize);
        if (values.isEmpty()) return empty(batchSize);
        List<JSONMap> maps = requireMaps(values);
        return executeTable(table, maps, batchSize, true);
    }
    public BatchResult update(String table, List<?> values) { return update(table, values, 1000); }
    public BatchResult update(String table, List<?> values, int batchSize) {
        requireList(values); requireBatchSize(batchSize);
        if (values.isEmpty()) return empty(batchSize);
        List<JSONMap> maps = requireMaps(values);
        return executeTable(table, maps, batchSize, false);
    }
    public BatchResult execute(String sql, List<Object[]> params) { return execute(sql, params, 1000); }
    public BatchResult execute(String sql, List<Object[]> params, int batchSize) {
        if (sql == null || sql.trim().isEmpty()) throw new DbParameterException("sql must not be empty");
        requireList(params); requireBatchSize(batchSize);
        return executeSql(sql, params, batchSize);
    }
    private <T> BatchResult executePojo(List<T> values, int size, boolean insert) {
        int count = (values.size() + size - 1) / size, completed = 0; Throwable cause = null;
        List<Integer> failed = new ArrayList<>();
        try {
            for (int start = 0; start < values.size(); start += size) {
                int end = Math.min(values.size(), start + size);
                if (insert) new PojoInsert<>((Class<T>) values.get(0).getClass()).batch(values.subList(start, end), end - start);
                else new PojoUpdate<>((Class<T>) values.get(0).getClass()).batch(values.subList(start, end), end - start);
                completed++;
            }
        } catch (Throwable e) { cause = e; failed.add(completed * size); }
        return result(values.size(), size, count, completed, failed, cause);
    }
    private BatchResult executeTable(String table, List<JSONMap> values, int size, boolean insert) {
        int count = (values.size() + size - 1) / size, completed = 0; Throwable cause = null;
        List<Integer> failed = new ArrayList<>();
        try {
            for (int start = 0; start < values.size(); start += size) {
                int end = Math.min(values.size(), start + size);
                if (insert) new TableInsert(table).batch(values.subList(start, end), end - start);
                else new TableUpdate(table).batch(values.subList(start, end), end - start);
                completed++;
            }
        } catch (Throwable e) { cause = e; failed.add(completed * size); }
        return result(values.size(), size, count, completed, failed, cause);
    }
    private BatchResult executeSql(String sql, List<Object[]> values, int size) {
        int count = (values.size() + size - 1) / size, completed = 0; long known = 0; int unknown = 0; List<Integer> failed = new ArrayList<>(); Throwable cause = null;
        for (int start = 0; start < values.size(); start += size) {
            int end = Math.min(values.size(), start + size);
            try {
                int[] counts = DBHolder.getSqlExecutor().batch(sql, new ArrayList<>(values.subList(start, end)));
                for (int i = 0; i < counts.length; i++) {
                    if (counts[i] == Statement.SUCCESS_NO_INFO) unknown++;
                    else if (counts[i] == Statement.EXECUTE_FAILED) failed.add(start + i);
                    else if (counts[i] >= 0) known += counts[i];
                }
                completed++;
                if (!failed.isEmpty()) break;
            } catch (Throwable e) { cause = e; failed.add(start); break; }
        }
        BatchStatus status = failed.isEmpty() ? BatchStatus.SUCCESS : (completed > 0 ? BatchStatus.PARTIAL_FAILURE : BatchStatus.FAILURE);
        return BatchResult.of(values.size(), size, count, completed, known, unknown, failed, status, cause);
    }
    private BatchResult result(int total, int size, int count, int completed, List<Integer> failed, Throwable cause) {
        BatchStatus status = cause == null ? BatchStatus.SUCCESS : (completed > 0 ? BatchStatus.PARTIAL_FAILURE : BatchStatus.FAILURE);
        return BatchResult.of(total, size, count, completed, 0, 0, failed, status, cause);
    }
    private List<JSONMap> requireMaps(List<?> values) {
        List<JSONMap> maps = new ArrayList<>(values.size());
        for (Object value : values) {
            if (!(value instanceof JSONMap)) throw new DbParameterException("table batch values must be JSONMap");
            maps.add((JSONMap) value);
        }
        return maps;
    }
    private BatchResult empty(int size) { return BatchResult.of(0, size, 0, 0, 0, 0, Collections.<Integer>emptyList(), BatchStatus.SUCCESS, null); }
    private void requireList(List<?> list) { if (list == null) throw new DbParameterException("values must not be null"); }
    private void requireBatchSize(int size) { if (size < 1) throw new DbParameterException("batchSize must be greater than zero"); }
}
