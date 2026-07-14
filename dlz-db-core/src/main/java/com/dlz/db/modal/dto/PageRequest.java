package com.dlz.db.modal.dto;

import java.util.Objects;

public final class PageRequest {
    private final int pageNo;
    private final int pageSize;

    private PageRequest(int pageNo, int pageSize) {
        if (pageNo < 1) {
            throw new IllegalArgumentException("pageNo must be greater than or equal to 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public static PageRequest of(int pageNo, int pageSize) {
        return new PageRequest(pageNo, pageSize);
    }

    public int pageNo() {
        return pageNo;
    }

    public int pageSize() {
        return pageSize;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PageRequest)) {
            return false;
        }
        PageRequest that = (PageRequest) other;
        return pageNo == that.pageNo && pageSize == that.pageSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNo, pageSize);
    }
}
