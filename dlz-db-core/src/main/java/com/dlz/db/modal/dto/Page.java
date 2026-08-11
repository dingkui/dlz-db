package com.dlz.db.modal.dto;

import com.dlz.db.annotation.Schema;
import com.dlz.db.annotation.SchemaField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
@Setter
@Accessors(chain = true)
@Schema("分页对象")
public class Page<T> extends Sort<Page> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE_SIZE = 20;

    @SchemaField("当前页码，从1开始")
    private long current = 0;
    @SchemaField("每页条数")
    private long size = DEFAULT_PAGE_SIZE;
    @SchemaField("数据总条数")
    private long total;
    @SchemaField("总页数")
    private long pages;
    @SchemaField("数据集合")
    private List<T> records;

    public static <T> Page<T> build(long current, long size, Order... order) {
        return new Page<>(current, size, order);
    }

    public static <T> Page<T> of(List<T> records, long total, PageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        Page<T> page = new Page<>(request.pageNo(), request.pageSize());
        page.setTotal(total);
        page.setRecords(records == null ? new ArrayList<T>() : new ArrayList<>(records));
        return page;
    }

    public List<T> records() { return records; }
    public long total() { return total; }
    public int pageNo() { return (int) current; }
    public int pageSize() { return (int) size; }
    public int pages() { return (int) pages; }
    public boolean hasNext() { return current < pages; }
    public boolean hasPrevious() { return current > 1; }


    public static <T> Page<T> build(Order... order) {
        return new Page<>(order);
    }

    public Page(long current, long size, Order... order) {
        super(order);
        this.setSize(size);
        this.setCurrent(current);
    }

    public Page(Order... order) {
        super(order);
    }
    public Page() {
        super();
    }
    public <E> Page<E> cover(Function<T, E> c) {
        Page<E> page = new Page<>(current,size);
        page.setTotal(this.total);
        page.setRecords(this.records.stream().map(c).collect(Collectors.toList()));
        return page;
    }

    public Page<T> setSize(long size) {
        if (size > 5000) {
            size = 5000;
        }
        this.size = size;
        cnt();
        return this;
    }

    public Page<T> setCurrent(long current) {
        this.current = current;
        return cnt();
    }

    public Page<T> setTotal(long total) {
        this.total = total;
        return cnt();
    }


    public Page<T> doPage(Supplier<Long> total, Supplier<List<T>> record) {
        if(getCurrent()<=0){
            setCurrent(1);
        }
        setTotal(total.get());
        //是否需要查询列表（需要统计条数并且条数是0的情况不查询，直接返回空列表）
        if (this.total > 0) {
            this.records = record.get();
        } else {
            this.records = new ArrayList<>(0);
        }
        return this;
    }

    private Page<T> cnt() {
        if (size <= 0) {
            setPages(1);
            setCurrent(0);
            return this;
        }
        pages = total % size == 0 ? total / size : total / size + 1;
        if (pages > 0 && current > pages) {
            setCurrent(pages);
        }
        return this;
    }
}