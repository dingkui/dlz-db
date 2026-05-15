package com.dlz.db.support.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ColumnInfo {
    private String columnName;
    private String columnType;
    private String columnComment;
    private Class<?> javaType;
}