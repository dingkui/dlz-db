package com.dlz.db.modal.dto;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BatchResult {
    private final int totalItems;
    private final int batchSize;
    private final int batchCount;
    private final int completedBatches;
    private final long knownAffectedRows;
    private final int unknownAffectedRows;
    private final List<Integer> failedPositions;
    private final BatchStatus status;
    private final Throwable cause;

    private BatchResult(int totalItems, int batchSize, int batchCount, int completedBatches,
                        long knownAffectedRows, int unknownAffectedRows,
                        List<Integer> failedPositions, BatchStatus status, Throwable cause) {
        if (totalItems < 0 || batchSize < 1 || batchCount < 0 || completedBatches < 0
                || unknownAffectedRows < 0 || knownAffectedRows < 0) {
            throw new IllegalArgumentException("invalid batch result values");
        }
        if (failedPositions == null || status == null) {
            throw new IllegalArgumentException("failedPositions and status must not be null");
        }
        this.totalItems = totalItems;
        this.batchSize = batchSize;
        this.batchCount = batchCount;
        this.completedBatches = completedBatches;
        this.knownAffectedRows = knownAffectedRows;
        this.unknownAffectedRows = unknownAffectedRows;
        this.failedPositions = Collections.unmodifiableList(new ArrayList<>(failedPositions));
        this.status = status;
        this.cause = cause;
    }

    public static BatchResult of(int totalItems, int batchSize, int batchCount, int completedBatches,
                                 long knownAffectedRows, int unknownAffectedRows,
                                 List<Integer> failedPositions, BatchStatus status, Throwable cause) {
        return new BatchResult(totalItems, batchSize, batchCount, completedBatches,
                knownAffectedRows, unknownAffectedRows, failedPositions, status, cause);
    }

    public static Builder builder(int totalItems, int batchSize) {
        return new Builder(totalItems, batchSize);
    }

    public int totalItems() { return totalItems; }
    public int batchSize() { return batchSize; }
    public int batchCount() { return batchCount; }
    public int completedBatches() { return completedBatches; }
    public long knownAffectedRows() { return knownAffectedRows; }
    public int unknownAffectedRows() { return unknownAffectedRows; }
    public List<Integer> failedPositions() { return failedPositions; }
    public BatchStatus status() { return status; }
    public Throwable cause() { return cause; }
    public boolean isSuccess() { return status == BatchStatus.SUCCESS; }

    public static final class Builder {
        private final int totalItems;
        private final int batchSize;
        private final int batchCount;
        private int completedBatches;
        private long knownAffectedRows;
        private int unknownAffectedRows;
        private final List<Integer> failedPositions = new ArrayList<>();
        private Throwable cause;

        private Builder(int totalItems, int batchSize) {
            if (totalItems < 0 || batchSize < 1) throw new IllegalArgumentException("invalid batch values");
            this.totalItems = totalItems;
            this.batchSize = batchSize;
            this.batchCount = (totalItems + batchSize - 1) / batchSize;
        }

        public Builder completed(int start, int[] counts) {
            if (counts != null) {
                for (int i = 0; i < counts.length; i++) {
                    if (counts[i] == Statement.SUCCESS_NO_INFO) unknownAffectedRows++;
                    else if (counts[i] == Statement.EXECUTE_FAILED) failedPositions.add(start + i);
                    else if (counts[i] >= 0) knownAffectedRows += counts[i];
                }
            }
            completedBatches++;
            return this;
        }

        public Builder failed(int position, Throwable cause) {
            failedPositions.add(position);
            this.cause = cause;
            return this;
        }

        public boolean hasFailure() { return !failedPositions.isEmpty(); }

        public BatchResult build() {
            BatchStatus status = failedPositions.isEmpty()
                    ? BatchStatus.SUCCESS
                    : (completedBatches > 0 ? BatchStatus.PARTIAL_FAILURE : BatchStatus.FAILURE);
            return BatchResult.of(totalItems, batchSize, batchCount, completedBatches,
                    knownAffectedRows, unknownAffectedRows, failedPositions, status, cause);
        }
    }
}
