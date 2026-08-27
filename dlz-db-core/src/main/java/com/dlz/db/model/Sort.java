package com.dlz.db.model;

import com.dlz.db.core.anno.Schema;
import com.dlz.db.core.anno.SchemaField;
import com.dlz.db.internal.inf.IChained;
import com.dlz.db.util.DbConvertUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
@Schema("排序对象")
public class Sort<T extends Sort> implements Serializable, IChained<T> {
    private static final long serialVersionUID = 1L;
    @SchemaField("排序")
    private List<Order> orders=new ArrayList<>();

    public Sort(Order... order){
        this.orders.addAll( Arrays.asList(order));
    }
    public Sort(List<Order> orders){
        this.orders.addAll(orders);
    }
    public String getSortSql() {
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        return " ORDER BY " + orders.stream()
                .map(o -> {
                    final String column = o.getColumn();
                    DbConvertUtil.validateDbName(column,"排序字段");
                    return DbConvertUtil.toDbNames(column) + (o.isAsc() ? " ASC" : " DESC");
                })
                .collect(Collectors.joining(","));
    }

    public T removeOrder(Predicate<Order> filter) {
        for(int i = this.orders.size() - 1; i >= 0; --i) {
            if (filter.test(this.orders.get(i))) {
                this.orders.remove(i);
            }
        }
        return me();
    }

    public T addOrder(Order... items) {
        return addOrder(Arrays.asList(items));
    }

    public T addOrder(List<Order> items) {
        List<Order> collect = items.stream().filter(o -> o.getColumn() != null).collect(Collectors.toList());
        if(!collect.isEmpty()){
            this.orders.addAll(collect);
        }
        return me();
    }

    @Override
    public T me() {
        return (T)this;
    }
}