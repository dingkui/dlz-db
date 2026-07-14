package com.dlz.db.modal.options.point.context;

/** 不可变分页参数。 */
public final class Pagination {
    private final long offset;
    private final int limit;

    public Pagination(long offset, int limit) {
        if (offset < 0) throw new IllegalArgumentException("offset must not be negative");
        if (limit < 1) throw new IllegalArgumentException("limit must be greater than zero");
        this.offset = offset;
        this.limit = limit;
    }

    public long getOffset() { return offset; }
    public int getLimit() { return limit; }
}
